package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.dependencies.packages.PyPackageVersionSpecifier
import io.paddle.plugin.python.dependencies.resolvers.PipResolver
import io.paddle.plugin.python.utils.PyPackageName
import io.paddle.project.PaddleProject
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import kotlin.system.measureTimeMillis


val PaddleProject.requirements: Requirements
    get() = extensions.get(Requirements.Extension.key)!!

class Requirements(val project: PaddleProject, val descriptors: MutableList<Descriptor>) : Hashable {

    val resolved: Collection<PyPackage> by lazy {
        project.terminal.info("Resolving requirements...")
        val result: Collection<PyPackage>
        measureTimeMillis {
            result = PipResolver.resolve(project) + project.environment.venv.pyPackages
        }.also {
            project.terminal.info("Finished resolving requirements: $it ms")
        }
        result
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
