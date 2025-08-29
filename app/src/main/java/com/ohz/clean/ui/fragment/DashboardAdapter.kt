package com.ohz.clean.ui.fragment

import android.app.Activity
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import com.ohz.clean.ui.base.adapter.BindableVH
import com.ohz.clean.ui.base.adapter.differ.AsyncDiffer
import com.ohz.clean.ui.base.adapter.differ.DifferItem
import com.ohz.clean.ui.base.adapter.differ.setupDiffer
import com.ohz.clean.ui.base.adapter.modular.ModularAdapter
import com.ohz.clean.ui.base.adapter.modular.mods.DataBinderMod
import com.ohz.clean.ui.base.adapter.modular.mods.TypedVHCreatorMod
import dagger.hilt.android.scopes.ActivityScoped
import eu.darken.sdmse.common.lists.differ.HasAsyncDiffer
import javax.inject.Inject

@ActivityScoped
class DashboardAdapter @Inject constructor(
    private val activity: Activity,
) :
    ModularAdapter<DashboardAdapter.BaseVH<DashboardAdapter.Item, ViewBinding>>(),
    HasAsyncDiffer<DashboardAdapter.Item> {

    override val asyncDiffer: AsyncDiffer<*, Item> = setupDiffer()

    override fun getItemCount(): Int = data.size

    init {
        addMod(DataBinderMod(data))
//        addMod(TypedVHCreatorMod({ data[it] is TitleCardVH.Item }) { TitleCardVH(it) })
//        addMod(TypedVHCreatorMod({ data[it] is DebugCardVH.Item }) { DebugCardVH(it) })
        addMod(TypedVHCreatorMod({ data[it] is SetupCardVH.Item }) { SetupCardVH(it) })
//        addMod(TypedVHCreatorMod({ data[it] is UpgradeCardVH.Item }) { UpgradeCardVH(it) })
//        addMod(TypedVHCreatorMod({ data[it] is UpdateCardVH.Item }) { UpdateCardVH(it) })
        addMod(TypedVHCreatorMod({ data[it] is DashboardToolCard.Item }) { DashboardToolCard(it) })
    }

    abstract class BaseVH<D : Item, B : ViewBinding>(
        @LayoutRes layoutId: Int,
        parent: ViewGroup
    ) : VH(layoutId, parent), BindableVH<D, B>

    interface Item : DifferItem {
        override val payloadProvider: ((DifferItem, DifferItem) -> DifferItem?)
            get() = { old, new ->
                if (new::class.isInstance(old)) new else null
            }
    }
}