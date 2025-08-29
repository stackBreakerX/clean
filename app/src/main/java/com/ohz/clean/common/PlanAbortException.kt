package com.ohz.clean.common

open class PlanAbortException(
    message: String,
    val treatAsSuccess: Boolean = false,
) : AutomationException(message)
