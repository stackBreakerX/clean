package com.ohz.clean.ui.base.adapter.selection

import androidx.recyclerview.selection.ItemKeyProvider
import com.ohz.clean.ui.base.adapter.DataAdapter

class ItemSelectionKeyProvider(
    private val adapter: DataAdapter<out SelectableItem>
) : ItemKeyProvider<String>(SCOPE_MAPPED) {

    override fun getKey(position: Int): String? {
        return adapter.data[position].itemSelectionKey
    }

    override fun getPosition(key: String): Int {
        return adapter.data.indexOfFirst { it.itemSelectionKey == key }
    }
}