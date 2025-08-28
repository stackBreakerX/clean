package com.ohz.clean.common


sealed interface AppCleanerTask : SDMTool.Task {
    override val type: SDMTool.Type get() = SDMTool.Type.APPCLEANER

    sealed interface Result : SDMTool.Task.Result {
        override val type: SDMTool.Type get() = SDMTool.Type.APPCLEANER
    }
}

