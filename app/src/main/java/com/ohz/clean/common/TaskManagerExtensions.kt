package com.ohz.clean.common

import kotlin.collections.filter
import kotlin.collections.maxByOrNull

fun TaskManager.State.getLatestTask(tool: SDMTool.Type): TaskManager.ManagedTask? {
    return tasks.filter { it.toolType == tool && it.isComplete }.maxByOrNull { it.completedAt!! }
}

fun TaskManager.State.getLatestResult(tool: SDMTool.Type): SDMTool.Task.Result? {
    return getLatestTask(tool)?.result
}
