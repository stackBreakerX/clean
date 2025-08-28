package com.ohz.clean.common

import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.collections.map
import kotlin.collections.toSet

val AppCleaner.Data?.hasData: Boolean
    get() = this?.junks?.isNotEmpty() ?: false

suspend fun Collection<Exclusion.Path>.excludeNestedLookups(
    matches: Collection<ExpendablesFilter.Match>
): Set<ExpendablesFilter.Match> {
    var temp = matches.map { it.lookup }.toSet()
    this.forEach { temp = it.excludeNestedLookups(temp) }
    return matches
        .filter { temp.contains(it.lookup) }
        .toSet()
}