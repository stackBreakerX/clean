package com.ohz.clean.common.analyzer.storage

import com.ohz.clean.common.analyzer.AnalyzerTask
import com.ohz.clean.common.storage.StorageId
import eu.darken.sdmse.common.ca.CaString
import eu.darken.sdmse.common.ca.toCaString
import eu.darken.sdmse.common.pkgs.features.InstallId
import kotlinx.parcelize.Parcelize
import com.ohz.clean.R

@Parcelize
data class AppDeepScanTask(
    val storageId: StorageId,
    val installId: InstallId,
) : AnalyzerTask {

    @Parcelize
    data class Result(
        private val success: Boolean,
    ) : AnalyzerTask.Result {
        override val primaryInfo: CaString
            get() = R.string.general_result_success_message.toCaString()
    }
}