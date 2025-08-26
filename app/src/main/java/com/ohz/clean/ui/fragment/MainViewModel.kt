package com.ohz.clean.ui.fragment

import androidx.lifecycle.SavedStateHandle
import com.ohz.clean.common.analyzer.Analyzer
import com.ohz.clean.ui.base.ViewModel3
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.sdmse.common.coroutine.DispatcherProvider
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

    ) :
    ViewModel3(dispatcherProvider) {
}