package com.ohz.clean.ui.base.adapter.differ

import androidx.recyclerview.widget.RecyclerView
import com.ohz.clean.ui.base.adapter.modular.ModularAdapter
import eu.darken.sdmse.common.lists.differ.HasAsyncDiffer


fun <X, T> X.update(newData: List<T>?)
        where X : HasAsyncDiffer<T>, X : RecyclerView.Adapter<*> {

    asyncDiffer.submitUpdate(newData ?: emptyList())
}

fun <A, T : DifferItem> A.setupDiffer(): AsyncDiffer<A, T>
        where A : HasAsyncDiffer<T>, A : ModularAdapter<*> =
    AsyncDiffer(this)
