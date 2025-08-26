package com.ohz.clean.common

import java.time.Duration
import java.time.Instant
import java.util.UUID

interface Report {
    val reportId: ReportId
    val startAt: Instant
    val endAt: Instant
    val tool: SDMTool.Type
    val status: Status
    val primaryMessage: String?
    val secondaryMessage: String?
    val errorMessage: String?

    val affectedCount: Int?
    val affectedSpace: Long?
    val extra: String?

    val duration: Duration
        get() = Duration.between(startAt, endAt)

    enum class Status {
        SUCCESS,
        PARTIAL_SUCCESS,
        FAILURE
    }
}

typealias ReportId = UUID