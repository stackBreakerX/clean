package com.ohz.clean.ui.fragment

import com.ohz.clean.ui.base.ViewModel3
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.sdmse.common.coroutine.DispatcherProvider
import javax.inject.Inject

/**
 * @description
 * @version
 */
@HiltViewModel
class MainViewModel @Inject constructor(dispatcherProvider: DispatcherProvider) : ViewModel3(dispatcherProvider) {
}