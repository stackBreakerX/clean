package com.ohz.clean.common

import com.ohz.clean.common.dataarea.DataArea
import eu.darken.sdmse.common.debug.logging.log
import eu.darken.sdmse.common.debug.logging.logTag
import eu.darken.sdmse.common.pkgs.Pkg
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.onEach

@Singleton
class ClutterRepo @Inject constructor(
    _markerSources: Set<@JvmSuppressWildcards MarkerSource>,
) : MarkerSource {

    private val markerSources = _markerSources.onEach { log(TAG) { "Loaded clutter source: $it" } }

    val sourceCount: Int = markerSources.size

    override suspend fun getMarkerForPkg(pkgId: Pkg.Id): Collection<Marker> {
        val result = mutableSetOf<Marker>()
        for (markerSource in markerSources) result.addAll(markerSource.getMarkerForPkg(pkgId))
        return result
    }

    override suspend fun match(areaType: DataArea.Type, prefixFreeBasePath: List<String>): Collection<Marker.Match> {
        val result = mutableSetOf<Marker.Match>()
        for (markerSource in markerSources) result.addAll(markerSource.match(areaType, prefixFreeBasePath))
        return result
    }

    override suspend fun getMarkerForLocation(areaType: DataArea.Type): Collection<Marker> {
        val result = mutableSetOf<Marker>()
        for (markerSource in markerSources) result.addAll(markerSource.getMarkerForLocation(areaType))
        return result
    }

    companion object {
        val TAG: String = logTag("ClutterRepo")
    }
}