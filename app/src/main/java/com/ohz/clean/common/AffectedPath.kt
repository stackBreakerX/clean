package com.ohz.clean.common

import androidx.annotation.DrawableRes
import eu.darken.sdmse.common.files.APath

interface AffectedPath {
    val reportId: ReportId
    val action: Action
    val path: APath

    enum class Action {
        DELETED,
        ;
    }
}

@get:DrawableRes
val AffectedPath.Action.iconRes: Int
    get() = when (this) {
        AffectedPath.Action.DELETED -> com.ohz.clean.R.drawable.ic_delete
    }