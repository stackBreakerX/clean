package com.ohz.clean.common

import com.ohz.clean.common.dataarea.DataArea
import eu.darken.sdmse.common.pkgs.Pkg

interface MarkerSource {
    suspend fun getMarkerForLocation(areaType: DataArea.Type): Collection<Marker>
    suspend fun getMarkerForPkg(pkgId: Pkg.Id): Collection<Marker>
    suspend fun match(areaType: DataArea.Type, prefixFreeBasePath: List<String>): Collection<Marker.Match>
}