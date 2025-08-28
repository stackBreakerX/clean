package com.ohz.clean.common

import kotlin.collections.any


fun Marker.hasFlags(vararg flag: Marker.Flag): Boolean {
    return flag.any { flags.contains(it) }
}