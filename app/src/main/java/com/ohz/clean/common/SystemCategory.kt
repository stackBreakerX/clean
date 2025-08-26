package com.ohz.clean.common

import com.ohz.clean.common.storage.StorageId
import kotlin.collections.sumOf

data class SystemCategory(
    override val storageId: StorageId,
    override val groups: Collection<ContentGroup>,
    val spaceUsedOverride: Long? = null,
) : ContentCategory {
    override val spaceUsed: Long
        get() = spaceUsedOverride ?: groups.sumOf { it.groupSize }
}