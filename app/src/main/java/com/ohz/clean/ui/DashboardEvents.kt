package com.ohz.clean.ui

import android.content.Intent
import com.ohz.clean.common.AppCleanerProcessingTask
import com.ohz.clean.common.CorpseFinderDeleteTask
import com.ohz.clean.common.DeduplicatorDeleteTask
import com.ohz.clean.common.Duplicate
import com.ohz.clean.common.SDMTool
import com.ohz.clean.common.SystemCleanerProcessingTask

sealed interface DashboardEvents {

    data object TodoHint : DashboardEvents
    data object SetupDismissHint : DashboardEvents

    data class CorpseFinderDeleteConfirmation(
        val task: CorpseFinderDeleteTask,
    ) : DashboardEvents

    data class SystemCleanerDeleteConfirmation(
        val task: SystemCleanerProcessingTask,
    ) : DashboardEvents

    data class AppCleanerDeleteConfirmation(
        val task: AppCleanerProcessingTask,
    ) : DashboardEvents

    data class DeduplicatorDeleteConfirmation(
        val task: DeduplicatorDeleteTask,
        val clusters: List<Duplicate.Cluster>? = null,
    ) : DashboardEvents

    data class TaskResult(
        val result: SDMTool.Task.Result
    ) : DashboardEvents

    data class OpenIntent(val intent: Intent) : DashboardEvents
}