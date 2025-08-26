package com.ohz.clean.common.analyzer

import com.ohz.clean.common.SDMTool


interface AnalyzerTask : SDMTool.Task {
    override val type: SDMTool.Type get() = SDMTool.Type.ANALYZER

    interface Result : SDMTool.Task.Result {
        override val type: SDMTool.Type get() = SDMTool.Type.ANALYZER
    }
}

