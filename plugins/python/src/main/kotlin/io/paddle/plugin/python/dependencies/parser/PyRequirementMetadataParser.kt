package io.paddle.plugin.python.dependencies.parser

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.python.packaging.*
import com.jetbrains.python.packaging.requirement.PyRequirementRelation
import com.jetbrains.python.packaging.requirement.PyRequirementVersionSpec
import io.paddle.plugin.python.dependencies.parser.antlr.EnvMarkersLexer
import io.paddle.plugin.python.dependencies.parser.antlr.EnvMarkersParser
import one.util.streamex.StreamEx
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.jetbrains.annotations.ApiStatus
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * This class is copied from IntelliJ's PyRequirementParser, but with some slight modifications.
 *
 * @see com.jetbrains.python.packaging.PyRequirementParser
 */
object PyRequirementMetadataParser {
    // common regular expressions

    private val LINE_WS_REGEXP = "[ \t]"

    private val LINE_WSS_REGEXP = LINE_WS_REGEXP + "*"

    private val COMMENT_GROUP = "comment"

    private val COMMENT_REGEXP = "(?<" + COMMENT_GROUP + ">" + LINE_WS_REGEXP + "+#.*)?"

    private val NAME_GROUP = "name"

    // PEP-508
    // https://www.python.org/dev/peps/pep-0508/

    private val IDENTIFIER_REGEXP = "[A-Za-z0-9]([-_\\.]?[A-Za-z0-9])*"

    private val NAME_REGEXP = "(?<" + NAME_GROUP + ">" + IDENTIFIER_REGEXP + ")"

    private val EXTRAS_REGEXP = "\\[" + IDENTIFIER_REGEXP + "(" + LINE_WS_REGEXP + "*," + LINE_WS_REGEXP + "*" + IDENTIFIER_REGEXP + ")*" + "\\]"

    // archive-related regular expressions

    private val GITHUB_ARCHIVE_URL = Pattern.compile("https?://github\\.com/[^/\\s]+/(?<$NAME_GROUP>[^/\\s]+)/archive/\\S+$COMMENT_REGEXP")

    private val GITLAB_ARCHIVE_URL = Pattern.compile("https?://gitlab\\.com/[^/\\s]+/(?<$NAME_GROUP>[^/\\s]+)/repository/\\S+$COMMENT_REGEXP")

    private val ARCHIVE_URL = Pattern.compile(
        "https?://\\S+/" +
            "(?<$NAME_GROUP>\\S+)" +
            "(\\.tar\\.gz|\\.zip)(#(sha1|sha224|sha256|sha384|sha512|md5)=\\w+)?" + COMMENT_REGEXP
    )

    // vcs-related regular expressions
    // don't forget to update calculateVcsInstallOptions(Matcher) after this section changing

    private val VCS_EDITABLE_GROUP = "editable"

    private val VCS_EDITABLE_REGEXP = "((?<" + VCS_EDITABLE_GROUP + ">-e|--editable)" + LINE_WS_REGEXP + "+)?"

    private val VCS_SRC_BEFORE_GROUP = "srcb"

    private val VCS_SRC_AFTER_GROUP = "srca"

    private val VCS_SRC_BEFORE_REGEXP = "(?<" + VCS_SRC_BEFORE_GROUP + ">--src" + LINE_WS_REGEXP + "+\\S+" + LINE_WS_REGEXP + "+)?"

    private val VCS_SRC_AFTER_REGEXP = "(?<" + VCS_SRC_AFTER_GROUP + ">" + LINE_WS_REGEXP + "+--src" + LINE_WS_REGEXP + "+\\S+)?"

    private val PATH_IN_VCS_GROUP = "path"

    private val PATH_IN_VCS_REGEXP = "(?<" + PATH_IN_VCS_GROUP + ">[^@#\\s]+)"

    private val VCS_REVISION_REGEXP = "(@[^#\\s]+)?"

    private val VCS_EGG_BEFORE_SUBDIR_GROUP = "eggb"

