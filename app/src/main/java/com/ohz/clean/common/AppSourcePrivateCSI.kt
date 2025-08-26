package com.ohz.clean.common

import com.ohz.clean.common.dataarea.DataArea
import com.ohz.clean.common.dataarea.DataAreaManager
import com.ohz.clean.common.dataarea.currentAreas
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import eu.darken.sdmse.common.debug.logging.logTag
import eu.darken.sdmse.common.files.APath
import eu.darken.sdmse.common.files.isAncestorOf
import javax.inject.Inject

@Reusable
class AppSourcePrivateCSI @Inject constructor(
    private val areaManager: DataAreaManager,
    private val sourceChecks: Set<@JvmSuppressWildcards AppSourceCheck>,
    private val similarityFilter: SimilarityFilter,
) : LocalCSIProcessor {

    override suspend fun hasJurisdiction(type: DataArea.Type): Boolean = type == DataArea.Type.APP_APP_PRIVATE

    override suspend fun identifyArea(target: APath): AreaInfo? = areaManager.currentAreas()
        .filter { it.type == DataArea.Type.APP_APP_PRIVATE }
        .mapNotNull { area ->
            if (!area.path.isAncestorOf(target)) return@mapNotNull null

            AreaInfo(
                dataArea = area,
                file = target,
                prefix = area.path,
                isBlackListLocation = true
            )
        }
        .singleOrNull()

    override suspend fun findOwners(areaInfo: AreaInfo): CSIProcessor.Result {
        require(hasJurisdiction(areaInfo.type)) { "Wrong jurisdiction: ${areaInfo.type}" }

        return sourceChecks
            .map { it.process(areaInfo) }
            .let { subResults ->
                CSIProcessor.Result(
                    owners = subResults
                        .map { it.owners }
                        .flatten()
                        .let { similarityFilter.filterFalsePositives(areaInfo, it) }
                        .toSet(),
                    hasKnownUnknownOwner = subResults.any { it.hasKnownUnknownOwner },
                )
            }
    }

    @Module @InstallIn(SingletonComponent::class)
    abstract class DIM {
        @Binds @IntoSet abstract fun mod(mod: AppSourcePrivateCSI): CSIProcessor
    }

    companion object {
        val TAG: String = logTag("CSI", "AppSource", "Private")
    }
}