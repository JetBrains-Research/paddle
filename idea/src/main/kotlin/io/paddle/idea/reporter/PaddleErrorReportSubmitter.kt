package io.paddle.idea.reporter

import com.intellij.diagnostic.ITNReporter

class PaddleErrorReportSubmitter : ITNReporter() {
    override fun getReportActionText(): String = "Report to Paddle"
}