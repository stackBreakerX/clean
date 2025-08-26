package com.ohz.clean.common

import android.os.Parcelable
import eu.darken.sdmse.common.pkgs.Pkg
import eu.darken.sdmse.common.pkgs.features.InstallId
import eu.darken.sdmse.common.user.UserHandle2
import kotlinx.parcelize.Parcelize

@Parcelize
data class Owner(
    val pkgId: Pkg.Id,
    val userHandle: UserHandle2,
    val flags: Set<Marker.Flag> = emptySet(),
) : Parcelable {

    val installId: InstallId
        get() = InstallId(pkgId, userHandle)

    fun hasFlag(flag: Marker.Flag): Boolean = flags.contains(flag)

}