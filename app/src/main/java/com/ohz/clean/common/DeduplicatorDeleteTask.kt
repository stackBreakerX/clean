package com.ohz.clean.common

import android.os.Parcelable
import eu.darken.sdmse.common.ca.caString
import eu.darken.sdmse.common.files.APath
import eu.darken.sdmse.common.getQuantityString2
import kotlinx.parcelize.Parcelize
import java.util.UUID
import com.ohz.clean.R

@Parcelize
data class DeduplicatorDeleteTask(
    val mode: TargetMode = TargetMode.All(),
) : DeduplicatorTask, Reportable {

    sealed interface TargetMode : Parcelable {

        @Parcelize
        data class All(
            val id: UUID = UUID.randomUUID(),
        ) : TargetMode

        @Parcelize
        data class Clusters(
            val deleteAll: Boolean = false,
            val targets: Set<Duplicate.Cluster.Id>,
        ) : TargetMode

        @Parcelize
        data class Groups(
            val deleteAll: Boolean = false,
            val targets: Set<Duplicate.Group.Id>,
        ) : TargetMode

        @Parcelize
        data class Duplicates(
            val targets: Set<Duplicate.Id>
        ) : TargetMode
    }

    sealed interface Result : DeduplicatorTask.Result

    @Parcelize
    data class Success(
        override val affectedSpace: Long,
        override val affectedPaths: Set<APath>,
    ) : Result, ReportDetails.AffectedSpace, ReportDetails.AffectedPaths {

        override val primaryInfo
            get() = caString {
                getQuantityString2(R.plurals.deduplicator_result_x_clusters_processed, affectedCount)
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