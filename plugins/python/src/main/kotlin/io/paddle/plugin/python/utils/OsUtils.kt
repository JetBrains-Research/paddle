package io.paddle.plugin.python.utils

import org.codehaus.plexus.util.Os

object OsUtils {
    val family by lazy {
        when {
            Os.isFamily(Os.FAMILY_WINDOWS) -> "win"
            Os.isFamily(Os.FAMILY_MAC) -> "mac"
            Os.isFamily(Os.FAMILY_UNIX) -> "linux"
            else -> error("Unknown OS family.")
        }
    }

    // FIXME: os.arch is architecture of current JRE, not the platform itself?
    val arch by lazy {
        val currentArch = Os.OS_ARCH
        when {
            "86" in currentArch && "64" in currentArch -> "x86_64"
            "64" in currentArch && ("arm" in currentArch || "aarch" in currentArch) -> "arm64"
            "64" in currentArch && "amd" in currentArch -> "amd64"
            "32" in currentArch -> "32"
            "86" in currentArch -> "86"
            else -> error("Unknown OS architecture.")
        }
    }
}
