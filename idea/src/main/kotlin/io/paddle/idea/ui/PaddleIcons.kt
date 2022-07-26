package io.paddle.idea.ui

import com.intellij.openapi.util.IconLoader

object PaddleIcons {
    @JvmField
    val Main = IconLoader.getIcon("/icons/paddle-tw-color.svg", javaClass)

    object ToolWindow {
        @JvmField
        val Light = IconLoader.getIcon("/icons/paddle-tw-light.svg", javaClass)

        @JvmField
        val Dark = IconLoader.getIcon("/icons/paddle-tw-dark.svg", javaClass)
    }
}
