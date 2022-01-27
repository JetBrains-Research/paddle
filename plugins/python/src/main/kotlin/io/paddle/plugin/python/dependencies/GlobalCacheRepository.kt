package io.paddle.plugin.python.dependencies

import io.paddle.plugin.python.PaddlePyConfig
import io.paddle.plugin.python.dependencies.index.PyPackage
import io.paddle.plugin.python.dependencies.index.PyPackagesRepository
import io.paddle.plugin.python.utils.deepResolve
import io.paddle.plugin.python.utils.exists
import io.paddle.project.Project
import java.io.File
import java.nio.file.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.schedule
import kotlin.io.path.deleteExisting
import kotlin.io.path.name

/**
 * A service class for managing Paddle's cache.
 *
 * @see PaddlePyConfig.cacheDir
 */
object GlobalCacheRepository {
    private const val CACHE_SYNC_PERIOD_MS: Long = 60000L
    private val cachedPackages: MutableCollection<CachedPackage> = ConcurrentHashMap.newKeySet()

    init {
        Timer("CachedPackagesSynchronizer", true).schedule(delay = 0, period = CACHE_SYNC_PERIOD_MS) {
            cachedPackages.clear()
            PaddlePyConfig.cacheDir.toFile().listFiles()?.forEach { repoDir ->
                val metadata = repoDir.resolve("metadata.txt").takeIf { it.exists() } ?: return@forEach
                val repo = PyPackagesRepository.loadCachedMetadata(metadata)
                repoDir.listFiles()?.filter { it.isDirectory }?.forEach { packageDir ->
                    packageDir.listFiles()?.forEach { versionDir ->
                        cachedPackages.add(CachedPackage(packageDir.name, versionDir.name, repo, versionDir.toPath()))
                    }
                }
                for (pkg in cachedPackages) {
                    for (dependencySpec in pkg.infoDirectory.metadata.requiresDist) {
                        val dependencyName = dependencySpec.nameReq().name().text
                        // TODO: implement dependency resolution
                        val dependency = cachedPackages.findLast { it.name == dependencyName } ?: continue
                        pkg.dependencies.register(dependency)
                    }
                }
            }
        }
    }


    private fun hasCached(pkg: PyPackage): Boolean {
        return cachedPackages.any { it.descriptor == pkg.descriptor && it.srcPath.exists() }
    }

    private fun getPathToCachedPackage(pkg: PyPackage): Path =
        PaddlePyConfig.cacheDir.deepResolve(
            pkg.repo.cacheFileName,
            pkg.name,
            pkg.version
        )

    fun findPackage(pkg: PyPackage, project: Project): CachedPackage {
        return if (!hasCached(pkg)) {
            installToCache(pkg, project)
        } else {
            cachedPackages.find { it.descriptor == pkg.descriptor }!!
        }
    }

    private fun installToCache(pkg: PyPackage, project: Project): CachedPackage {
        return TempVenvManager.getInstance(project).clearInstall(pkg).expose(
            onSuccess = { copyPackageRecursivelyFromGlobalVenv(pkg, project) },
            onFail = { error("Some conflict occurred during installation of ${pkg.name}.") }
        )
    }

    private fun copyPackageRecursivelyFromGlobalVenv(pkg: PyPackage, project: Project): CachedPackage {
        val venvManager = TempVenvManager.getInstance(project)
        val targetPathToCache = getPathToCachedPackage(pkg)

        // Save metadata info about repository: url and name
        val repoMetadataPath = targetPathToCache.parent.parent.resolve("metadata.txt")
        if (!repoMetadataPath.exists()) {
            repoMetadataPath.parent.toFile().mkdirs()
            pkg.repo.writeMetadataCache(repoMetadataPath)
        }

        copyPackageSourcesFromGlobalVenv(venvManager, pkg, targetPathToCache)
        val cachedPkg = CachedPackage(pkg.name, pkg.version, pkg.repo, srcPath = targetPathToCache)
        copyPackageDependenciesFromGlobalVenv(venvManager, cachedPkg, project)
        cachedPackages.add(cachedPkg)

        return cachedPkg
    }

