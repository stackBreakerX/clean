package com.ohz.clean.common

import eu.darken.sdmse.common.ca.toCaString
import eu.darken.sdmse.common.error.HasLocalizedError
import eu.darken.sdmse.common.error.LocalizedError
import com.ohz.clean.R

open class AutomationNotRunningException(
    override val message: String = "Accessibility service isn't running"
) : AutomationUnavailableException(), HasLocalizedError {
    override fun getLocalizedError(): LocalizedError = LocalizedError(
        throwable = this,
        label = R.string.automation_error_not_running_title.toCaString(),
        description = R.string.automation_error_not_running_body.toCaString(),
    )
}