    private val VCS_EGG_AFTER_SUBDIR_GROUP = "egga"

    private val VCS_EXTRAS_BEFORE_SUBDIR_GROUP = "extrasb"

    private val VCS_EXTRAS_AFTER_SUBDIR_GROUP = "extrasa"

    private val VCS_PARAMS_REGEXP = ("(" +
        "(" +
        "#egg=(?<" + VCS_EGG_BEFORE_SUBDIR_GROUP + ">[^&\\s\\[\\]]+)(?<" + VCS_EXTRAS_BEFORE_SUBDIR_GROUP + ">" + EXTRAS_REGEXP + ")?" +
        "(&subdirectory=\\S+)?" +
        ")" +
        "|" +
        "(" +
        "#subdirectory=[^&\\s]+" +
        "&egg=(?<" + VCS_EGG_AFTER_SUBDIR_GROUP + ">[^\\s\\[\\]]+)(?<" + VCS_EXTRAS_AFTER_SUBDIR_GROUP + ">" + EXTRAS_REGEXP + ")?" +
        ")" +
        ")?")

    private val VCS_GROUP = "vcs"

    private val VCS_URL_PREFIX = VCS_SRC_BEFORE_REGEXP + VCS_EDITABLE_REGEXP + "(?<" + VCS_GROUP + ">"

    private val VCS_URL_SUFFIX = PATH_IN_VCS_REGEXP + VCS_REVISION_REGEXP + VCS_PARAMS_REGEXP + ")" + VCS_SRC_AFTER_REGEXP + COMMENT_REGEXP

    private val GIT_USER_AT_REGEXP = "[\\w-]+@"

    // supports: git+user@...

    private val GIT_PROJECT_URL = Pattern.compile(VCS_URL_PREFIX + "git\\+" + GIT_USER_AT_REGEXP + "[^:\\s]+:" + VCS_URL_SUFFIX)

    // supports: bzr+lp:...

    private val BZR_PROJECT_URL = Pattern.compile(VCS_URL_PREFIX + "bzr\\+lp:" + VCS_URL_SUFFIX)

    // supports: (bzr|git|hg|svn)(+smth)?://...

    private val VCS_PROJECT_URL = Pattern.compile(VCS_URL_PREFIX + "(bzr|git|hg|svn)(\\+[A-Za-z]+)?://?[^/]+/" + VCS_URL_SUFFIX)

    // requirement-related regular expressions
    // don't forget to update calculateRequirementInstallOptions(Matcher) after this section changing

    private val REQUIREMENT_EXTRAS_GROUP = "extras"

    private val REQUIREMENT_EXTRAS_REGEXP = "(?<" + REQUIREMENT_EXTRAS_GROUP + ">" + EXTRAS_REGEXP + ")?"

    // PEP-440
    // https://www.python.org/dev/peps/pep-0440/

    private val REQUIREMENT_VERSIONS_SPECS_GROUP = "versionspecs"

    private val REQUIREMENT_VERSION_SPEC_REGEXP = "(<=?|!=|===?|>=?|~=)" + LINE_WS_REGEXP + "*[\\.\\*\\+!\\w-]+"

    private val REQUIREMENT_VERSIONS_SPECS_REGEXP =
        REQUIREMENT_VERSION_SPEC_REGEXP + "(" + LINE_WSS_REGEXP + "," + LINE_WSS_REGEXP + REQUIREMENT_VERSION_SPEC_REGEXP + ")*"

    private val REQUIREMENT_VERSIONS_SPECS_GROUP_REGEXP = "(?<" + REQUIREMENT_VERSIONS_SPECS_GROUP + ">" +
        REQUIREMENT_VERSIONS_SPECS_REGEXP + "|" + "\\(" + REQUIREMENT_VERSIONS_SPECS_REGEXP + "\\)" + ")?"

    private val REQUIREMENT_OPTIONS_GROUP = "options"

