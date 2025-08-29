package eu.darken.sdmse.common.lists.differ

import com.ohz.clean.ui.base.adapter.differ.AsyncDiffer
import com.ohz.clean.ui.base.adapter.differ.DifferItem
import com.ohz.clean.ui.base.adapter.DataAdapter

interface HasAsyncDiffer<T : DifferItem> : DataAdapter<T> {

    override val data: List<T>
        get() = asyncDiffer.currentList

    val asyncDiffer: AsyncDiffer<*, T>

}