/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.challenges.challenge7

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException

/**
 * MD5 hash generator. More information about this class is available from
 * [ostermiller.org](http://ostermiller.org/utils/MD5.html).
 *
 * This class takes as input a message of arbitrary length and produces as output a 128-bit
 * "fingerprint" or "message digest" of the input. It is conjectured that it is computationally
 * infeasible to produce two messages having the same message digest, or to produce any message
 * having a given pre-specified target message digest. The MD5 algorithm is intended for digital
 * signature applications, where a large file must be "compressed" in a secure manner before being
 * encrypted with a private (secret) key under a public-key cryptosystem such as RSA.
 *
 * For more information see RFC1321.
 *
 * @author Santeri Paavolainen http://santtu.iki.fi/md5/
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
class MD5 {
    /**
     * The current state from which the hash sum can be computed or updated.
     *
     * @since ostermillerutils 1.00.00
     */
    private var workingState = MD5State()

    /**
     * Cached copy of the final MD5 hash sum. This is created when the hash is requested and it is
     * invalidated when the hash is updated.
     *
     * @since ostermillerutils 1.00.00
     */
    private var finalState = MD5State()

    /**
     * Temporary buffer cached here for performance reasons.
     *
     * @since ostermillerutils 1.00.00
     */
    private val decodeBuffer = IntArray(16)

    init {
        reset()
    }

    /**
     * Gets this hash sum as an array of 16 bytes.
     *
     * @return Array of 16 bytes, the hash of all updated bytes.
     * @since ostermillerutils 1.00.00
     */
    fun getHash(): ByteArray {
        if (!finalState.valid) {
            finalState.copy(workingState)
            val bitCount = finalState.bitCount
            // Compute the number of left over bits
            val leftOver = ((bitCount ushr 3) and 0x3f).toInt()
            // Compute the amount of padding to add based on number of left over bits.
            val padlen = if (leftOver < 56) (56 - leftOver) else (120 - leftOver)
            // add the padding
            update(finalState, padding, 0, padlen)
            // add the length (computed before padding was added)
            update(finalState, encode(bitCount), 0, 8)
            finalState.valid = true
        }
        // make a copy of the hash before returning it.
        return encode(finalState.state, 16)
    }

    /**
     * Returns 32-character hex representation of this hash.
     *
     * @return String representation of this object's hash.
     * @since ostermillerutils 1.00.00
     */
    fun getHashString(): String = toHex(this.getHash())

    /**
     * Reset the MD5 sum to its initial state.
     *
     * @since ostermillerutils 1.00.00
     */
    fun reset() {
        workingState.reset()
        finalState.valid = false
    }

    /**
     * Returns 32-character hex representation of this hash.
     *
     * @return String representation of this object's hash.
     * @since ostermillerutils 1.00.00
     */
    override fun toString(): String = getHashString()

    /**
     * Update this hash with the given data.
     *
     * A state may be passed into this method so that we can add padding and finalize a md5 hash
     * without limiting our ability to update more data later.
     *
     * If length bytes are not available to be hashed, as many bytes as possible will be hashed.
     *
     * @param state Which state is updated.
     * @param buffer Array of bytes to be hashed.
     * @param offset Offset to buffer array.
     * @param length number of bytes to hash.
     * @since ostermillerutils 1.00.00
     */
    private fun update(
        state: MD5State,
        buffer: ByteArray,
        offset: Int,
        length: Int,
    ) {
        var len = length
        finalState.valid = false

        // if length goes beyond the end of the buffer, cut it short.
        if ((len + offset) > buffer.size) {
            len = buffer.size - offset
        }

        // compute number of bytes mod 64
        // this is what we have sitting in a buffer
        // that have not been hashed yet
        var index = ((state.bitCount ushr 3) and 0x3f).toInt()

        // add the length to the count (translate bytes to bits)
        state.bitCount += len.toLong() shl 3

        val partlen = 64 - index

        var i = 0
        if (len >= partlen) {
            System.arraycopy(buffer, offset, state.buffer, index, partlen)
            transform(state, decode(state.buffer, 64, 0))
            i = partlen
            while ((i + 63) < len) {
                transform(state, decode(buffer, 64, i))
                i += 64
            }
            index = 0
        }

        // buffer remaining input
        if (i < len) {
            val start = i
            while (i < len) {
                state.buffer[index + i - start] = buffer[i + offset]
                i++
            }
        }
    }

    /**
     * Update this hash with the given data.
     *
     * If length bytes are not available to be hashed, as many bytes as possible will be hashed.
     *
     * @param buffer Array of bytes to be hashed.
     * @param offset Offset to buffer array.
     * @param length number of bytes to hash.
     * @since ostermillerutils 1.00.00
     */
    fun update(
        buffer: ByteArray,
        offset: Int,
        length: Int,
    ) {
        update(workingState, buffer, offset, length)
    }

    /**
     * Update this hash with the given data.
     *
     * If length bytes are not available to be hashed, as many bytes as possible will be hashed.
     *
     * @param buffer Array of bytes to be hashed.
     * @param length number of bytes to hash.
     * @since ostermillerutils 1.00.00
     */
    fun update(
        buffer: ByteArray,
        length: Int,
    ) {
        update(buffer, 0, length)
    }

    /**
     * Update this hash with the given data.
     *
     * @param buffer Array of bytes to be hashed.
     * @since ostermillerutils 1.00.00
     */
    fun update(buffer: ByteArray) {
        update(buffer, 0, buffer.size)
    }

    /**
     * Updates this hash with a single byte.
     *
     * @param b byte to be hashed.
     * @since ostermillerutils 1.00.00
     */
    fun update(b: Byte) {
        val buffer = ByteArray(1)
        buffer[0] = b
        update(buffer, 1)
    }

    /**
     * Update this hash with a String. The string is converted to bytes using the current platform's
     * default character encoding.
     *
     * @param s String to be hashed.
     * @since ostermillerutils 1.00.00
     */
    fun update(s: String) {
        update(s.toByteArray())
    }

    /**
     * Update this hash with a String.
     *
     * @param s String to be hashed.
     * @param enc The name of a supported character encoding.
     * @throws UnsupportedEncodingException If the named encoding is not supported.
     * @since ostermillerutils 1.00.00
     */
    @Throws(UnsupportedEncodingException::class)
    fun update(
        s: String,
        enc: String,
    ) {
        update(s.toByteArray(charset(enc)))
    }

    private fun decode(
        buffer: ByteArray,
        len: Int,
        offset: Int,
    ): IntArray {
        var i = 0
        var j = 0
        while (j < len) {
            decodeBuffer[i] = ((buffer[j + offset].toInt() and 0xff)) or
                (((buffer[j + 1 + offset].toInt() and 0xff)) shl 8) or
                (((buffer[j + 2 + offset].toInt() and 0xff)) shl 16) or
                (((buffer[j + 3 + offset].toInt() and 0xff)) shl 24)
            i++
            j += 4
        }
        return decodeBuffer
    }

    /**
     * Contains internal state of the MD5 class. Passes MD5 test suite as defined in RFC1321.
     *
     * @since ostermillerutils 1.00.00
     */
    private inner class MD5State {
        /**
         * True if this state is valid.
         *
         * @since ostermillerutils 1.00.00
         */
        var valid = true

        /**
         * 128-byte state
         *
         * @since ostermillerutils 1.00.00
         */
        var state = IntArray(4)

        /**
         * 64-bit count of the number of bits that have been hashed.
         *
         * @since ostermillerutils 1.00.00
         */
        var bitCount: Long = 0

        /**
         * 64-byte buffer (512 bits) for storing to-be-hashed characters
         *
         * @since ostermillerutils 1.00.00
         */
        var buffer = ByteArray(64)

        init {
            reset()
        }

        /**
         * Reset to initial state.
         *
         * @since ostermillerutils 1.00.00
         */
        fun reset() {
            state[0] = 0x67452301
            state[1] = 0xefcdab89.toInt()
            state[2] = 0x98badcfe.toInt()
            state[3] = 0x10325476

            bitCount = 0
        }

        /**
         * Set this state to be exactly the same as some other.
         *
         * @param from state to copy from.
         * @since ostermillerutils 1.00.00
         */
        fun copy(from: MD5State) {
            System.arraycopy(from.buffer, 0, this.buffer, 0, this.buffer.size)
            System.arraycopy(from.state, 0, this.state, 0, this.state.size)
            this.valid = from.valid
            this.bitCount = from.bitCount
        }
    }

    companion object {
        /**
         * 64 bytes of padding that can be added if the length is not divisible by 64.
         *
         * @since ostermillerutils 1.00.00
         */
        private val padding =
            byteArrayOf(
                0x80.toByte(),
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
            )

        /**
         * Command line program that will take files as arguments and output the MD5 sum for each file.
         *
         * @param args command line arguments
         * @since ostermillerutils 1.00.00
         */
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.isEmpty()) {
                System.err.println("Please specify a file.")
            } else {
                args.forEach { filename ->
                    try {
                        println("${getHashString(File(filename))} $filename")
                    } catch (x: IOException) {
                        System.err.println(x.message)
                    }
                }
            }
        }

        /**
         * Gets the MD5 hash of the given byte array.
         *
         * @param b byte array for which an MD5 hash is desired.
         * @return Array of 16 bytes, the hash of all updated bytes.
         * @since ostermillerutils 1.00.00
         */
        @JvmStatic
        fun getHash(b: ByteArray): ByteArray = MD5().apply { update(b) }.getHash()

        /**
         * Gets the MD5 hash of the given byte array.
         *
         * @param b byte array for which an MD5 hash is desired.
         * @return 32-character hex representation the data's MD5 hash.
         * @since ostermillerutils 1.00.00
         */
        @JvmStatic
        fun getHashString(b: ByteArray): String = MD5().apply { update(b) }.getHashString()

        /**
         * Gets the MD5 hash the data on the given InputStream.
         *
         * @param input byte array for which an MD5 hash is desired.
         * @return Array of 16 bytes, the hash of all updated bytes.
         * @throws IOException if an I/O error occurs.
         * @since ostermillerutils 1.00.00
         */
        @JvmStatic
        @Throws(IOException::class)
        fun getHash(input: InputStream): ByteArray {
            val md5 = MD5()
            val buffer = ByteArray(1024)
            generateSequence { input.read(buffer).takeIf { it != -1 } }
                .forEach { bytesRead -> md5.update(buffer, bytesRead) }
            return md5.getHash()
        }

        /**
         * Gets the MD5 hash the data on the given InputStream.
         *
         * @param input byte array for which an MD5 hash is desired.
         * @return 32-character hex representation the data's MD5 hash.
         * @throws IOException if an I/O error occurs.
         * @since ostermillerutils 1.00.00
         */
        @JvmStatic
        @Throws(IOException::class)
        fun getHashString(input: InputStream): String {
            val md5 = MD5()
            val buffer = ByteArray(1024)
            generateSequence { input.read(buffer).takeIf { it != -1 } }
                .forEach { bytesRead -> md5.update(buffer, bytesRead) }
            return md5.getHashString()
        }

        /**
         * Gets the MD5 hash of the given file.
         *
         * @param f file for which an MD5 hash is desired.
         * @return Array of 16 bytes, the hash of all updated bytes.
         * @throws IOException if an I/O error occurs.
         * @since ostermillerutils 1.00.00
         */
        @JvmStatic
        @Throws(IOException::class)
        fun getHash(f: File): ByteArray = FileInputStream(f).use { getHash(it) }

        /**
         * Gets the MD5 hash of the given file.
         *
         * @param f file array for which an MD5 hash is desired.
         * @return 32-character hex representation the data's MD5 hash.
         * @throws IOException if an I/O error occurs.
         * @since ostermillerutils 1.00.00
         */
        @JvmStatic
        @Throws(IOException::class)
        fun getHashString(f: File): String = FileInputStream(f).use { getHashString(it) }

        /**
         * Gets the MD5 hash of the given String. The string is converted to bytes using the current
         * platform's default character encoding.
         *
         * @param s String for which an MD5 hash is desired.
         * @return Array of 16 bytes, the hash of all updated bytes.
         * @since ostermillerutils 1.00.00
         */
        @JvmStatic
        fun getHash(s: String): ByteArray = MD5().apply { update(s) }.getHash()

        /**
         * Gets the MD5 hash of the given String. The string is converted to bytes using the current
         * platform's default character encoding.
         *
         * @param s String for which an MD5 hash is desired.
         * @return 32-character hex representation the data's MD5 hash.
         * @since ostermillerutils 1.00.00
         */
        @JvmStatic
        fun getHashString(s: String): String = MD5().apply { update(s) }.getHashString()

        /**
         * Gets the MD5 hash of the given String.
         *
         * @param s String for which an MD5 hash is desired.
         * @param enc The name of a supported character encoding.
         * @return Array of 16 bytes, the hash of all updated bytes.
         * @throws UnsupportedEncodingException If the named encoding is not supported.
         * @since ostermillerutils 1.00.00
         */
        @JvmStatic
        @Throws(UnsupportedEncodingException::class)
        fun getHash(
            s: String,
            enc: String,
        ): ByteArray = MD5().apply { update(s, enc) }.getHash()

        /**
         * Gets the MD5 hash of the given String.
         *
         * @param s String for which an MD5 hash is desired.
         * @param enc The name of a supported character encoding.
         * @return 32-character hex representation the data's MD5 hash.
         * @throws UnsupportedEncodingException If the named encoding is not supported.
         * @since ostermillerutils 1.00.00
         */
        @JvmStatic
        @Throws(UnsupportedEncodingException::class)
        fun getHashString(
            s: String,
            enc: String,
        ): String = MD5().apply { update(s, enc) }.getHashString()

        /**
         * Turns array of bytes into string representing each byte as a two digit unsigned hex number.
         *
         * @param hash Array of bytes to convert to hex-string
         * @return Generated hex string
         * @since ostermillerutils 1.00.00
         */
        private fun toHex(hash: ByteArray): String =
            hash.joinToString("") { byte ->
                (byte.toInt() and 0xff).toString(16).padStart(2, '0')
            }

        @Suppress("ktlint:standard:function-naming")
        private fun FF(
            a: Int,
            b: Int,
            c: Int,
            d: Int,
            x: Int,
            s: Int,
            ac: Int,
        ): Int {
            var result = a
            result += ((b and c) or (b.inv() and d))
            result += x
            result += ac
            result = (result shl s) or (result ushr (32 - s))
            return result + b
        }

        @Suppress("ktlint:standard:function-naming")
        private fun GG(
            a: Int,
            b: Int,
            c: Int,
            d: Int,
            x: Int,
            s: Int,
            ac: Int,
        ): Int {
            var result = a
            result += ((b and d) or (c and d.inv()))
            result += x
            result += ac
            result = (result shl s) or (result ushr (32 - s))
            return result + b
        }

        @Suppress("ktlint:standard:function-naming")
        private fun HH(
            a: Int,
            b: Int,
            c: Int,
            d: Int,
            x: Int,
            s: Int,
            ac: Int,
        ): Int {
            var result = a
            result += (b xor c xor d)
            result += x
            result += ac
            result = (result shl s) or (result ushr (32 - s))
            return result + b
        }

        @Suppress("ktlint:standard:function-naming")
        private fun II(
            a: Int,
            b: Int,
            c: Int,
            d: Int,
            x: Int,
            s: Int,
            ac: Int,
        ): Int {
            var result = a
            result += (c xor (b or d.inv()))
            result += x
            result += ac
            result = (result shl s) or (result ushr (32 - s))
            return result + b
        }

        private fun encode(l: Long): ByteArray {
            val out = ByteArray(8)
            out[0] = (l and 0xff).toByte()
            out[1] = ((l ushr 8) and 0xff).toByte()
            out[2] = ((l ushr 16) and 0xff).toByte()
            out[3] = ((l ushr 24) and 0xff).toByte()
            out[4] = ((l ushr 32) and 0xff).toByte()
            out[5] = ((l ushr 40) and 0xff).toByte()
            out[6] = ((l ushr 48) and 0xff).toByte()
            out[7] = ((l ushr 56) and 0xff).toByte()
            return out
        }

        private fun encode(
            input: IntArray,
            len: Int,
        ): ByteArray {
            val out = ByteArray(len)
            var i = 0
            var j = 0
            while (j < len) {
                out[j] = (input[i] and 0xff).toByte()
                out[j + 1] = ((input[i] ushr 8) and 0xff).toByte()
                out[j + 2] = ((input[i] ushr 16) and 0xff).toByte()
                out[j + 3] = ((input[i] ushr 24) and 0xff).toByte()
                i++
                j += 4
            }
            return out
        }

        private fun transform(
            state: MD5.MD5State,
            x: IntArray,
        ) {
            var a = state.state[0]
            var b = state.state[1]
            var c = state.state[2]
            var d = state.state[3]

            // Round 1
            a = FF(a, b, c, d, x[0], 7, 0xd76aa478.toInt()) // 1
            d = FF(d, a, b, c, x[1], 12, 0xe8c7b756.toInt()) // 2
            c = FF(c, d, a, b, x[2], 17, 0x242070db) // 3
            b = FF(b, c, d, a, x[3], 22, 0xc1bdceee.toInt()) // 4
            a = FF(a, b, c, d, x[4], 7, 0xf57c0faf.toInt()) // 5
            d = FF(d, a, b, c, x[5], 12, 0x4787c62a) // 6
            c = FF(c, d, a, b, x[6], 17, 0xa8304613.toInt()) // 7
            b = FF(b, c, d, a, x[7], 22, 0xfd469501.toInt()) // 8
            a = FF(a, b, c, d, x[8], 7, 0x698098d8) // 9
            d = FF(d, a, b, c, x[9], 12, 0x8b44f7af.toInt()) // 10
            c = FF(c, d, a, b, x[10], 17, 0xffff5bb1.toInt()) // 11
            b = FF(b, c, d, a, x[11], 22, 0x895cd7be.toInt()) // 12
            a = FF(a, b, c, d, x[12], 7, 0x6b901122) // 13
            d = FF(d, a, b, c, x[13], 12, 0xfd987193.toInt()) // 14
            c = FF(c, d, a, b, x[14], 17, 0xa679438e.toInt()) // 15
            b = FF(b, c, d, a, x[15], 22, 0x49b40821) // 16

            // Round 2
            a = GG(a, b, c, d, x[1], 5, 0xf61e2562.toInt()) // 17
            d = GG(d, a, b, c, x[6], 9, 0xc040b340.toInt()) // 18
            c = GG(c, d, a, b, x[11], 14, 0x265e5a51) // 19
            b = GG(b, c, d, a, x[0], 20, 0xe9b6c7aa.toInt()) // 20
            a = GG(a, b, c, d, x[5], 5, 0xd62f105d.toInt()) // 21
            d = GG(d, a, b, c, x[10], 9, 0x02441453) // 22
            c = GG(c, d, a, b, x[15], 14, 0xd8a1e681.toInt()) // 23
            b = GG(b, c, d, a, x[4], 20, 0xe7d3fbc8.toInt()) // 24
            a = GG(a, b, c, d, x[9], 5, 0x21e1cde6) // 25
            d = GG(d, a, b, c, x[14], 9, 0xc33707d6.toInt()) // 26
            c = GG(c, d, a, b, x[3], 14, 0xf4d50d87.toInt()) // 27
            b = GG(b, c, d, a, x[8], 20, 0x455a14ed) // 28
            a = GG(a, b, c, d, x[13], 5, 0xa9e3e905.toInt()) // 29
            d = GG(d, a, b, c, x[2], 9, 0xfcefa3f8.toInt()) // 30
            c = GG(c, d, a, b, x[7], 14, 0x676f02d9) // 31
            b = GG(b, c, d, a, x[12], 20, 0x8d2a4c8a.toInt()) // 32

            // Round 3
            a = HH(a, b, c, d, x[5], 4, 0xfffa3942.toInt()) // 33
            d = HH(d, a, b, c, x[8], 11, 0x8771f681.toInt()) // 34
            c = HH(c, d, a, b, x[11], 16, 0x6d9d6122) // 35
            b = HH(b, c, d, a, x[14], 23, 0xfde5380c.toInt()) // 36
            a = HH(a, b, c, d, x[1], 4, 0xa4beea44.toInt()) // 37
            d = HH(d, a, b, c, x[4], 11, 0x4bdecfa9) // 38
            c = HH(c, d, a, b, x[7], 16, 0xf6bb4b60.toInt()) // 39
            b = HH(b, c, d, a, x[10], 23, 0xbebfbc70.toInt()) // 40
            a = HH(a, b, c, d, x[13], 4, 0x289b7ec6) // 41
            d = HH(d, a, b, c, x[0], 11, 0xeaa127fa.toInt()) // 42
            c = HH(c, d, a, b, x[3], 16, 0xd4ef3085.toInt()) // 43
            b = HH(b, c, d, a, x[6], 23, 0x04881d05) // 44
            a = HH(a, b, c, d, x[9], 4, 0xd9d4d039.toInt()) // 45
            d = HH(d, a, b, c, x[12], 11, 0xe6db99e5.toInt()) // 46
            c = HH(c, d, a, b, x[15], 16, 0x1fa27cf8) // 47
            b = HH(b, c, d, a, x[2], 23, 0xc4ac5665.toInt()) // 48

            // Round 4
            a = II(a, b, c, d, x[0], 6, 0xf4292244.toInt()) // 49
            d = II(d, a, b, c, x[7], 10, 0x432aff97) // 50
            c = II(c, d, a, b, x[14], 15, 0xab9423a7.toInt()) // 51
            b = II(b, c, d, a, x[5], 21, 0xfc93a039.toInt()) // 52
            a = II(a, b, c, d, x[12], 6, 0x655b59c3) // 53
            d = II(d, a, b, c, x[3], 10, 0x8f0ccc92.toInt()) // 54
            c = II(c, d, a, b, x[10], 15, 0xffeff47d.toInt()) // 55
            b = II(b, c, d, a, x[1], 21, 0x85845dd1.toInt()) // 56
            a = II(a, b, c, d, x[8], 6, 0x6fa87e4f) // 57
            d = II(d, a, b, c, x[15], 10, 0xfe2ce6e0.toInt()) // 58
            c = II(c, d, a, b, x[6], 15, 0xa3014314.toInt()) // 59
            b = II(b, c, d, a, x[13], 21, 0x4e0811a1) // 60
            a = II(a, b, c, d, x[4], 6, 0xf7537e82.toInt()) // 61
            d = II(d, a, b, c, x[11], 10, 0xbd3af235.toInt()) // 62
            c = II(c, d, a, b, x[2], 15, 0x2ad7d2bb) // 63
            b = II(b, c, d, a, x[9], 21, 0xeb86d391.toInt()) // 64

            state.state[0] += a
            state.state[1] += b
            state.state[2] += c
            state.state[3] += d
        }
    }
}
