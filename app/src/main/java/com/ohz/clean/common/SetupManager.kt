package com.ohz.clean.common

import eu.darken.sdmse.common.coroutine.AppScope
import eu.darken.sdmse.common.debug.logging.Logging.Priority.VERBOSE
import eu.darken.sdmse.common.debug.logging.log
import eu.darken.sdmse.common.debug.logging.logTag
import eu.darken.sdmse.common.flow.replayingShare
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.all
import kotlin.collections.any
import kotlin.collections.filterIsInstance
import kotlin.collections.forEach
import kotlin.collections.map
import kotlin.collections.minOfOrNull
import kotlin.collections.toList

@Singleton
class SetupManager @Inject constructor(
    @param:AppScope private val appScope: CoroutineScope,
    private val setupModules: Set<@JvmSuppressWildcards SetupModule>,
    setupHealer: SetupHealer,
) {

    val state: Flow<State> = combine(
        combine(
            setupModules.map { module ->
                module.state.onEach {
                    log(
                        TAG,
                        VERBOSE
                    ) { "Module $module -> $it" }
                }
            }
        ) { it.toList() },
        setupHealer.state,
    ) { moduleStates, healerState ->
        State(
            moduleStates = moduleStates,
            isHealerWorking = healerState.isWorking,
        )
    }
        .onEach { log(TAG) { "Setup state: $it" } }
        .replayingShare(appScope)

    suspend fun refresh() {
        log(TAG) { "refresh()" }
        setupModules.forEach { it.refresh() }
    }


    data class State(
        val moduleStates: List<SetupModule.State>,
        val isDismissed: Boolean = true,
        val isHealerWorking: Boolean,
    ) {

        val startedLoadingAt: Instant?
            get() = moduleStates.filterIsInstance<SetupModule.State.Loading>().minOfOrNull { it.startAt }

        val isDone: Boolean = !isHealerWorking && moduleStates.all { it is SetupModule.State.Current && it.isComplete }
        val isIncomplete: Boolean = moduleStates.filterIsInstance<SetupModule.State.Current>().any { !it.isComplete }
        val isLoading: Boolean = moduleStates.any { it is SetupModule.State.Loading }
        val isWorking: Boolean = isHealerWorking || isLoading
    }

    companion object {
        private val TAG = logTag("Setup", "Manager")
    }
}