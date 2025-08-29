package com.ohz.clean.common

import com.ohz.clean.ui.view.progress.Progress
import com.ohz.clean.ui.view.progress.updateProgressPrimary
import com.ohz.clean.ui.view.progress.updateProgressSecondary
import com.ohz.clean.ui.view.progress.withProgress
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import eu.darken.sdmse.common.ca.CaString
import eu.darken.sdmse.common.ca.caString
import eu.darken.sdmse.common.ca.toCaString
import eu.darken.sdmse.common.coroutine.AppScope
import eu.darken.sdmse.common.debug.logging.Logging.Priority.INFO
import eu.darken.sdmse.common.debug.logging.Logging.Priority.WARN
import eu.darken.sdmse.common.debug.logging.log
import eu.darken.sdmse.common.debug.logging.logTag
import eu.darken.sdmse.common.files.APath
import eu.darken.sdmse.common.files.GatewaySwitch
import eu.darken.sdmse.common.files.WriteException
import eu.darken.sdmse.common.files.delete
import eu.darken.sdmse.common.files.filterDistinctRoots
import eu.darken.sdmse.common.files.isAncestorOf
import eu.darken.sdmse.common.files.matches
import eu.darken.sdmse.common.flow.replayingShare
import eu.darken.sdmse.common.pkgs.pkgops.PkgOps
import eu.darken.sdmse.common.root.RootManager
import eu.darken.sdmse.common.sharedresource.SharedResource
import eu.darken.sdmse.common.sharedresource.keepResourceHoldersAlive
import eu.darken.sdmse.common.user.UserManager2
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.also
import kotlin.collections.any
import kotlin.collections.filter
import kotlin.collections.flatMap
import kotlin.collections.flatten
import kotlin.collections.forEach
import kotlin.collections.isNullOrEmpty
import kotlin.collections.map
import kotlin.collections.mapNotNull
import kotlin.collections.none
import kotlin.collections.onEach
import kotlin.collections.plus
import kotlin.collections.set
import kotlin.collections.single
import kotlin.collections.sumOf
import kotlin.collections.toSet
import kotlin.let