    private val REQUIREMENT_OPTIONS_REGEXP = "(?<$REQUIREMENT_OPTIONS_GROUP>($LINE_WS_REGEXP+(--global-option|--install-option)=\"[^\"]*\")+)?"

    private val REQUIREMENT_MARKERS_GROUP = "markers"

    private val markers = arrayOf(
        "os_name", "sys_platform", "platform_machine", "platform_python_implementation",
        "platform_release", "platform_system", "platform_version", "python_version", "python_full_version",
        "implementation_name", "implementation_version", "extra"
    )

    private val availableMarkersRegexp = "(" + markers.joinToString("|") + ")"

    private val REQUIREMENT_MARKER_REGEXP = availableMarkersRegexp + LINE_WS_REGEXP + "(<=?|!=|===?|>=?|~=)" + LINE_WS_REGEXP + ".+"

    private val REQUIREMENT_MARKERS_REGEXP = "(?<" + REQUIREMENT_MARKERS_GROUP + ">" + LINE_WSS_REGEXP + "\\;" + LINE_WSS_REGEXP +
        REQUIREMENT_MARKER_REGEXP + "(" + LINE_WSS_REGEXP + "(or|and)" + LINE_WSS_REGEXP + REQUIREMENT_MARKER_REGEXP + ")*" + ")?"

    private val REQUIREMENT_GROUP = "requirement"

    private val REQUIREMENT = Pattern.compile(
        "(?<" + REQUIREMENT_GROUP + ">" +
            NAME_REGEXP +
            LINE_WSS_REGEXP +
            REQUIREMENT_EXTRAS_REGEXP +
            LINE_WSS_REGEXP +
            REQUIREMENT_VERSIONS_SPECS_GROUP_REGEXP +
            REQUIREMENT_MARKERS_REGEXP +
            COMMENT_REGEXP +
            ")?"
    )

    fun fromLine(line: String): PyRequirement? {
        val githubArchiveUrl = parseGitArchiveUrl(GITHUB_ARCHIVE_URL, line)
        if (githubArchiveUrl != null) {
            return githubArchiveUrl
        }
        val gitlabArchiveUrl = parseGitArchiveUrl(GITLAB_ARCHIVE_URL, line)
        if (gitlabArchiveUrl != null) {
            return gitlabArchiveUrl
        }
        val archiveUrl = parseArchiveUrl(line)
        if (archiveUrl != null) {
            return archiveUrl
        }
        return parseVcsProjectUrl(line) ?: parseRequirement(line)
    }

    fun fromText(text: String): List<PyRequirement?> {
        return fromText(text, null, HashSet())
    }

    fun fromFile(file: VirtualFile): List<PyRequirement?> {
        return fromText(loadText(file), file, HashSet())
    }

    private fun parseGitArchiveUrl(pattern: Pattern, line: String): PyRequirement? {
        val matcher = pattern.matcher(line)
        return if (matcher.matches()) {
            PyRequirementImpl(matcher.group(NAME_GROUP), emptyList(), listOf(dropComments(line, matcher)), "")
        } else null
    }

    private fun parseArchiveUrl(line: String): PyRequirement? {
        val matcher = ARCHIVE_URL.matcher(line)
        return if (matcher.matches()) {
            createVcsOrArchiveRequirement(
                parseNameAndVersionFromVcsOrArchive(matcher.group(NAME_GROUP)), listOf(dropComments(line, matcher)),
                null
            )
        } else null
    }

    private fun parseVcsProjectUrl(line: String): PyRequirement? {
        val vcsMatcher = VCS_PROJECT_URL.matcher(line)
        if (vcsMatcher.matches()) {
            return createVcsRequirement(vcsMatcher)
        }
        val gitMatcher = GIT_PROJECT_URL.matcher(line)
        if (gitMatcher.matches()) {
            return createVcsRequirement(gitMatcher)
        }
        val bzrMatcher = BZR_PROJECT_URL.matcher(line)
        return if (bzrMatcher.matches()) {
            createVcsRequirement(bzrMatcher)
        } else null
    }

