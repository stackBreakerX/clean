package com.ohz.clean.common

interface AppSourceCheck {

    suspend fun process(areaInfo: AreaInfo): Result


    data class Result(
        val owners: Set<Owner> = emptySet(),
        val hasKnownUnknownOwner: Boolean = false,
    )
}