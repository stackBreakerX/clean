package com.ohz.clean.ui.fragment

import androidx.lifecycle.SavedStateHandle
import com.ohz.clean.MainDirections
import com.ohz.clean.common.AppCleaner
import com.ohz.clean.common.AppCleanerProcessingTask
import com.ohz.clean.common.AppJunk
import com.ohz.clean.common.hasData
import com.ohz.clean.ui.base.ViewModel3
import com.ohz.clean.ui.view.progress.Progress
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.sdmse.common.SingleLiveEvent
import eu.darken.sdmse.common.coroutine.DispatcherProvider
import eu.darken.sdmse.common.debug.logging.Logging.Priority.INFO
import eu.darken.sdmse.common.debug.logging.log
import eu.darken.sdmse.common.debug.logging.logTag
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import java.nio.file.Files.delete
import javax.inject.Inject
import kotlin.collections.map
import kotlin.collections.mapNotNull
import kotlin.collections.sortedByDescending
import kotlin.collections.toSet

@HiltViewModel
class AppCleanerListViewModel @Inject constructor(
    @Suppress("unused") private val handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val appCleaner: AppCleaner,
) : ViewModel3(dispatcherProvider) {

    init {
        appCleaner.state
            .map { it.data }
            .filter { !it.hasData }
            .take(1)
            .onEach { popNavStack() }
            .launchInViewModel()
    }

//    val events = SingleLiveEvent<AppCleanerListEvents>()

    val state = combine(
        appCleaner.state.map { it.data }.filterNotNull(),
        appCleaner.progress,
    ) { data, progress ->
        val items = data.junks
            .sortedByDescending { it.size }

        State(
            items = items,
            progress = progress,
        )
    }.asLiveData2()

//    fun showDetails(item: AppCleanerListAdapter.Item) = launch {
//        log(TAG, INFO) { "showDetails(${item.junk.identifier})" }
//        AppCleanerListFragmentDirections.actionAppCleanerListFragmentToAppCleanerDetailsFragment2(
//            identifier = item.junk.identifier
//        ).navigate()
//    }

//    fun delete(items: Collection<AppCleanerListAdapter.Item>, confirmed: Boolean = false) = launch {
//        log(TAG, INFO) { "delete(${items.size})" }
//        if (!upgradeRepo.isPro()) {
//            MainDirections.goToUpgradeFragment().navigate()
//            return@launch
//        }
//        if (!confirmed) {
//            events.postValue(AppCleanerListEvents.ConfirmDeletion(items))
//            return@launch
//        }
//        val task = AppCleanerProcessingTask(targetPkgs = items.map { it.junk.identifier }.toSet())
//        val result = taskManager.submit(task) as AppCleanerProcessingTask.Result
//        log(TAG) { "delete(): Result was $result" }
//        when (result) {
//            is AppCleanerProcessingTask.Success -> events.postValue(AppCleanerListEvents.TaskResult(result))
//        }
//    }

//    fun exclude(items: Collection<AppCleanerListAdapter.Item>) = launch {
//        log(TAG, INFO) { "exclude(${items.size})" }
//        val targets = items.mapNotNull {
//            when (it) {
//                is AppCleanerListRowVH.Item -> it.junk.identifier
//                else -> null
//            }
//        }.toSet()
//        appCleaner.exclude(targets)
//        events.postValue(AppCleanerListEvents.ExclusionsCreated(targets.size))
//    }

    data class State(
        val items: List<AppJunk>,
        val progress: Progress.Data?,
    )

    companion object {
        private val TAG = logTag("AppCleaner", "List", "ViewModel")
    }
}