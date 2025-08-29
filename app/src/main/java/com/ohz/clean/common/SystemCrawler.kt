package com.ohz.clean.common

import com.ohz.clean.common.dataarea.DataArea
import com.ohz.clean.common.dataarea.DataAreaManager
import com.ohz.clean.common.dataarea.currentAreas
import com.ohz.clean.ui.view.progress.Progress
import com.ohz.clean.ui.view.progress.updateProgressCount
import com.ohz.clean.ui.view.progress.updateProgressPrimary
import com.ohz.clean.ui.view.progress.updateProgressSecondary
import eu.darken.sdmse.common.coroutine.DispatcherProvider
import eu.darken.sdmse.common.debug.Bugs
import eu.darken.sdmse.common.debug.logging.Logging.Priority.ERROR
import eu.darken.sdmse.common.debug.logging.Logging.Priority.INFO
import eu.darken.sdmse.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.sdmse.common.debug.logging.Logging.Priority.WARN
import eu.darken.sdmse.common.debug.logging.asLog
import eu.darken.sdmse.common.debug.logging.log
import eu.darken.sdmse.common.debug.logging.logTag
import eu.darken.sdmse.common.files.APathGateway
import eu.darken.sdmse.common.files.APathLookup
import eu.darken.sdmse.common.files.GatewaySwitch
import eu.darken.sdmse.common.files.Segments
import eu.darken.sdmse.common.files.isDirectory
import eu.darken.sdmse.common.files.segs
import eu.darken.sdmse.common.files.startsWith
import eu.darken.sdmse.common.files.walk
import eu.darken.sdmse.common.flow.throttleLatest
import eu.darken.sdmse.common.sharedresource.useRes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import java.io.IOException
import javax.inject.Inject
import kotlin.also
import kotlin.collections.all
import kotlin.collections.any
import kotlin.collections.filter
import kotlin.collections.firstNotNullOfOrNull
import kotlin.collections.flatten
import kotlin.collections.isNotEmpty
import kotlin.collections.map
import kotlin.collections.none
import kotlin.collections.onEach
import kotlin.collections.plus
import kotlin.collections.set
import kotlin.collections.single
import kotlin.collections.toSet
import kotlin.to

class SystemCrawler @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val areaManager: DataAreaManager,
    private val gatewaySwitch: GatewaySwitch,
    private val forensics: FileForensics,
) : Progress.Host, Progress.Client {

    private val progressPub = MutableStateFlow<Progress.Data?>(Progress.Data())
    override val progress: Flow<Progress.Data?> = progressPub.throttleLatest(250)

    override fun updateProgress(update: (Progress.Data?) -> Progress.Data?) {
        progressPub.value = update(progressPub.value)
    }

    private val matchesInternal = MutableSharedFlow<MatchEvent>(extraBufferCapacity = 1024)
    val matchEvents: Flow<MatchEvent> = matchesInternal

    suspend fun crawl(filters: Set<SystemCleanerFilter>): Collection<FilterContent> {
        log(TAG) { "crawl()" }
        updateProgressPrimary(R.string.general_progress_searching)
        updateProgressSecondary(R.string.general_progress_generating_searchpaths)
        updateProgressCount(Progress.Count.Indeterminate())


        val currentAreas = areaManager.currentAreas()
        val targetAreas = filters
            .map { it.targetAreas() }.flatten().toSet()
            .map { type -> currentAreas.filter { it.type == type } }.flatten().toSet()
        log(TAG) { "Target areas wanted by filters: $targetAreas" }

        val globalSkips = mutableSetOf<Segments>()
        if (targetAreas.all { it.path.segments != segs("", "data", "data") }) {
            globalSkips.add(segs("", "data", "data"))
            globalSkips.add(segs("", "data", "user", "0"))
        }
        globalSkips.add(segs("", "data", "media", "0"))
        log(TAG) { "Global skip segments: $globalSkips" }

        val sieveContents = mutableMapOf<FilterIdentifier, Set<SystemCleanerFilter.Match>>()

        listOf(gatewaySwitch, forensics).useRes {
            targetAreas
                .asFlow()
                .flowOn(dispatcherProvider.Default)
                .flatMapMerge(2) { targetArea ->
                    val filter: suspend (APathLookup<*>) -> Boolean =
                        filter@{ toCheck: APathLookup<*> ->
                            if (globalSkips.any { toCheck.segments.startsWith(it) }) {
                                log(TAG, INFO) { "Excluded $toCheck by global skip" }
                                return@filter false
                            }

                            if (toCheck.isDirectory) {
                                val contentAreaCheck = toCheck.child("peek-content-area")
                                val area: DataArea? =
                                    forensics.identifyArea(contentAreaCheck)?.dataArea

                                if (area?.type != targetArea.type) {
                                    if (Bugs.isTrace) log(TAG, VERBOSE) {
                                        "AreaCheck: Want ${targetArea.type}, Got ${area?.type} for ${toCheck.path}"
                                    }
                                    return@filter false
                                }
                            }

//                        exclusions.none { excl ->
//                            excl.match(toCheck).also { if (it) log(TAG, INFO) { "Excluded $toCheck by $excl" } }
//                        }
                            true
                        }
                    log(TAG) { "Scanning $targetArea" }

                    targetArea.path
                        .walk(
                            gatewaySwitch,
                            options = APathGateway.WalkOptions(onFilter = filter)
                        )
                        .map { targetArea to it }
                        .onCompletion { log(TAG) { "Finished scanning $targetArea" } }
                }
                .buffer(2048 * 4)
                .flatMapMerge(4) { (area, item) ->
                    flow {
                        if (Bugs.isTrace) log(TAG, VERBOSE) { "Trying to match $item" }
                        updateProgressSecondary(item.path)
                        val matched: Pair<SystemCleanerFilter, SystemCleanerFilter.Match>? = filters
                            .filter { it.targetAreas().contains(area.type) }
                            .firstNotNullOfOrNull { filter ->
                                val match = try {
                                    filter.match(item)
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: IOException) {
                                    log(
                                        TAG,
                                        WARN
                                    ) { "IO error while matching ($filter): ${e.asLog()}" }
                                    null
                                } catch (e: Exception) {
                                    log(TAG, ERROR) { "Sieve failed ($filter): ${e.asLog()}" }
                                    throw SystemCleanerFilterException(filter, e)
                                }
                                if (match != null) filter to match else null
                            }
                        if (matched != null) emit(item to matched)
                    }
                }
                .collect { (item, matched) ->
                    log(TAG, INFO) { "Filter match: $matched <- $item" }
                    sieveContents[matched.first.identifier] =
                        (sieveContents[matched.first.identifier] ?: emptySet()).plus(matched.second)
                    matchesInternal.emit(MatchEvent(matched.first, item))
                }
        }

        val firstPass = sieveContents.map { entry ->
            log(TAG, INFO) { "${entry.key} has ${entry.value.size} matches (first pass)." }
            val filter = filters.single { it.identifier == entry.key }
            FilterContent(
                identifier = entry.key,
                icon = filter.getIcon(),
                label = filter.getLabel(),
                description = filter.getDescription(),
                items = entry.value,
            )
        }

        // With nested exclusions applied

        return firstPass
            .map { it.copy(items = it.items) }
            .onEach {
                log(
                    TAG,
                    INFO
                ) { "${it.identifier} has ${it.items.size} matches (second pass)." }
            }
            .filter { it.items.isNotEmpty() }
    }

    data class MatchEvent(
        val filter: SystemCleanerFilter,
        val match: APathLookup<*>,
    )

    companion object {
        private val TAG = logTag("SystemCleaner", "Crawler")
    }
}