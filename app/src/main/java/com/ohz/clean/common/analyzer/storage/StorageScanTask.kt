package com.ohz.clean.common.analyzer.storage

import com.ohz.clean.R
import com.ohz.clean.common.analyzer.AnalyzerTask
import com.ohz.clean.common.storage.StorageId
import eu.darken.sdmse.common.ca.CaString
import eu.darken.sdmse.common.ca.toCaString
import kotlinx.parcelize.Parcelize

@Parcelize
data class StorageScanTask(
    val target: StorageId
) : AnalyzerTask {

    @Parcelize
    data class Result(
        private val itemCount: Int,
    ) : AnalyzerTask.Result {
        override val primaryInfo: CaString
            get() = R.string.general_result_success_message.toCaString()
    }
}