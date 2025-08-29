package com.ohz.clean.common


sealed interface CorpseFinderTask : SDMTool.Task {
    override val type: SDMTool.Type get() = SDMTool.Type.CORPSEFINDER

    sealed interface Result : SDMTool.Task.Result {
        override val type: SDMTool.Type get() = SDMTool.Type.CORPSEFINDER
    }
}