    fun parseRequirement(line: String): MetadataPyRequirementImpl? {
        val matcher = REQUIREMENT.matcher(line)
        if (matcher.matches()) {
            val name = matcher.group(NAME_GROUP)
            val extras = matcher.group(REQUIREMENT_EXTRAS_GROUP)
            val versionSpecs = parseVersionSpecs(matcher.group(REQUIREMENT_VERSIONS_SPECS_GROUP))
            val markers = parseMarkers(matcher.group(REQUIREMENT_MARKERS_GROUP))

            return MetadataPyRequirementImpl(name, versionSpecs, extras, markers)
        }
        return null
    }

    @ApiStatus.Internal
    fun fromText(text: String, containingFile: VirtualFile?, visitedFiles: MutableSet<VirtualFile?>): List<PyRequirement?> {
        if (containingFile != null) {
            visitedFiles.add(containingFile)
        }
        return StreamEx
            .of(splitByLinesAndCollapse(text))
            .flatCollection { parseLine(it, containingFile, visitedFiles) }
            .nonNull()
            .distinct()
            .toList()
    }

    private fun loadText(file: VirtualFile): String {
        val document = FileDocumentManager.getInstance().getDocument(file)
        return document?.text ?: ""
    }

    private fun dropComments(line: String, matcher: Matcher): String {
        val commentIndex = matcher.start(COMMENT_GROUP)
        return if (commentIndex == -1) {
            line
        } else line.substring(0, findFirstNotWhiteSpaceBefore(line, commentIndex) + 1)
    }

    private fun parseNameAndVersionFromVcsOrArchive(name: String): Pair<String, String?> {
        var isName = true
        val nameParts: MutableList<String> = ArrayList()
        val versionParts: MutableList<String> = ArrayList()
        for (part: String in StringUtil.split(name, "-")) {
            val partStartsWithDigit = !part.isEmpty() && Character.isDigit(part[0])
            if (partStartsWithDigit || ("dev" == part)) {
                isName = false
            }
            if (isName) {
                nameParts.add(part)
            } else {
                versionParts.add(part)
            }
        }
        return Pair.create(normalizeVcsOrArchiveNameParts(nameParts), normalizeVcsOrArchiveVersionParts(versionParts))
    }

    private fun createVcsOrArchiveRequirement(
        nameAndVersion: Pair<String, String?>,
        installOptions: List<String>,
        extras: String?
    ): PyRequirement {
        val name = nameAndVersion.getFirst()
        val version = nameAndVersion.getSecond()
            ?: if (extras == null) {
                return PyRequirementImpl(name, emptyList(), installOptions, "")
            } else {
                return PyRequirementImpl(name, emptyList(), installOptions, extras)
            }
        val versionSpecs = listOf(
            pyRequirementVersionSpec(
                PyRequirementRelation.EQ, version
            )
        )
        return if (extras == null) {
            PyRequirementImpl(name, versionSpecs, installOptions, "")
        } else {
            PyRequirementImpl(name, versionSpecs, installOptions, extras)
        }
    }

    private fun createVcsRequirement(matcher: Matcher): PyRequirement {
        val path = matcher.group(PATH_IN_VCS_GROUP)
        val egg = getEgg(matcher)
        val project = extractProject(dropTrunk(dropRevision(path)))
        val nameAndVersion = parseNameAndVersionFromVcsOrArchive(egg ?: StringUtil.trimEnd(project, ".git"))
        return createVcsOrArchiveRequirement(nameAndVersion, calculateVcsInstallOptions(matcher), getVcsExtras(matcher))
    }

    fun parseVersionSpecs(versionSpecs: String?): List<PyRequirementVersionSpec> {
        return versionSpecs?.trim(' ', '(', ')')?.split(",")
            ?.map { it.trim { char -> char <= ' ' } }
            ?.mapNotNull { parseVersionSpec(it) }
            ?.toList() ?: emptyList()
    }

