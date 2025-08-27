package com.ohz.clean.common

import eu.darken.sdmse.common.pkgs.features.InstallId

class ClearCacheTask(
    val targets: List<InstallId>,
    val returnToApp: Boolean,
    val onSuccess: (InstallId) -> Unit,
    val onError: (InstallId, Exception) -> Unit,
) : AutomationTask {

    data class Result(
        val successful: Collection<InstallId>,
        val failed: Map<InstallId, Exception>,
    ) : AutomationTask.Result
}