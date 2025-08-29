package com.ohz.clean.ui.fragment

import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import com.ohz.clean.common.SDMTool
import com.ohz.clean.databinding.DashboardToolCardBinding
import com.ohz.clean.ui.base.adapter.binding
import com.ohz.clean.ui.view.progress.Progress
import eu.darken.sdmse.common.dpToPx
import kotlin.apply
import com.ohz.clean.R.*
import com.ohz.clean.ui.MainActionItem

class DashboardToolCard(parent: ViewGroup) :
    DashboardAdapter.BaseVH<DashboardToolCard.Item, DashboardToolCardBinding>(
        layout.dashboard_tool_card,
        parent
    ) {

    override val viewBinding = lazy { DashboardToolCardBinding.bind(itemView) }

    override val onBindData: DashboardToolCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = binding { item ->

        icon.setImageResource(
            when (item.toolType) {
                SDMTool.Type.CORPSEFINDER -> drawable.ghost
                SDMTool.Type.SYSTEMCLEANER -> drawable.ic_baseline_view_list_24
                SDMTool.Type.APPCLEANER -> drawable.ic_recycle
                SDMTool.Type.DEDUPLICATOR -> drawable.ic_content_duplicate_24
                SDMTool.Type.APPCONTROL, SDMTool.Type.ANALYZER -> 0
            }
        )
        title.setText(
            when (item.toolType) {
                SDMTool.Type.CORPSEFINDER -> string.corpsefinder_tool_name
                SDMTool.Type.SYSTEMCLEANER -> string.systemcleaner_tool_name
                SDMTool.Type.APPCLEANER -> string.appcleaner_tool_name
                SDMTool.Type.DEDUPLICATOR -> string.deduplicator_tool_name
                SDMTool.Type.APPCONTROL, SDMTool.Type.ANALYZER -> 0
            }
        )
        description.apply {
            setText(
                when (item.toolType) {
                    SDMTool.Type.CORPSEFINDER -> string.corpsefinder_explanation_short
                    SDMTool.Type.SYSTEMCLEANER -> string.systemcleaner_explanation_short
                    SDMTool.Type.APPCLEANER -> string.appcleaner_explanation_short
                    SDMTool.Type.DEDUPLICATOR -> string.deduplicator_explanation_short
                    SDMTool.Type.APPCONTROL, SDMTool.Type.ANALYZER -> 0
                }
            )
            isGone = item.progress != null || item.result != null
        }

        toolLoadingIndicator.isGone = !item.isInitializing

        activityContainer.isGone = item.progress == null && item.result == null
        progressBar.isInvisible = item.progress == null
        statusPrimary.isInvisible = item.progress != null
        statusSecondary.isInvisible = item.progress != null

        if (item.progress != null) {
            progressBar.setProgress(item.progress)
        } else if (item.result != null) {
            statusPrimary.text = item.result.primaryInfo.get(context)
            statusSecondary.text = item.result.secondaryInfo?.get(context)
        } else {
            statusPrimary.text = null
            statusSecondary.text = null
        }

        detailsAction.apply {
            isGone = item.progress != null || item.onDelete == null
            setOnClickListener { item.onViewDetails() }
        }

        scanAction.apply {
            if (item.onDelete == null) {
                text = getString(string.general_scan_action)
                iconPadding = context.dpToPx(4f)
            } else {
                text = null
                iconPadding = 0
            }
            isGone = item.progress != null
            setOnClickListener { item.onScan() }
            isEnabled = !item.isInitializing
        }
        deleteAction.apply {
            isGone = item.progress != null || item.onDelete == null
            setOnClickListener { item.onDelete?.invoke() }
            if (item.showProRequirement) {
                setIconResource(drawable.ic_baseline_stars_24)
            } else if (item.onDelete != null) {
                setIconResource(drawable.ic_delete)
            } else {
                icon = null
            }
            isEnabled = !item.isInitializing
        }
        cancelAction.apply {
            isGone = item.progress == null
            setOnClickListener { item.onCancel() }
        }

        itemView.apply {
            setOnClickListener { item.onViewTool() }
            isClickable = item.progress == null && item.onDelete != null
        }
    }

    data class Item(
        val toolType: SDMTool.Type,
        val isInitializing: Boolean,
        val result: SDMTool.Task.Result?,
        val progress: Progress.Data?,
        val showProRequirement: Boolean,
        val onScan: () -> Unit,
        val onDelete: (() -> Unit)?,
        val onViewTool: () -> Unit,
        val onViewDetails: () -> Unit,
        val onCancel: () -> Unit,
    ) : DashboardAdapter.Item, MainActionItem {
        override val stableId: Long = toolType.hashCode().toLong()
    }
}