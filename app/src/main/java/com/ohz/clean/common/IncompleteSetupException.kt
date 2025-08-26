package com.ohz.clean.common

import androidx.navigation.Navigation
import eu.darken.sdmse.common.ca.caString
import eu.darken.sdmse.common.ca.toCaString
import eu.darken.sdmse.common.error.HasLocalizedError
import eu.darken.sdmse.common.error.LocalizedError
import kotlin.collections.joinToString
import kotlin.text.trimIndent
import com.ohz.clean.R

class IncompleteSetupException(private val setupTypes: Set<SetupModule.Type>) : Exception(), HasLocalizedError {

    constructor(setupType: SetupModule.Type) : this(setOf(setupType))

    override fun getLocalizedError(): LocalizedError = LocalizedError(
        throwable = this,
        label = R.string.general_error_setup_require_label.toCaString(),
        description = caString { ctx ->
            """
                ${ctx.getString(R.string.general_error_setup_require_msg)}
                
                ${setupTypes.joinToString(",") { "'${ctx.getString(it.labelRes)}'" }}
            """.trimIndent()
        },
        fixActionLabel = R.string.setup_title.toCaString(),
        fixAction = {
        }
    )
}