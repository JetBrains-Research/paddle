package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.dependencies.packages.PyPackageVersionSpecifier
import io.paddle.plugin.python.dependencies.resolvers.PipResolver
import io.paddle.plugin.python.utils.PyPackageName
import io.paddle.project.PaddleProject
import io.paddle.project.extensions.routeAsString
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable


val PaddleProject.requirements: Requirements
    get() = checkNotNull(extensions.get(Requirements.Extension.key)) { "Could not load extension Requirements for project $routeAsString" }

class Requirements(val project: PaddleProject, val descriptors: MutableList<Descriptor>) : Hashable {

    val resolved: Collection<PyPackage> by lazy {
        val installedPackages = project.environment.venv.pyPackages
        val resolvedPackages = try {
            PipResolver.resolve(project)
        } catch (e: PipResolver.RetrySignal) {
            project.terminal.warn("Retrying resolve...")
            PipResolver.resolve(project)
        }

        // Uninstall packages which were removed from requirements of the project
        val irrelevantPackages = installedPackages.minus(resolvedPackages) // FIXME: looks like it drops Pytest because of uppercase here
        irrelevantPackages.forEach { project.environment.uninstall(it) }

        resolvedPackages
    }

    object Extension : PaddleProject.Extension<Requirements> {
        override val key: Extendable.Key<Requirements> = Extendable.Key()

        override fun create(project: PaddleProject): Requirements {
            val config = project.config.get<Map<String, List<Map<String, String>>>>("requirements") ?: emptyMap()

            val mainRequirements = config["main"] ?: emptyList()
            val devRequirements = config["dev"] ?: emptyList()

            val descriptors =
                mainRequirements.map { req ->
                    Descriptor(
                        name = checkNotNull(req["name"]) {
                            "Failed to parse ${project.buildFile.canonicalPath}: <name> must be provided for every requirement."
                        },
                        versionSpecifier = req["version"]?.let { PyPackageVersionSpecifier.fromString(it) },
                        type = Descriptor.Type.MAIN
                    )
                } + devRequirements.map { req ->
                    Descriptor(
                        name = checkNotNull(req["name"]) {
                            "Failed to parse ${project.buildFile.canonicalPath}: <name> must be provided for every requirement."
                        },
                        versionSpecifier = req["version"]?.let { PyPackageVersionSpecifier.fromString(it) },
                        type = Descriptor.Type.DEV
                    )
                }

            return Requirements(project, descriptors.toMutableList())
        }
    }

    data class Descriptor(val name: PyPackageName, val versionSpecifier: PyPackageVersionSpecifier? = null, val type: Type = Type.MAIN) : Hashable {
        enum class Type {
            MAIN, DEV
        }

        override fun hash(): String {
            val hashables = mutableListOf(name.hashable(), type.toString().hashable())
            versionSpecifier?.let { hashables.add(versionSpecifier.toString().hashable()) }
            return hashables.hashable().hash()
        }

        override fun toString(): String = versionSpecifier?.let { "$name$versionSpecifier" } ?: name
    }

    fun findByName(name: PyPackageName): Descriptor? {
        return descriptors.find { it.name == name }
    }

    override fun hash(): String {
        return descriptors.hashable().hash()
    }
}
