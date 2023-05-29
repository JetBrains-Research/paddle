
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.paddle.idea.execution.marker.PyTestTargetFinder
import io.paddle.project.PaddleProjectProvider
import junit.framework.TestCase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.io.path.Path

class PyTestTargetFinderTest : BasePlatformTestCase() {
    @BeforeEach
    override fun setUp() = super.setUp()

    @AfterEach
    override fun tearDown() = super.tearDown()

    private val testDataPath = Path("src", "test", "resources", "testRunMarkerProject")

    private val paddleProject = PaddleProjectProvider
        .getInstance(testDataPath.toFile())
        .getProject(testDataPath.toFile())
        ?: error("can't resolve project")

    override fun getTestDataPath() = "src"

    fun `test file parses corrects`() {
        val functionFile = Path("test", "resources", "testRunMarkerProject", "tests", "test1.py")
        val psiOfFile = myFixture.configureByFile(functionFile.toString())
        val matchingTest = PyTestTargetFinder.findTestTaskForElement(psiOfFile, paddleProject)
        TestCase.assertNotNull(matchingTest)
        TestCase.assertEquals("test_one_file", matchingTest!!["id"] as? String)
    }
    fun `test disabled`() = Unit
}
