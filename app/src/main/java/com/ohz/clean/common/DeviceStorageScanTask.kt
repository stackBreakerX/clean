package com.ohz.clean.common

import com.ohz.clean.common.analyzer.AnalyzerTask
import eu.darken.sdmse.common.ca.CaString
import eu.darken.sdmse.common.ca.toCaString
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeviceStorageScanTask(
    val flag: Boolean = true,
) : AnalyzerTask {

    @Parcelize
    data class Result(
        private val itemCount: Int,
    ) : AnalyzerTask.Result {
        override val primaryInfo: CaString
            get() = R.string.general_result_success_message.toCaString()
    }
}