    private fun parseMarkers(markers: String?): EnvMarkersParser.MarkerContext? {
        val parser = markers?.trim()?.let { EnvMarkersParser(CommonTokenStream(EnvMarkersLexer(CharStreams.fromString(it)))) }
        return parser?.quotedMarker()?.marker();
    }

    private fun calculateRequirementInstallOptions(matcher: Matcher): List<String> {
        val result: MutableList<String> = ArrayList()
        result.add(matcher.group(REQUIREMENT_GROUP))
        val requirementOptions = matcher.group(REQUIREMENT_OPTIONS_GROUP)
        requirementOptions?.split(" ")?.map { it.trim { char -> char <= ' ' } }?.filter(String::isNotEmpty)?.forEach { result.add(it) }
        return result
    }

    private fun splitByLinesAndCollapse(text: String): List<String> {
        val result: MutableList<String> = ArrayList()
        val sb = StringBuilder()
        for (line: String in StringUtil.splitByLines(text)) {
            if (line.endsWith("\\") && !line.endsWith("\\\\")) {
                sb.append(line, 0, line.length - 1)
            } else {
                if (sb.isEmpty()) {
                    result.add(line)
                } else {
                    sb.append(line)
                    result.add(sb.toString())
                    sb.setLength(0)
                }
            }
        }
        return result
    }

    private fun parseLine(
        line: String,
        containingFile: VirtualFile?,
        visitedFiles: MutableSet<VirtualFile?>
    ): List<PyRequirement?> {
        if (line.startsWith("-r")) {
            return parseRecursiveLine(line, containingFile, visitedFiles, "-r".length)
        }
        return if (line.startsWith("--requirement ")) {
            parseRecursiveLine(line, containingFile, visitedFiles, "--requirement ".length)
        } else listOf(fromLine(line))
    }

    private fun normalizeVcsOrArchiveNameParts(nameParts: List<String>): String {
        return normalizeName(StringUtil.join(nameParts, "-"))
    }

    private fun normalizeVcsOrArchiveVersionParts(versionParts: List<String>): String? {
        return if (versionParts.isEmpty()) null else normalizeVersion(StringUtil.join(versionParts, "-"))
    }

    private fun calculateVcsInstallOptions(matcher: Matcher): List<String> {
        val result: MutableList<String> = ArrayList()
        val srcBefore = matcher.group(VCS_SRC_BEFORE_GROUP)
        if (srcBefore != null) {
            result.addAll(listOf(*srcBefore.split("\\s+").toTypedArray()))
        }
        val editable = matcher.group(VCS_EDITABLE_GROUP)
        if (editable != null) {
            result.add(editable)
        }
        result.add(matcher.group(VCS_GROUP))
        val srcAfter = matcher.group(VCS_SRC_AFTER_GROUP)
        if (srcAfter != null) {
            result.addAll(listOf(*srcAfter.split("\\s+").toTypedArray()).subList(1, 3)) // skip spaces before --src and get only two values
        }
        return result
    }

    private fun getEgg(matcher: Matcher): String? {
        val beforeSubdir = matcher.group(VCS_EGG_BEFORE_SUBDIR_GROUP)
        return beforeSubdir ?: matcher.group(VCS_EGG_AFTER_SUBDIR_GROUP)
    }

    private fun extractProject(path: String): String {
        val end = if (path.endsWith("/")) path.length - 1 else path.length
        val slashIndex = path.lastIndexOf("/", end - 1)
        if (slashIndex != -1) {
            return path.substring(slashIndex + 1, end)
        }
        return if (end != path.length) {
            path.substring(0, end)
        } else path
    }

    private fun dropTrunk(path: String): String {
        val slashTrunk = "/trunk"
        if (path.endsWith(slashTrunk)) {
            return path.substring(0, path.length - slashTrunk.length)
        }
        val slashTrunkSlash = "/trunk/"
        return if (path.endsWith(slashTrunkSlash)) {
            path.substring(0, path.length - slashTrunkSlash.length)
        } else path
    }

