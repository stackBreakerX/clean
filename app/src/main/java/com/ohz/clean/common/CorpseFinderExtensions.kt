package com.ohz.clean.common

import kotlin.collections.isNotEmpty

val CorpseFinder.Data?.hasData: Boolean
    get() = this?.corpses?.isNotEmpty() ?: false