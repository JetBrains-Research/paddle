package io.paddle.idea.utils

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder

fun <T> UserDataHolder.getOrPut(key: Key<T>, factory: () -> T): T {
    return getUserData(key) ?: run {
        val value = factory()
        putUserData(key, value)
        value
    }
}
