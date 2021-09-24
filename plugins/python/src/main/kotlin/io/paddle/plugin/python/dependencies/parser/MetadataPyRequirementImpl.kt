package io.paddle.plugin.python.dependencies.parser

import com.intellij.webcore.packaging.InstalledPackage
import com.jetbrains.python.packaging.PyPackage
import com.jetbrains.python.packaging.PyRequirement
import com.jetbrains.python.packaging.requirement.PyRequirementVersionSpec
import io.paddle.plugin.python.dependencies.parser.antlr.EnvMarkersParser

data class MetadataPyRequirementImpl(
    private val name: String,
    private val versionSpecs: List<PyRequirementVersionSpec>,
    private val extras: String?,
    val markers: EnvMarkersParser.MarkerContext?
) : PyRequirement {

    override fun getName(): String = name
    override fun getExtras(): String = extras ?: ""
    override fun getVersionSpecs(): List<PyRequirementVersionSpec> = versionSpecs
    override fun getInstallOptions(): List<String> = emptyList()

    override fun match(packages: Collection<PyPackage>): PyPackage? {
        val normalizedName = name.replace('_', '-')
        return packages.firstOrNull { pkg: InstalledPackage ->
            normalizedName.equals(pkg.name, true) && versionSpecs.all { it.matches(pkg.version) }
        }
    }
}
