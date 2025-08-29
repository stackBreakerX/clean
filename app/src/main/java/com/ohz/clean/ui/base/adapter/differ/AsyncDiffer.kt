package com.ohz.clean.ui.base.adapter.differ

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.ohz.clean.ui.base.adapter.modular.ModularAdapter
import com.ohz.clean.ui.base.adapter.modular.mods.StableIdMod
import eu.darken.sdmse.common.lists.differ.HasAsyncDiffer

class AsyncDiffer<A, T : DifferItem> internal constructor(
    adapter: A,
    compareItem: (T, T) -> Boolean = { i1, i2 -> i1.stableId == i2.stableId },
    compareItemContent: (T, T) -> Boolean = { i1, i2 -> i1 == i2 },
    determinePayload: (T, T) -> Any? = { i1, i2 ->
        when {
            i1::class == i2::class -> i1.payloadProvider?.invoke(i1, i2)
            else -> null
        }
    }
) where A : HasAsyncDiffer<T>, A : ModularAdapter<*> {
    private val callback = object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = compareItem(oldItem, newItem)
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = compareItemContent(oldItem, newItem)
        override fun getChangePayload(oldItem: T, newItem: T): Any? = determinePayload(oldItem, newItem)
    }

    private val internalList = mutableListOf<T>()
    private val listDiffer = AsyncListDiffer(adapter, callback)

    val currentList: List<T>
        get() = synchronized(internalList) { internalList }

    init {
        adapter.addMod(position = 0, mod = StableIdMod(currentList))
    }

    fun submitUpdate(newData: List<T>) {
        listDiffer.submitList(newData) {
            synchronized(internalList) {
                internalList.clear()
                internalList.addAll(newData)
            }
        }
    }
}