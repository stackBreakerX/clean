package com.ohz.clean.common.analyzer.content

import com.ohz.clean.common.ByteFormatter
import com.ohz.clean.common.ContentGroup
import com.ohz.clean.common.R
import com.ohz.clean.common.ReportDetails
import com.ohz.clean.common.Reportable
import com.ohz.clean.common.analyzer.AnalyzerTask
import com.ohz.clean.common.storage.StorageId
import eu.darken.sdmse.common.ca.CaString
import eu.darken.sdmse.common.ca.caString
import eu.darken.sdmse.common.files.APath
import eu.darken.sdmse.common.getQuantityString2
import eu.darken.sdmse.common.pkgs.features.InstallId
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContentDeleteTask(
    val storageId: StorageId,
    val groupId: ContentGroup.Id,
    val targetPkg: InstallId? = null,
    val targets: Set<APath>,
) : AnalyzerTask, Reportable {

    @Parcelize
    data class Result(
        override val affectedSpace: Long,
        override val affectedPaths: Set<APath>,
    ) : AnalyzerTask.Result, ReportDetails.AffectedSpace, ReportDetails.AffectedPaths {

        override val primaryInfo: CaString
            get() = caString {
                val itemText = getQuantityString2(
                    R.plurals.general_delete_success_deleted_x,
                    affectedPaths.size,
                )
                val spaceText = run {
                    val (spaceFormatted, spaceQuantity) = ByteFormatter.formatSize(this, affectedSpace)
                    getQuantityString2(
                        R.plurals.general_result_x_space_freed,
                        spaceQuantity,
                        spaceFormatted,
                    )
                }
                "$itemText $spaceText"
            }
    }
}