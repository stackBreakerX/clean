package com.ohz.clean.common

import com.ohz.clean.ui.view.progress.Progress
import eu.darken.sdmse.common.debug.logging.log
import eu.darken.sdmse.common.flow.throttleLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.hashCode

abstract class CorpseFilter(
    private val tag: String,
    private val defaultProgress: Progress.Data
) : Progress.Host, Progress.Client {

    private val progressPub = MutableStateFlow<Progress.Data?>(defaultProgress)
    override val progress: Flow<Progress.Data?> = progressPub.throttleLatest(500)

    override fun updateProgress(update: (Progress.Data?) -> Progress.Data?) {
        progressPub.value = update(progressPub.value)
    }

    suspend fun scan(): Collection<Corpse> = try {
        doScan()
    } finally {
        progressPub.value = defaultProgress
        log(tag) { "Scan finished" }
    }

    override fun toString(): String = "${this::class.simpleName}(${hashCode()})"

    internal abstract suspend fun doScan(): Collection<Corpse>

    interface Factory {
        suspend fun isEnabled(): Boolean
        suspend fun create(): CorpseFilter
    }
}