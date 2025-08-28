package com.ohz.clean.common

import eu.darken.sdmse.common.ca.caString
import eu.darken.sdmse.common.getQuantityString2
import eu.darken.sdmse.common.pkgs.Pkg
import kotlinx.parcelize.Parcelize
import com.ohz.clean.R

@Parcelize
data class AppCleanerScanTask(
    val pkgIdFilter: Set<Pkg.Id> = emptySet(),
) : AppCleanerTask {

    sealed interface Result : AppCleanerTask.Result

    @Parcelize
    data class Success(
        private val itemCount: Int,
        private val recoverableSpace: Long,
    ) : Result {
        override val primaryInfo
            get() = caString {
                getQuantityString2(R.plurals.appcleaner_result_x_items_found, itemCount)
            }

        override val secondaryInfo
            get() = caString {
                val (formatted, quantity) = ByteFormatter.formatSize(this, recoverableSpace)
                getQuantityString2(
                    R.plurals.x_space_can_be_freed,
                    quantity,
                    formatted,
                )
            }
    }
}