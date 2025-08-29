package com.ohz.clean.common

sealed interface DeduplicatorTask : SDMTool.Task {
    override val type: SDMTool.Type get() = SDMTool.Type.DEDUPLICATOR

    sealed interface Result : SDMTool.Task.Result {
        override val type: SDMTool.Type get() = SDMTool.Type.DEDUPLICATOR
    }
}

