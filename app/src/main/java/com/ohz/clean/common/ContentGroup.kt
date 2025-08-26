package com.ohz.clean.common

import android.os.Parcelable
import eu.darken.sdmse.common.ca.CaString
import kotlinx.parcelize.Parcelize
import java.util.UUID
import kotlin.collections.sumOf

data class ContentGroup(
    val id: Id = Id(),
    val label: CaString?,
    val contents: Collection<ContentItem> = emptyList(),
) {

    val groupSize: Long
        get() = contents.sumOf { it.size ?: 0L }

    @Parcelize
    data class Id(val value: String = UUID.randomUUID().toString()) : Parcelable
}
