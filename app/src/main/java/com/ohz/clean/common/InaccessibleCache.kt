package com.ohz.clean.common

import eu.darken.sdmse.common.files.APath
import eu.darken.sdmse.common.pkgs.Pkg
import eu.darken.sdmse.common.pkgs.features.InstallId
import eu.darken.sdmse.common.user.UserHandle2

data class InaccessibleCache(
    val identifier: InstallId,
    val isSystemApp: Boolean,
    val itemCount: Int,
    val totalSize: Long,
    val publicSize: Long?,
    val theoreticalPaths: Set<APath>,
) {

    val pkgId: Pkg.Id
        get() = identifier.pkgId

    val userHandle: UserHandle2
        get() = identifier.userHandle

    val privateSize: Long = totalSize - (publicSize ?: 0L)

    val isEmpty: Boolean = totalSize == 0L
}