package com.ohz.clean.common

import com.ohz.clean.common.dataarea.DataArea
import com.ohz.clean.common.dataarea.isPublic
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import eu.darken.sdmse.common.datastore.value
import eu.darken.sdmse.common.debug.logging.log
import eu.darken.sdmse.common.debug.logging.logTag
import eu.darken.sdmse.common.files.APath
import eu.darken.sdmse.common.files.APathLookup
import eu.darken.sdmse.common.files.GatewaySwitch
import eu.darken.sdmse.common.files.Segments
import eu.darken.sdmse.common.pkgs.Pkg
import eu.darken.sdmse.common.storage.StorageEnvironment
import javax.inject.Inject
import javax.inject.Provider
import kotlin.collections.isNotEmpty
import kotlin.collections.map

@Reusable
class DefaultCachesPublicFilter @Inject constructor(
    environment: StorageEnvironment,
    private val gatewaySwitch: GatewaySwitch,
) : BaseExpendablesFilter() {

    private val cacheFolderPrefixes = environment.ourExternalCacheDirs.map { it.name }

    override suspend fun initialize() {
        log(TAG) { "initialize()" }
    }

    override suspend fun match(
        pkgId: Pkg.Id,
        target: APathLookup<APath>,
        areaType: DataArea.Type,
        pfpSegs: Segments
    ): ExpendablesFilter.Match? {
        if (pfpSegs.isNotEmpty() && IGNORED_FILES.contains(pfpSegs[pfpSegs.size - 1])) return null

        if (!areaType.isPublic) return null

        return if (pfpSegs.size >= 3 && cacheFolderPrefixes.contains(pfpSegs[1])) {
            target.toDeletionMatch()
        } else {
            null
        }
    }

    override suspend fun process(
        targets: Collection<ExpendablesFilter.Match>,
        allMatches: Collection<ExpendablesFilter.Match>
    ): ExpendablesFilter.ProcessResult {
        return deleteAll(
            targets.map { it as ExpendablesFilter.Match.Deletion },
            gatewaySwitch,
            allMatches
        )
    }

    @Reusable
    class Factory @Inject constructor(
        private val filterProvider: Provider<DefaultCachesPublicFilter>
    ) : ExpendablesFilter.Factory {
        override suspend fun isEnabled(): Boolean = true
        override suspend fun create(): ExpendablesFilter = filterProvider.get()
    }

    @InstallIn(SingletonComponent::class)
    @Module
    abstract class DIM {
        @Binds @IntoSet abstract fun mod(mod: Factory): ExpendablesFilter.Factory
    }

    companion object {
        private val TAG = logTag("AppCleaner", "Scanner", "Filter", "DefaultCaches", "Public")
        private val IGNORED_FILES: Collection<String> = listOf(
            ".nomedia"
        )
    }
}