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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import javax.inject.Inject

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



//    data class State(
//        val storage: DeviceStorage?,
//        val progress: Progress.Data?,
//    )
}