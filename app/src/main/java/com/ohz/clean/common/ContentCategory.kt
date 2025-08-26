package com.ohz.clean.common

import com.ohz.clean.common.storage.StorageId


sealed interface ContentCategory {
    val storageId: StorageId
    val spaceUsed: Long
    val groups: Collection<ContentGroup>
}