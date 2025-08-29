package com.ohz.clean.common

import eu.darken.sdmse.common.files.APathLookup
import kotlin.collections.sumOf
import kotlin.reflect.KClass

data class Corpse(
    val filterType: KClass<out CorpseFilter>,
    val ownerInfo: OwnerInfo,
    val lookup: APathLookup<*>,
    val content: Collection<APathLookup<*>>,
    val isWriteProtected: Boolean = false,
    val riskLevel: RiskLevel = RiskLevel.NORMAL,
) {
    val identifier: CorpseIdentifier
        get() = lookup.lookedUp

    val areaInfo: AreaInfo
        get() = ownerInfo.areaInfo

    val size: Long
        get() = lookup.size + content.sumOf { it.size }

    override fun toString(): String =
        "Corpse(identifier=$identifier, type=${areaInfo.type}, owners=${ownerInfo.owners}, size=$size)"
}