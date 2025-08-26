package com.ohz.clean.common

import com.ohz.clean.common.dataarea.DataArea
import eu.darken.sdmse.common.debug.logging.Logging.Priority.WARN
import eu.darken.sdmse.common.debug.logging.log
import eu.darken.sdmse.common.files.APath
import eu.darken.sdmse.common.pkgs.features.Installed
import kotlin.collections.filter
import kotlin.collections.map

fun Installed.getPrivateDataDirs(areas: Collection<DataArea>): Collection<APath> {
    val privateAreas = areas
        .filter { it.type == DataArea.Type.PRIVATE_DATA }
        .filter { it.userHandle == userHandle }

    if (privateAreas.isEmpty()) log(WARN) { "No PRIVATE_DATA areas provided" }

    return privateAreas.map { it.path.child(packageName) }
}