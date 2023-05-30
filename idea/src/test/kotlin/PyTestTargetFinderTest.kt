
import com.intellij.psi.util.childrenOfType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.python.psi.*
import io.paddle.idea.execution.marker.PyTestTargetFinder
import io.paddle.project.PaddleProjectProvider
import io.paddle.utils.config.PaddleAppRuntime
import junit.framework.TestCase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.test.KoinTest
import kotlin.io.path.Path

class PyTestTargetFinderTest : BasePlatformTestCase(), KoinTest {

    init {
        assert(PaddleAppRuntime.isTests)
    }

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

    private fun getTestFileAndPsi(vararg path: String) = myFixture
        .configureByFile(Path("test", "resources", "testRunMarkerProject", "tests", *path).toString())

    fun `test file`() {
        val psiOfFile = getTestFileAndPsi("test1.py")
        val matchingTest = PyTestTargetFinder.findTestTaskForElement(psiOfFile, paddleProject)
        TestCase.assertNotNull(matchingTest)
        TestCase.assertEquals("test_one_file", matchingTest!!["id"] as? String)
    }

    fun `test function in file`() {
        val psiOfFile = getTestFileAndPsi("test1.py")
        val functionElement = psiOfFile.childrenOfType<PyFunction>().first()
        val matchingTest = PyTestTargetFinder.findTestTaskForElement(functionElement, paddleProject)
        TestCase.assertNotNull(matchingTest)
        TestCase.assertEquals("test_one_file", matchingTest!!["id"] as? String)
    }

    fun `test subfolder`() {
        val psiOfFile = getTestFileAndPsi("tests_subfolder", "test2.py")
        val matchingTest = PyTestTargetFinder.findTestTaskForElement(psiOfFile, paddleProject)
        TestCase.assertNotNull(matchingTest)
        TestCase.assertEquals("test_subfolder", matchingTest!!["id"] as? String)
    }

    fun `test subsubfolder`() {
        val psiOfFile = getTestFileAndPsi("tests_subfolder", "tests_subsubfolder", "test3.py")
        val matchingTest = PyTestTargetFinder.findTestTaskForElement(psiOfFile, paddleProject)
        TestCase.assertNotNull(matchingTest)
        TestCase.assertEquals("test_subfolder", matchingTest!!["id"] as? String)
    }

    fun `test subsubfolder function`() {
        val psiOfFile = getTestFileAndPsi("tests_subfolder", "tests_subsubfolder", "test3.py")
        val matchingTest = PyTestTargetFinder.findTestTaskForElement(psiOfFile.childrenOfType<PyFunction>().first(), paddleProject)
        TestCase.assertNotNull(matchingTest)
        TestCase.assertEquals("test_subfolder", matchingTest!!["id"] as? String)
    }

    fun `test class`() {
        val psiOfFile = getTestFileAndPsi( "test_class.py")
        val clazz = psiOfFile.childrenOfType<PyClass>().first()
        val methods = clazz.childrenOfType<PyStatementList>().first().childrenOfType<PyFunction>()

        val classTest = PyTestTargetFinder.findTestTaskForElement(clazz, paddleProject)
        TestCase.assertNotNull(classTest)
        TestCase.assertEquals("test_whole_class", classTest!!["id"] as? String)

        val testOneFun = methods.find { it.name == "test_one" } ?: error("Can't find test_one function")
        val testTwoFun = methods.find { it.name == "test_two" } ?: error("Can't find test_two function")

        val testOneTest = PyTestTargetFinder.findTestTaskForElement(testOneFun, paddleProject)
        TestCase.assertNotNull(testOneTest)
        TestCase.assertEquals("tests_one_class_function", testOneTest!!["id"] as? String) // the order of test entries is important

        val testTwoTest = PyTestTargetFinder.findTestTaskForElement(testTwoFun, paddleProject)

        TestCase.assertNotNull(testTwoTest)
        TestCase.assertEquals("test_whole_class", testTwoTest!!["id"] as? String)
    }

    fun `test subdir class`() {
        val psiOfFile = getTestFileAndPsi( "another_subdir", "test_class.py")
        val clazz = psiOfFile.childrenOfType<PyClass>().first()

        val classTest = PyTestTargetFinder.findTestTaskForElement(clazz, paddleProject)
        TestCase.assertNotNull(classTest)
        TestCase.assertEquals("test_whole_class_subdir", classTest!!["id"] as? String)
    }
}
