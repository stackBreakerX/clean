package com.ohz.clean.common

import kotlin.collections.map


fun Marker.Match.toOwners(areaInfo: AreaInfo) = this.packageNames.map {
    Owner(
        pkgId = it,
        flags = flags,
        userHandle = areaInfo.userHandle,
    )
}