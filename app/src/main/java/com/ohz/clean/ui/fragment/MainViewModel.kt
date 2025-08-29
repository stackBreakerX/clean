package com.ohz.clean.ui.fragment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.ohz.clean.MainDirections
import com.ohz.clean.common.AppCleaner
import com.ohz.clean.common.AppCleanerOneClickTask
import com.ohz.clean.common.AppCleanerProcessingTask
import com.ohz.clean.common.AppCleanerScanTask
import com.ohz.clean.common.AppCleanerSchedulerTask
import com.ohz.clean.common.AppCleanerTask
import com.ohz.clean.common.CorpseFinder
import com.ohz.clean.common.CorpseFinderDeleteTask
import com.ohz.clean.common.CorpseFinderOneClickTask
import com.ohz.clean.common.CorpseFinderScanTask
import com.ohz.clean.common.CorpseFinderSchedulerTask
import com.ohz.clean.common.CorpseFinderTask
import com.ohz.clean.common.DeduplicatorDeleteTask
import com.ohz.clean.common.DeduplicatorOneClickTask
import com.ohz.clean.common.DeduplicatorScanTask
import com.ohz.clean.common.DeduplicatorTask
import com.ohz.clean.common.DeviceStorage
import com.ohz.clean.common.DeviceStorageScanTask
import com.ohz.clean.common.SDMTool
import com.ohz.clean.common.SetupManager
import com.ohz.clean.common.SystemCleaner
import com.ohz.clean.common.SystemCleanerOneClickTask
import com.ohz.clean.common.SystemCleanerProcessingTask
import com.ohz.clean.common.SystemCleanerScanTask
import com.ohz.clean.common.SystemCleanerSchedulerTask
import com.ohz.clean.common.SystemCleanerTask
import com.ohz.clean.common.TaskManager
import com.ohz.clean.common.UninstallWatcherTask
import com.ohz.clean.common.analyzer.Analyzer
import com.ohz.clean.common.getLatestResult
import com.ohz.clean.common.hasData
import com.ohz.clean.ui.DashboardEvents
import com.ohz.clean.ui.base.ViewModel3
import com.ohz.clean.ui.view.progress.Progress
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.sdmse.common.SingleLiveEvent
import eu.darken.sdmse.common.coroutine.DispatcherProvider
import eu.darken.sdmse.common.debug.logging.Logging.Priority.INFO
import eu.darken.sdmse.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.sdmse.common.debug.logging.log
import eu.darken.sdmse.common.flow.intervalFlow
import eu.darken.sdmse.common.flow.replayingShare
import eu.darken.sdmse.common.flow.throttleLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * @description
 * @version
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    @Suppress("unused") private val handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val analyzer: Analyzer,
    private val setupManager: SetupManager,
    private val appCleaner: AppCleaner,
    private val corpseFinder: CorpseFinder,
    private val taskManager: TaskManager,
    private val systemCleaner: SystemCleaner,

    ) :
    ViewModel3(dispatcherProvider) {

    private val TAG = "MainViewModel"

    val events = SingleLiveEvent<DashboardEvents>()


    init {
        analyzer.data
            .take(1)
            .filter { it.storages.isEmpty() }
            .onEach { analyzer.submit(DeviceStorageScanTask()) }
            .launchInViewModel()

    }

    val state = combine(
        analyzer.data,
        analyzer.progress,
    ) { data, progress ->
        data
    }.asLiveData2()

    private val setupCardItem: Flow<SetupManager.State?> = setupManager.state
        .flatMapLatest { setupState ->
            if (setupState.isDone || setupState.isDismissed) return@flatMapLatest flowOf(null)

            if (setupState.isIncomplete) return@flatMapLatest flowOf(setupState)

            if (!setupState.isLoading) return@flatMapLatest flowOf(null)

            intervalFlow(1.seconds).map {
                val now = Instant.now()
                val loadingStart = setupState.startedLoadingAt ?: now
                if (Duration.between(loadingStart, now) >= Duration.ofSeconds(5)) {
                    setupState
                } else {
                    null
                }
            }
        }


//    data class State(
//        val storage: DeviceStorage?,
//        val progress: Progress.Data?,
//    )

    private val corpseFinderItem: Flow<DashboardToolCard.Item> = combine(
        (corpseFinder.state as Flow<CorpseFinder.State?>).onStart { emit(null) },
        taskManager.state.map { it.getLatestResult(SDMTool.Type.CORPSEFINDER) },
    ) { state, lastResult ->
        DashboardToolCard.Item(
            toolType = SDMTool.Type.CORPSEFINDER,
            isInitializing = state == null,
            result = lastResult,
            progress = state?.progress,
            showProRequirement = false,
            onScan = {
                launch { submitTask(CorpseFinderScanTask()) }
            },
            onDelete = {
                val task = CorpseFinderDeleteTask()
                events.postValue(DashboardEvents.CorpseFinderDeleteConfirmation(task))
            }.takeIf { state?.data?.hasData == true },
            onCancel = {
                launch { taskManager.cancel(SDMTool.Type.CORPSEFINDER) }
            },
            onViewTool = { showCorpseFinder() },
            onViewDetails = {
                DashboardFragmentDirections.actionDashboardFragmentToCorpseFinderDetailsFragment()
                    .navigate()
            },
        )
    }

    private val systemCleanerItem: Flow<DashboardToolCard.Item> = combine(
        (systemCleaner.state as Flow<SystemCleaner.State?>).onStart { emit(null) },
        taskManager.state.map { it.getLatestResult(SDMTool.Type.SYSTEMCLEANER) },
    ) { state, lastResult ->
        DashboardToolCard.Item(
            toolType = SDMTool.Type.SYSTEMCLEANER,
            isInitializing = state == null,
            result = lastResult,
            progress = state?.progress,
            showProRequirement = false,
            onScan = {
                launch { submitTask(SystemCleanerScanTask()) }
            },
            onDelete = {
                val task = SystemCleanerProcessingTask()
                events.postValue(DashboardEvents.SystemCleanerDeleteConfirmation(task))
            }.takeIf { state?.data?.hasData == true },
            onCancel = {
                launch { taskManager.cancel(SDMTool.Type.SYSTEMCLEANER) }
            },
            onViewTool = { showSystemCleaner() },
            onViewDetails = {
                DashboardFragmentDirections.actionDashboardFragmentToSystemCleanerDetailsFragment()
                    .navigate()
            },
        )
    }

    private val appCleanerItem: Flow<DashboardToolCard.Item> = combine(
        (appCleaner.state as Flow<AppCleaner.State?>).onStart { emit(null) },
        taskManager.state.map { it.getLatestResult(SDMTool.Type.APPCLEANER) },
    ) { state, lastResult ->
        DashboardToolCard.Item(
            toolType = SDMTool.Type.APPCLEANER,
            isInitializing = state == null,
            result = lastResult,
            progress = state?.progress,
            showProRequirement = false,
            onScan = {
                launch { submitTask(AppCleanerScanTask()) }
            },
            onDelete = {
                val task = AppCleanerProcessingTask()
                events.postValue(DashboardEvents.AppCleanerDeleteConfirmation(task))
            }.takeIf { state?.data?.hasData == true },
            onCancel = {
                launch { taskManager.cancel(SDMTool.Type.APPCLEANER) }
            },
            onViewTool = { showAppCleaner() },
            onViewDetails = {
                DashboardFragmentDirections.actionDashboardFragmentToAppCleanerDetailsFragment()
                    .navigate()
            },
        )
    }

//    private val deduplicatorItem: Flow<DashboardToolCard.Item?> = combine(
//        (deduplicator.state as Flow<Deduplicator.State?>).onStart { emit(null) },
//        taskManager.state.map { it.getLatestResult(SDMTool.Type.DEDUPLICATOR) },
//        upgradeInfo.map { it?.isPro ?: false },
//    ) { state, lastResult, isPro ->
//        DashboardToolCard.Item(
//            toolType = SDMTool.Type.DEDUPLICATOR,
//            isInitializing = state == null,
//            result = lastResult,
//            progress = state?.progress,
//            showProRequirement = !isPro,
//            onScan = {
//                launch { submitTask(DeduplicatorScanTask()) }
//            },
//            onDelete = {
//                launch {
//                    val event = DashboardEvents.DeduplicatorDeleteConfirmation(
//                        task = DeduplicatorDeleteTask(),
//                        clusters = deduplicator.state.first().data?.clusters?.sortedByDescending { it.averageSize }
//                    )
//                    events.postValue(event)
//                }
//            }.takeIf { state?.data?.hasData == true },
//            onCancel = {
//                launch { taskManager.cancel(SDMTool.Type.DEDUPLICATOR) }
//            },
//            onViewTool = { showDeduplicator() },
//            onViewDetails = {
//                DashboardFragmentDirections.actionDashboardFragmentToDeduplicatorDetailsFragment().navigate()
//            },
//        )
//    }

    private val listStateInternal: Flow<ListState> = eu.darken.sdmse.common.flow.combine(
        setupCardItem,
        corpseFinderItem,
        systemCleanerItem,
        appCleanerItem,
//        deduplicatorItem,
    ) {
        setupItem: SetupCardVH.Item?,
        corpseFinderItem: DashboardToolCard.Item?,
        systemCleanerItem: DashboardToolCard.Item?,
        appCleanerItem: DashboardToolCard.Item?,
//        deduplicatorItem: DashboardToolCard.Item?,
        _ ->
        val items = mutableListOf<DashboardAdapter.Item>()


        val anyInitializing = setOfNotNull(
            corpseFinderItem?.isInitializing,
            systemCleanerItem?.isInitializing,
            appCleanerItem?.isInitializing,
//            deduplicatorItem?.isInitializing,
        ).any { it }


        setupItem?.let { items.add(it) }

        corpseFinderItem?.let { items.add(it) }
        systemCleanerItem?.let { items.add(it) }
        appCleanerItem?.let { items.add(it) }
//        deduplicatorItem?.let { items.add(it) }


        val listState = ListState(
            items = items,
            isEasterEgg = false,
        )
        listState
    }
        .throttleLatest(500)
        .replayingShare(vmScope)

    val listState = listStateInternal.asLiveData()

    private suspend fun submitTask(task: SDMTool.Task) {
        val result = taskManager.submit(task)
        when (result) {
            is CorpseFinderTask.Result -> when (result) {
                is CorpseFinderScanTask.Success -> {}
                is UninstallWatcherTask.Success -> {}
                is CorpseFinderSchedulerTask.Success -> {}
                is CorpseFinderDeleteTask.Success -> events.postValue(
                    DashboardEvents.TaskResult(
                        result
                    )
                )

                is CorpseFinderOneClickTask.Success -> events.postValue(
                    DashboardEvents.TaskResult(
                        result
                    )
                )
            }

            is SystemCleanerTask.Result -> when (result) {
                is SystemCleanerScanTask.Success -> {}
                is SystemCleanerSchedulerTask.Success -> {}
                is SystemCleanerProcessingTask.Success -> events.postValue(
                    DashboardEvents.TaskResult(
                        result
                    )
                )

                is SystemCleanerOneClickTask.Success -> events.postValue(
                    DashboardEvents.TaskResult(
                        result
                    )
                )
            }

            is AppCleanerTask.Result -> when (result) {
                is AppCleanerScanTask.Success -> {}
                is AppCleanerSchedulerTask.Success -> {}
                is AppCleanerProcessingTask.Success -> events.postValue(
                    DashboardEvents.TaskResult(
                        result
                    )
                )

                is AppCleanerOneClickTask.Success -> events.postValue(
                    DashboardEvents.TaskResult(
                        result
                    )
                )
            }

            is DeduplicatorTask.Result -> when (result) {
                is DeduplicatorScanTask.Success -> {}
                is DeduplicatorDeleteTask.Success -> events.postValue(
                    DashboardEvents.TaskResult(
                        result
                    )
                )

                is DeduplicatorOneClickTask.Success -> events.postValue(
                    DashboardEvents.TaskResult(
                        result
                    )
                )
            }
        }
    }

    fun showCorpseFinder() {
        log(TAG, INFO) { "showCorpseFinderDetails()" }
        DashboardFragmentDirections.actionDashboardFragmentToCorpseFinderListFragment().navigate()
    }

    data class ListState(
        val items: List<DashboardAdapter.Item>,
        val isEasterEgg: Boolean = false,
    )
}