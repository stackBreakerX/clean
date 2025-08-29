package com.ohz.clean.ui.fragment

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.ohz.clean.R
import com.ohz.clean.common.SetupManager
import com.ohz.clean.databinding.DashboardSetupItemBinding
import com.ohz.clean.ui.base.adapter.binding


class SetupCardVH(parent: ViewGroup) :
    DashboardAdapter.BaseVH<SetupCardVH.Item, DashboardSetupItemBinding>(R.layout.dashboard_setup_item, parent = parent) {

    override val viewBinding = lazy { DashboardSetupItemBinding.bind(itemView) }

    override val onBindData: DashboardSetupItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = binding { item ->
        val state = item.setupState
        title.text = when {
            state.isIncomplete -> getString(R.string.setup_incomplete_card_title)
            else -> getString(R.string.setup_label)
        }
        body.text = when {
            state.isHealerWorking -> getString(R.string.setup_incomplete_card_healing_in_progress_body)
            state.isLoading -> getString(R.string.setup_checking_card_body)
            else -> getString(R.string.setup_incomplete_card_body)
        }

        dismissAction.apply {
            isVisible = !state.isWorking
            setOnClickListener { item.onDismiss() }
        }

        setupProgress.isVisible = state.isWorking

        continueSetupAction.apply {
            isVisible = !state.isHealerWorking || state.isIncomplete
            setOnClickListener { item.onContinue() }
            text = when {
                state.isIncomplete -> getString(R.string.setup_incomplete_card_continue_action)
                else -> getString(R.string.general_view_action)
            }
        }
    }

    data class Item(
        val setupState: SetupManager.State,
        val onDismiss: () -> Unit,
        val onContinue: () -> Unit
    ) : DashboardAdapter.Item {
        override val stableId: Long = this.javaClass.hashCode().toLong()
    }

}