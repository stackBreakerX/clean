package com.ohz.clean.ui.base.adapter.differ

import com.ohz.clean.ui.base.adapter.ListItem


interface DifferItem : ListItem {
    val stableId: Long

    val payloadProvider: ((DifferItem, DifferItem) -> DifferItem?)?
        get() = null
}