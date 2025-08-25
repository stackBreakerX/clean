package com.ohz.clean.ui.base

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import eu.darken.sdmse.common.debug.logging.log
import eu.darken.sdmse.common.debug.logging.logTag
import kotlin.jvm.javaClass

abstract class ViewModel1 : ViewModel() {
    internal val _tag: String = logTag("ViewModel", javaClass.simpleName, "${this.hashCode()}")

    init {
        log(_tag) { "Initialized" }
    }

    @CallSuper
    override fun onCleared() {
        log(_tag) { "onCleared()" }
        super.onCleared()
    }
}