    private fun copyPackageSourcesFromGlobalVenv(venvManager: TempVenvManager, pkg: PyPackage, targetPathToCache: Path) {
        val packageSources = venvManager.getFilesRelatedToPackage(pkg.descriptor)
        val sep = File.separatorChar
        packageSources.forEach {
            val commonPathBin = Paths.get(it.path.commonPrefixWith(venvManager.venv.bin.path).trim(sep))
            val commonPathPyCache = Paths.get(it.path.commonPrefixWith(venvManager.venv.pycache.path).trim(sep))
            val targetPathToFile = when {
                commonPathBin.name == "bin" -> {
                    // If common path prefix ends with "bin", copy it to "BIN" folder
                    val suffix = it.path.substringAfter(venvManager.venv.bin.path).trim(sep)
                    targetPathToCache.resolve("BIN").deepResolve(*suffix.split(sep).toTypedArray()).toFile()
                }
                commonPathPyCache.name == "__pycache__" -> {
                    // If common path prefix ends with "__pycache__", copy it to "PYCACHE" folder
                    val suffix = it.path.substringAfter(venvManager.venv.pycache.path).trim(sep)
                    targetPathToCache.resolve("PYCACHE").deepResolve(*suffix.split(sep).toTypedArray()).toFile()
                }
                else -> {
                    // Otherwise, it is general file from site-packages folder which should be copied as is
                    val suffix = it.path.substringAfter(venvManager.venv.sitePackages.path).trim(sep)
                    targetPathToCache.deepResolve(*suffix.split(sep).toTypedArray()).toFile()
                }
            }
            targetPathToFile.parentFile.mkdirs()
            it.copyRecursively(targetPathToFile, overwrite = true)
        }
    }

    private fun copyPackageDependenciesFromGlobalVenv(venvManager: TempVenvManager, cachedPkg: CachedPackage, project: Project) {
        for (dependencySpec in cachedPkg.infoDirectory.metadata.requiresDist) {
            val dependencyName = dependencySpec.nameReq().name().text
            if (!venvManager.contains(dependencyName)) {
                continue // assumption: PIP didn't install it => we don't need it
            }

            // https://packaging.python.org/en/latest/specifications/core-metadata/#requires-dist-multiple-use
            // https://www.python.org/dev/peps/pep-0508/
            // But it turns out there could be cases like that: pytest (>=4.3.0) ; extra == 'dev'
            // This case doesn't follow PEP, so there is a temporary workaround for this purpose.
            if (dependencySpec.nameReq().text.contains("extra ==")) {
                continue
            }

            val dependencyVersion = venvManager.getInstalledPackageVersionByName(dependencyName)

            // TODO: avoid dependency resolution: we need repo for each dependency later, but can we avoid resolving it each time?
            val dependencyPkg = PyPackage.resolve(dependencyName, dependencyVersion, project)

            if (hasCached(dependencyPkg)) {
                continue
            }
            val dependentPkg = copyPackageRecursivelyFromGlobalVenv(dependencyPkg, project)
            cachedPkg.dependencies.register(dependentPkg)
        }
    }

    fun createSymlinkToPackageRecursively(pkg: CachedPackage, venv: VenvDir) {
        if (!venv.sitePackages.resolve(pkg.name).exists()) {
            pkg.sources.forEach {
                when (it.name) {
                    "BIN" -> {
                        it.listFiles()?.forEach { executable ->
                            val link = venv.bin.resolve(executable.name).toPath()
                            if (link.exists()) {
                                link.deleteExisting()
                            }
                            Files.createSymbolicLink(link, executable.toPath())
                        }
                    }
                    "PYCACHE" -> {
                        it.listFiles()?.forEach { pyc ->
                            val link = venv.pycache.resolve(pyc.name).toPath()
                            if (link.exists()) {
                                link.deleteExisting()
                            }
                            Files.createSymbolicLink(link, pyc.toPath())
                        }
                    }
                    else -> {
                        Files.createSymbolicLink(venv.sitePackages.resolve(it.name).toPath(), it.toPath())
                    }
                }
            }
            for (dependentPkg in pkg.dependencies.all()) {
                createSymlinkToPackageRecursively(dependentPkg, venv)
            }
        } else {
            // TODO: check versions compatibility?
        }
    }
}
