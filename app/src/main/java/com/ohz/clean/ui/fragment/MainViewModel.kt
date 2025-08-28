package com.ohz.clean.ui.fragment

import androidx.lifecycle.SavedStateHandle
import com.ohz.clean.common.DeviceStorage
import com.ohz.clean.common.DeviceStorageScanTask
import com.ohz.clean.common.SetupManager
import com.ohz.clean.common.analyzer.Analyzer
import com.ohz.clean.ui.base.ViewModel3
import com.ohz.clean.ui.view.progress.Progress
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.sdmse.common.coroutine.DispatcherProvider
import eu.darken.sdmse.common.flow.intervalFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
    ) :
    ViewModel3(dispatcherProvider) {

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
}