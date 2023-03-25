import io.paddle.terminal.Terminal
import io.paddle.terminal.TextOutput
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.io.File

class SampleTestInContainer {
    @Test
    fun `echo executor test`(){
        val executor = TestContainerExecutor()
        val terminal = Terminal(TextOutput.Console)
        executor.execute("echo", emptyList(), File("not_exists"), terminal).orElseDo { fail("The test failerd with non-zero $it") }
    }
}
