package com.ohz.clean.common

import kotlinx.coroutines.CancellationException

class UserCancelledAutomationException : CancellationException("User has cancelled automation")