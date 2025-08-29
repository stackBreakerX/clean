package com.ohz.clean.common

import eu.darken.sdmse.common.ca.CaDrawable
import eu.darken.sdmse.common.ca.CaString
import kotlin.collections.sumOf

data class FilterContent(
    val identifier: FilterIdentifier,
    val icon: CaDrawable,
    val label: CaString,
    val description: CaString,
    val items: Collection<SystemCleanerFilter.Match>
) {
    val size: Long
        get() = items.sumOf { it.lookup.size }
}