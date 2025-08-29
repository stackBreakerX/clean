package com.ohz.clean.ui.base.adapter.selection

import androidx.recyclerview.selection.SelectionTracker
import com.ohz.clean.ui.base.adapter.modular.ModularAdapter

class ItemSelectionMod(
    private val tracker: SelectionTracker<String>,
) : ModularAdapter.Module.Binder<ModularAdapter.VH> {

    override fun onBindModularVH(
        adapter: ModularAdapter<ModularAdapter.VH>,
        vh: ModularAdapter.VH,
        pos: Int,
        payloads: MutableList<Any>
    ) {
        if (vh !is SelectableVH) return

        vh.updatedSelectionState(tracker.isSelected(vh.itemSelectionKey))

        vh.itemSelectionKey
            ?.let { key ->
                vh.itemView.setOnLongClickListener {
                    tracker.select(key)
                    true
                }
            }
            ?: vh.itemView.setOnLongClickListener(null)

    }
}