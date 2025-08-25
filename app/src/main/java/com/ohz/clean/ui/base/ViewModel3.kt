package com.ohz.clean.ui.base

import androidx.navigation.NavDirections
import com.ohz.clean.common.navigation.NavEventSource
import com.ohz.clean.common.navigation.navVia
import eu.darken.sdmse.common.SingleLiveEvent
import eu.darken.sdmse.common.coroutine.DispatcherProvider
import eu.darken.sdmse.common.error.ErrorEventSource


abstract class ViewModel3(
    dispatcherProvider: DispatcherProvider,
) : ViewModel2(dispatcherProvider), NavEventSource, ErrorEventSource {

    override val navEvents = SingleLiveEvent<NavDirections?>()
    override val errorEvents = SingleLiveEvent<Throwable>()

    fun NavDirections.navigate() {
        navVia(navEvents)
    }

    fun popNavStack() {
        navEvents.postValue(null)
    }
}