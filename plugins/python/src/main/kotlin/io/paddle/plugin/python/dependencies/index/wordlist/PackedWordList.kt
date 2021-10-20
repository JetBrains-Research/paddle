package io.paddle.plugin.python.dependencies.index.wordlist

import io.paddle.plugin.python.dependencies.index.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


/**
 * Word list that is compressed via alphabet compression
 *
 * Will use binary search for [contains] operation.
 * Suggestions take linear time, since it requires full scan.
 */
open class PackedWordList(words: Set<String>) {
    private val alphabet = AlphabetCompression(words)

    /** Sizes of words to indices in [words] matrix */
    private val sizeToIndex = HashMap<Int, Int>()

    /**
     * Matrix of all words.
     *
     * All words are grouped by their size, e.g. `sizeToIndex["word".length] == 3`
     * if there were words with 1 and 2 letters, but there were no words with 3 letters.
     *
     * Words of one size are stored in ByteArray one by one without any separators,
     * e.g. words == ['a', 'n', 'g', 'e', 'l', 'a', 'a', 'n', 't', 'h', 'e', 'm'] for
     * ["angela", "anthem"].
     *
     * Note, that words are compressed via alphabet encoding, so bytes are not directly mapped
     * to ASCII characters.
     *
     * Words are sorted lexicographically, so binary search can be used to find word.
     */
    private val words: Array<ByteArray>

    init {
        val compressed = words.filter { it.isNotEmpty() }.map { it to alphabet.compressOrNull(it)!! }
        val bySize = compressed
            .groupBy { it.second.size }
            .mapValues { it.value.sortedWith { t, t2 -> t.second.compare(t2.second) } }

        this.words = Array(bySize.size) { emptyByteArray() }

        for ((index, entries) in bySize.entries.withIndex()) {
            val (size, entry) = entries

            sizeToIndex[size] = index
            this.words[index] = entry.flatMap { it.second.toList() }.toByteArray()
        }
    }

    open val all: Sequence<String>
        get() = sequence {
            val builder = StringBuilder(sizeToIndex.keys.maxOrNull() ?: 1)

            for ((size, index) in sizeToIndex) {
                val arr = words[index]

                iterate(arr, size) { _, from, until ->
                    alphabet.decompress(arr, from, until, builder)
                    val variant = builder.toString()
                    builder.clear()

                    this@sequence.yield(variant)
                }
            }
        }

    open fun contains(word: String): Boolean = findIndex(word) != null

    open fun isAlien(word: String): Boolean = alphabet.isAlien(word)

    protected fun findArray(word: String): Int? {
        return alphabet.compressOrNull(word)?.size?.let { sizeToIndex[it] }
    }

    /** Find word in presorted array of compressed words */
    private fun findIndex(word: String): Int? {
        if (word.isEmpty()) return null

        //word length equals to word size since each letter is compressed to one byte
        val size = word.length
        val index = sizeToIndex[size] ?: return null
        val words = words[index]

        val original = alphabet.compressOrNull(word) ?: return null

        return binarySearch(words.size / size) { i ->
            words.compare(i * size, size, original)
        }.takeIf { it >= 0 }
    }

    //TODO-tanvd here we can use binary search instead of full scan
    open fun prefix(prefix: String): Sequence<String> {
        if (prefix.isEmpty()) return emptySequence()

        val original = alphabet.compressOrNull(prefix) ?: return emptySequence()

        val possible = sizeToIndex.filter { it.key >= original.size }.mapValues { words[it.value] }

        return sequence {
            val builder = StringBuilder(sizeToIndex.keys.maxOrNull() ?: 1)

            for ((size, arr) in possible) {
                iterate(arr, size) { _, from, until ->
                    val isPrefix = original.indices.all { original[it] == arr[from + it] }
                    if (!isPrefix) return@iterate

                    alphabet.decompress(arr, from, until, builder)
                    val variant = builder.toString()
                    builder.clear()

                    this@sequence.yield(variant)
                }
            }
        }
    }


    private inline fun binarySearch(length: Int, indexComparator: (Int) -> Int): Int {
        var low = 0
        var high = length - 1
        while (low <= high) {
            val mid = low + ((high - low) ushr 1)
            val cmp: Int = indexComparator(mid)

            when {
                cmp < 0 -> low = mid + 1
                cmp > 0 -> high = mid - 1
                else -> return mid
            }
        }
        return -(low + 1)
    }

    protected inline fun iterate(arr: ByteArray, size: Int, body: (index: Int, from: Int, until: Int) -> Unit) {
        for (i in 0 until arr.size / size) {
            body(i, i * size, i * size + size)
        }
    }

    private inline fun <T> SequenceScope<T>.iterate(arr: ByteArray, size: Int, body: SequenceScope<T>.(index: Int, from: Int, until: Int) -> Unit) {
        for (i in 0 until arr.size / size) {
            body(i, i * size, i * size + size)
        }
    }

    /**
     * Empty word lists that suggests nothing, contains
     * nothing and every word for it is alien
     */
    private class Empty : PackedWordList(words = emptySet()) {
        override val all: Sequence<String> = emptySequence()

        override fun contains(word: String): Boolean = false

        override fun prefix(prefix: String): Sequence<String> = emptySequence()

        override fun isAlien(word: String): Boolean = true
    }

    companion object {
        /** Empty word list, that always returns false on contains */
        val empty: PackedWordList = Empty()
    }
}

object PackedWordListSerializer : KSerializer<PackedWordList> {
    private val delegateSerializer = SetSerializer(String.serializer())
    override val descriptor: SerialDescriptor = WrappedSerialDescriptor(serialName = "WordList", original = delegateSerializer.descriptor)

    override fun deserialize(decoder: Decoder): PackedWordList {
        val words = decoder.decodeSerializableValue(delegateSerializer)
        return PackedWordList(words)
    }

    override fun serialize(encoder: Encoder, value: PackedWordList) {
        val words = value.all.toSet()
        encoder.encodeSerializableValue(delegateSerializer, words)
    }
}
