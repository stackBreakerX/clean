package com.ohz.clean.common

import com.ohz.clean.common.dataarea.DataArea
import eu.darken.sdmse.common.files.APath

interface CSIProcessor {

    suspend fun hasJurisdiction(type: DataArea.Type): Boolean

    suspend fun identifyArea(target: APath): AreaInfo?

    suspend fun findOwners(areaInfo: AreaInfo): Result

    data class Result(
        val owners: Set<Owner> = emptySet(),
        val hasKnownUnknownOwner: Boolean = false,
    )

}