    private fun dropRevision(path: String): String {
        val atIndex = path.lastIndexOf("@")
        return if (atIndex != -1) {
            path.substring(0, atIndex)
        } else path
    }

    private fun getVcsExtras(matcher: Matcher): String? {
        val beforeSubdir = matcher.group(VCS_EXTRAS_BEFORE_SUBDIR_GROUP)
        return beforeSubdir ?: matcher.group(VCS_EXTRAS_AFTER_SUBDIR_GROUP)
    }

    private fun parseVersionSpec(versionSpec: String): PyRequirementVersionSpec? {
        var relation: PyRequirementRelation? = null
        if (versionSpec.startsWith("===")) {
            relation = PyRequirementRelation.STR_EQ
        } else if (versionSpec.startsWith("==")) {
            relation = PyRequirementRelation.EQ
        } else if (versionSpec.startsWith("<=")) {
            relation = PyRequirementRelation.LTE
        } else if (versionSpec.startsWith(">=")) {
            relation = PyRequirementRelation.GTE
        } else if (versionSpec.startsWith("<")) {
            relation = PyRequirementRelation.LT
        } else if (versionSpec.startsWith(">")) {
            relation = PyRequirementRelation.GT
        } else if (versionSpec.startsWith("~=")) {
            relation = PyRequirementRelation.COMPATIBLE
        } else if (versionSpec.startsWith("!=")) {
            relation = PyRequirementRelation.NE
        }
        if (relation != null) {
            val versionIndex = findFirstNotWhiteSpaceAfter(versionSpec, relation.presentableText.length)
            return pyRequirementVersionSpec(relation, versionSpec.substring(versionIndex))
        }
        return null
    }

    private fun parseMarker(marker: String): PyRequirementMarker? {
        val (property, someRelation, value) = marker.trim().split(" ", "\t")
        val markerRelation = when (someRelation) {
            "in" -> PyRequirementMarkerRelation.IN
            "not in" -> PyRequirementMarkerRelation.NOT_IN
            "==" -> PyRequirementMarkerRelation.EQ
            "!=" -> PyRequirementMarkerRelation.NOT_EQ
            else -> error("Error during parsing METADATA")
        }
        return PyRequirementMarker(property, markerRelation, value)
    }

    private fun parseRecursiveLine(
        line: String,
        containingFile: VirtualFile?,
        visitedFiles: MutableSet<VirtualFile?>,
        flagLength: Int
    ): List<PyRequirement?> {
        if (containingFile == null) return emptyList<PyRequirement>()
        val pathIndex = findFirstNotWhiteSpaceAfter(line, flagLength)
        if (pathIndex == line.length) return emptyList<PyRequirement>()
        val path = FileUtil.toSystemIndependentName(line.substring(pathIndex))
        val file = findRecursiveFile(containingFile, path)
        return if (file != null && !visitedFiles.contains(file)) {
            fromText(loadText(file), file, visitedFiles)
        } else emptyList<PyRequirement>()
    }

    private fun normalizeName(s: String): String {
        return s.replace('_', '-')
    }

    private fun normalizeVersion(s: String): String {
        return s.replace('_', '-').replace("-?py[\\d.]+".toRegex(), "")
    }

    private fun findFirstNotWhiteSpaceAfter(line: String, beginIndex: Int): Int {
        for (i in beginIndex until line.length) {
            if (!StringUtil.isWhiteSpace(line[i])) {
                return i
            }
        }
        return line.length
    }

    private fun findFirstNotWhiteSpaceBefore(line: String, beginIndex: Int): Int {
        for (i in beginIndex downTo 0) {
            if (!StringUtil.isWhiteSpace(line[i])) {
                return i
            }
        }
        return -1
    }

    private fun findRecursiveFile(containingFile: VirtualFile, path: String): VirtualFile? {
        val dir = containingFile.parent ?: return null
        return dir.findFileByRelativePath(path) ?: LocalFileSystem.getInstance().findFileByPath(path)
    }
}
