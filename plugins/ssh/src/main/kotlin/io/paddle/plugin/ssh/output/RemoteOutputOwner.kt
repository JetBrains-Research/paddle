package io.paddle.plugin.ssh.output

import com.github.fracpete.processoutput4j.core.StreamingProcessOutputType
import com.github.fracpete.processoutput4j.core.StreamingProcessOwner
import io.paddle.terminal.Terminal

class RemoteOutputOwner(private val terminal : Terminal) : StreamingProcessOwner {
    override fun getOutputType(): StreamingProcessOutputType {
        return StreamingProcessOutputType.BOTH
    }

    override fun processOutput(line: String?, stdout: Boolean) {
        if (stdout) terminal.stdout(line!!) else terminal.stderr(line!!)
    }
}
