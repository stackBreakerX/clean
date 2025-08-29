package com.ohz.clean.common

import eu.darken.sdmse.common.ca.caString
import eu.darken.sdmse.common.files.APath
import eu.darken.sdmse.common.getQuantityString2
import kotlinx.parcelize.Parcelize
import com.ohz.clean.R

@Parcelize
data class SystemCleanerProcessingTask(
    val targetFilters: Set<FilterIdentifier>? = null,
    val targetContent: Set<APath>? = null,
) : SystemCleanerTask, Reportable {

    sealed interface Result : SystemCleanerTask.Result

    @Parcelize
    data class Success(
        override val affectedSpace: Long,
        override val affectedPaths: Set<APath>
    ) : Result, ReportDetails.AffectedSpace, ReportDetails.AffectedPaths {

        override val primaryInfo
            get() = caString {
                getQuantityString2(R.plurals.systemcleaner_result_x_items_deleted, affectedCount)
            }

        override val secondaryInfo
            get() = caString {
                val (formatted, quantity) = ByteFormatter.formatSize(this, affectedSpace)
                getQuantityString2(
                    R.plurals.general_result_x_space_freed,
                    quantity,
                    formatted,
                )
            }
    }
}