@Singleton
class CorpseFinder @Inject constructor(
    @param:AppScope private val appScope: CoroutineScope,
    private val filterFactories: Set<@JvmSuppressWildcards CorpseFilter.Factory>,
    private val fileForensics: FileForensics,
    private val gatewaySwitch: GatewaySwitch,
    private val userManager: UserManager2,
    private val pkgOps: PkgOps,
    rootManager: RootManager,
    private val appInventorySetupModule: InventorySetupModule,
) : SDMTool, Progress.Client {

    override val type: SDMTool.Type = SDMTool.Type.CORPSEFINDER

    override val sharedResource = SharedResource.createKeepAlive(TAG, appScope)

    private val progressPub = MutableStateFlow<Progress.Data?>(null)
    override val progress: Flow<Progress.Data?> = progressPub
    override fun updateProgress(update: (Progress.Data?) -> Progress.Data?) {
        progressPub.value = update(progressPub.value)
    }

    private val internalData = MutableStateFlow(null as Data?)

    override val state: Flow<State> = combine(
        internalData,
        progress,
        rootManager.useRoot,
    ) { data, progress, useRoot ->
        State(
            data = data,
            progress = progress,
            isFilterPrivateDataAvailable = useRoot,
            isFilterDalvikCacheAvailable = useRoot,
            isFilterArtProfilesAvailable = useRoot,
            isFilterAppLibrariesAvailable = useRoot,
            isFilterAppSourcesAvailable = useRoot,
            isFilterPrivateAppSourcesAvailable = useRoot,
            isFilterEncryptedAppResourcesAvailable = useRoot,
        )
    }.replayingShare(appScope)

    private val toolLock = Mutex()
    override suspend fun submit(task: SDMTool.Task): SDMTool.Task.Result = toolLock.withLock {
        task as CorpseFinderTask
        log(TAG, INFO) { "submit($task) starting..." }
        updateProgress { Progress.Data() }

        try {
            val result = keepResourceHoldersAlive(fileForensics, gatewaySwitch, pkgOps) {
                when (task) {
                    is CorpseFinderScanTask -> performScan(task)
                    is CorpseFinderDeleteTask -> deleteCorpses(task)
                    is UninstallWatcherTask -> {
                        performScan(CorpseFinderScanTask(pkgIdFilter = setOf(task.target)))

                        val targets = internalData.value!!.corpses
                            .filter { it.ownerInfo.getOwner(task.target) != null }
                            .map { it.identifier }
                            .onEach { log(TAG) { "Uninstall watcher found target $it" } }
                            .toSet()

                        log(TAG) { "Watcher auto delete enabled=${task.autoDelete}" }

                        val internalDeleteResult = if (task.autoDelete) {
                            deleteCorpses(CorpseFinderDeleteTask(targetCorpses = targets)).also {
                                val watcherResult = ExternalWatcherResult.Deletion(
                                    appName = pkgOps.getLabel(task.target)?.toCaString(),
                                    pkgId = task.target,
                                    deletedItems = it.affectedCount,
                                    freedSpace = it.affectedSpace,
                                )
                            }
                        } else {
                            val watcherResult = ExternalWatcherResult.Scan(
                                pkgId = task.target,
                                foundItems = targets.size
                            )
                            null
                        }

                        UninstallWatcherTask.Success(
                            foundItems = targets.size,
                            affectedPaths = internalDeleteResult?.affectedPaths ?: emptySet(),
                            affectedSpace = internalDeleteResult?.affectedSpace ?: 0L,
                        )
                    }

                    is CorpseFinderSchedulerTask -> {
                        performScan()
                        deleteCorpses().let {
                            CorpseFinderSchedulerTask.Success(
                                affectedSpace = it.affectedSpace,
                                affectedPaths = it.affectedPaths,
                            )
                        }
                    }

                    is CorpseFinderOneClickTask -> {
                        performScan()
                        deleteCorpses().let {
                            CorpseFinderOneClickTask.Success(
                                affectedSpace = it.affectedSpace,
                                affectedPaths = it.affectedPaths,
                            )
                        }
                    }
                }
            }
            internalData.value = internalData.value?.copy(
                lastResult = result,
            )
            log(TAG, INFO) { "submit($task) finished: $result" }
            result
        } catch (e: CancellationException) {
            throw e
        } finally {
            updateProgress { null }
        }
    }

    private suspend fun performScan(
        task: CorpseFinderScanTask = CorpseFinderScanTask()
    ): CorpseFinderScanTask.Result {
        log(TAG) { "performScan(): $task" }

        if (!appInventorySetupModule.isComplete()) {
            log(TAG, WARN) { "SetupModule INVENTORY is not complete" }
            throw IncompleteSetupException(SetupModule.Type.INVENTORY)
        }

        internalData.value = null

        val filters = filterFactories
            .filter { it.isEnabled() }
            .map { it.create() }
            .onEach { log(TAG) { "Created filter: $it" } }


        val results = filters
            .map { filter ->
                filter
                    .withProgress(this@CorpseFinder) { scan() }
                    .also { log(TAG) { "$filter found ${it.size} corpses" } }
            }
            .flatten()
//            .filter { corpse ->
//                pkgExclusions.none { excl ->
//                    corpse.ownerInfo.owners.any { owner ->
//                        excl.match(owner.pkgId).also {
//                            if (it) log(TAG, INFO) { "Excluded due to $excl: $corpse" }
//                        }
//                    }
//                }
//            }
//            .filter { corpse ->
//                pathExclusions.none { excl ->
//                    excl.match(corpse.lookup).also {
//                        if (it) log(TAG, INFO) { "Excluded due to $excl: $corpse" }
//                    }
//                }
//            }
            .filter { corpse ->
                // One extra check for multi-user devices without root
                if (!userManager.hasMultiUserSupport) return@filter true

                // Another user might own corpses in secondary public storage
                if (corpse.areaInfo.userHandle != userManager.systemUser().handle) return@filter true

                if (corpse.ownerInfo.owners.any { pkgOps.isInstalleMaybe(it.pkgId, it.userHandle) }) {
                    log(TAG, WARN) { "Potential multi-user false positive: $corpse" }
                    return@filter false
                }

                return@filter true
            }

        results.forEach { log(TAG, INFO) { "Result: $it" } }

        log(TAG) { "Warming up fields..." }
        results.forEach { it.size }
        log(TAG) { "Field warm up done." }

        internalData.value = Data(
            corpses = results
        )

        return CorpseFinderScanTask.Success(
            itemCount = results.size,
            recoverableSpace = results.sumOf { it.size },
        )
    }

    private suspend fun deleteCorpses(
        task: CorpseFinderDeleteTask = CorpseFinderDeleteTask()
    ): CorpseFinderDeleteTask.Success {
        log(TAG) { "deleteCorpses(): $task" }

        val deletedCorpses = mutableSetOf<Corpse>()
        val deletedContents = mutableMapOf<Corpse, Set<APath>>()
        val snapshot = internalData.value ?: throw kotlin.IllegalStateException("Data is null")

        val targetCorpses = task.targetCorpses ?: snapshot.corpses.map { it.identifier }
        targetCorpses.forEach { targetCorpse ->
            val corpse = snapshot.corpses.single { it.identifier == targetCorpse }

            if (!task.targetContent.isNullOrEmpty()) {
                val deleted = mutableSetOf<APath>()

                task.targetContent
                    .filterDistinctRoots()
                    .forEach { targetContent ->
                        updateProgressPrimary(caString {
                            it.getString(
                                R.string.general_progress_deleting_x,
                                targetContent.userReadableName.get(it)
                            )
                        })
                        log(TAG) { "Deleting $targetContent..." }
                        updateProgressSecondary(targetContent.userReadablePath)
                        try {
                            targetContent.delete(gatewaySwitch, recursive = true)
                            log(TAG) { "Deleted $targetContent!" }
                            deleted.add(targetContent)
                        } catch (e: WriteException) {
                            log(TAG, WARN) { "Deletion failed for $targetContent: $e" }
                        }
                    }

                deletedContents[corpse] = deleted
            } else {
                updateProgressPrimary(caString {
                    it.getString(
                        R.string.general_progress_deleting_x,
                        corpse.lookup.userReadableName.get(it)
                    )
                })
                log(TAG) { "Deleting $targetCorpse..." }
                updateProgressSecondary(corpse.lookup.userReadablePath)
                try {
                    corpse.lookup.delete(gatewaySwitch, recursive = true)
                    log(TAG) { "Deleted $targetCorpse!" }
                    deletedCorpses.add(corpse)
                } catch (e: WriteException) {
                    log(TAG, WARN) { "Deletion failed for $targetCorpse: $e" }
                }
            }
        }

        updateProgressPrimary(R.string.general_progress_loading)
        updateProgressSecondary(CaString.EMPTY)

        var deletedContentSize = 0L

        internalData.value = snapshot.copy(
            corpses = snapshot.corpses
                .mapNotNull { corpse ->
                    when {
                        deletedCorpses.contains(corpse) -> null
                        deletedContents.containsKey(corpse) -> corpse.copy(
                            content = corpse.content.filter { contentItem ->
                                val isDeleted = deletedContents[corpse]!!.any { deleted ->
                                    deleted.isAncestorOf(contentItem) || deleted.matches(contentItem)
                                }
                                if (isDeleted) deletedContentSize += contentItem.size
                                !isDeleted
                            }
                        )

                        else -> corpse
                    }
                }
        )

        return CorpseFinderDeleteTask.Success(
            affectedSpace = deletedCorpses.sumOf { it.size } + deletedContentSize,
            affectedPaths = deletedCorpses.map { it.lookup.lookedUp }.toSet() + deletedContents.flatMap { it.value },
        )
    }

    suspend fun exclude(identifiers: Set<CorpseIdentifier>) = toolLock.withLock {
        log(TAG) { "exclude(): $identifiers" }

        val snapshot = internalData.value!!

        val targets = snapshot.corpses
            .filter { identifiers.contains(it.identifier) }

        val exclusions = targets.map {
            PathExclusion(
                path = it.lookup.lookedUp,
                tags = setOf(Exclusion.Tag.CORPSEFINDER),
            )
        }.toSet()
        internalData.value = snapshot.copy(
            corpses = snapshot.corpses.filter { corpse ->
                exclusions.none { it.match(corpse.lookup) }
            }
        )
    }

    data class State(
        val data: Data?,
        val progress: Progress.Data?,
        val isFilterPrivateDataAvailable: Boolean,
        val isFilterDalvikCacheAvailable: Boolean,
        val isFilterArtProfilesAvailable: Boolean,
        val isFilterAppLibrariesAvailable: Boolean,
        val isFilterAppSourcesAvailable: Boolean,
        val isFilterPrivateAppSourcesAvailable: Boolean,
        val isFilterEncryptedAppResourcesAvailable: Boolean,
    ) : SDMTool.State

    data class Data(
        val corpses: Collection<Corpse>,
        val lastResult: CorpseFinderTask.Result? = null,
    ) {
        val totalSize: Long get() = corpses.sumOf { it.size }
        val totalCount: Int get() = corpses.size
    }

    @InstallIn(SingletonComponent::class)
    @Module
    abstract class DIM {
        @Binds @IntoSet abstract fun mod(mod: CorpseFinder): SDMTool
    }

    companion object {
        private val TAG = logTag("CorpseFinder")
    }
}