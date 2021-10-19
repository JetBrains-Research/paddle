package io.paddle.plugin.python.dependencies.index.wordlist

import io.paddle.plugin.python.dependencies.index.letters
import io.paddle.plugin.python.dependencies.index.withIndexAt


/**
 * Alphabet compression of words.
 *
 * Will replace each char with one byte. Work best for UTF-8+
 * encodings. For ASCII is pointless.
 */
class AlphabetCompression(words: Set<String>) {
    companion object {
        private const val UNKNOWN: Byte = 0

        //Skip all bytes that are occupied as special characters
        private const val SKIP = 1
        private const val MAX_LETTERS = 256 - SKIP

        /**
         * Is alphabet compression recommended for this set.
         *
         * Basically, it will check if the set is large enough/
         */
        fun isRecommended(words: Set<String>): Boolean = words.size > 2_000

        /**
         * Is alphabet compression can be performed for this set.
         *
         * Basically, it will check how much there in set of distinct
         * letters.
         */
        fun isApplicable(words: Set<String>): Boolean = words.letters().toSet().size <= MAX_LETTERS
    }

    private val forward: Map<Char, Byte>
    private val backward: Map<Byte, Char>

    init {
        val indexedLetters = words.letters().withIndexAt(start = SKIP)
        forward = indexedLetters.associate { it.value to it.index.toByte() }
        backward = indexedLetters.associate { it.index.toByte() to it.value }
    }

    /**
     * Check if this alphabet intersects by any letter
     * with letters of word.
     */
    fun isAlien(word: String) = word.all { it !in forward }

    /**
     * Try to compress word. If any unknown letter
     * encountered -- return null
     */
    fun compressOrNull(word: String): ByteArray? {
        val array = ByteArray(word.length)
        var index = 0
        for (letter in word) {
            array[index++] = forward[letter] ?: return null
        }
        return array
    }

    /**
     * Try to compress word. If any unknown letter
     * encountered -- replace it with special byte.
     */
    fun compressOrUnknown(word: String): ByteArray {
        val array = ByteArray(word.length)
        var index = 0
        for (letter in word) {
            array[index++] = forward[letter] ?: UNKNOWN
        }
        return array
    }

    /**
     * Decompress word. Array expected to be compressed
     * by this [AlphabetCompression] instance.
     */
    fun decompress(array: ByteArray, from: Int, to: Int, builder: java.lang.StringBuilder) {
        for (i in from until to) {
            val byte = array[i]
            builder.append(backward[byte] ?: error("Unknown byte $byte found during decompression in AlphabetCompression."))
        }
    }
}
