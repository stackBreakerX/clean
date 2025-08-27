package com.ohz.clean.common

import kotlin.text.substring

data class CrawledNode(
    val node: ACSNodeInfo,
    val level: Int
) {

    private val levelPrefix = "${INDENT.substring(0, level)}${level}"

    val infoShort: String = "$levelPrefix: $node"

    companion object {
        const val INDENT =
            "--------------------------------------------------------------------------------------------------------------------"
    }
}