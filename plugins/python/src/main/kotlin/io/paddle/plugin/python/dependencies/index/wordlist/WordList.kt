package io.paddle.plugin.python.dependencies.index.wordlist

import gnu.trove.THashSet
import io.paddle.plugin.python.dependencies.index.letters

interface WordList {
    val all: Sequence<String>

    operator fun contains(word: String): Boolean

    fun prefix(prefix: String): Sequence<String>

    fun isAlien(word: String): Boolean

    open class Default(words: Set<String>) : WordList {

        private val words: THashSet<String> = THashSet(words)

        private val letters = THashSet(words.letters())

        override fun prefix(prefix: String): Sequence<String> {
            return words.asSequence().filter { it.startsWith(prefix) }
        }

        override val all: Sequence<String>
            get() = words.asSequence()

        override fun contains(word: String): Boolean {
            return word in words
        }

        override fun isAlien(word: String): Boolean {
            return word.all { it !in letters }
        }
    }
}
