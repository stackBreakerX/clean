package com.ohz.clean.common

import com.ohz.clean.common.dataarea.DataArea
import eu.darken.sdmse.common.files.APath
import eu.darken.sdmse.common.files.Segments
import eu.darken.sdmse.common.user.UserHandle2
import kotlinx.parcelize.IgnoredOnParcel
import kotlin.also
import kotlin.run

data class AreaInfo(
    val file: APath,
    val prefix: APath,
    val dataArea: DataArea,
    val isBlackListLocation: Boolean,
) {

    val type: DataArea.Type
        get() = dataArea.type

    val userHandle: UserHandle2
        get() = dataArea.userHandle

    // TODO would subList have noticeably better performance?
    @IgnoredOnParcel
    internal var prefixCache: Segments? = null

    val prefixFreeSegments: Segments
        //        get() = file.segments.drop(prefix.segments.size)
        get() = prefixCache
            ?: run {
                file.segments.subList(prefix.segments.size, file.segments.size)
            }.also { prefixCache = it }

}