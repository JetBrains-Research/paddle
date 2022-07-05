package io.paddle.idea.utils

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import com.intellij.psi.PsiElement

fun <T> UserDataHolder.getOrPut(key: Key<T>, factory: () -> T): T {
    return getUserData(key) ?: run {
        val value = factory()
        putUserData(key, value)
        value
    }
}

fun PsiElement.getSuperParent(level: Int): PsiElement {
    var current = this
    repeat(level) { current = current.parent }
    return current
}
