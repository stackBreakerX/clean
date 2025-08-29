package com.ohz.clean.common

import android.content.Intent
import androidx.core.net.toUri
import eu.darken.sdmse.common.ca.toCaString
import eu.darken.sdmse.common.error.HasLocalizedError
import eu.darken.sdmse.common.error.LocalizedError
import kotlinx.coroutines.TimeoutCancellationException
import kotlin.apply
import com.ohz.clean.R

open class AutomationTimeoutException(
    cause: TimeoutCancellationException,
) : AutomationException(
    "SD Maid couldn't complete the necessary steps within the timelimit. This could mean that I need to adjust the app for your device. Consider reaching out to me so I can fix it.",
    cause,
), HasLocalizedError {

    override fun getLocalizedError(): LocalizedError = LocalizedError(
        throwable = this,
        label = R.string.automation_error_timeout_title.toCaString(),
        description = R.string.automation_error_timeout_body.toCaString(),
        infoActionLabel = R.string.general_error_report_bug_action.toCaString(),
    )

}