/*
 * SPDX-FileCopyrightText: Copyright © 2021 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.spoofcookie.encoders

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.security.crypto.codec.Hex
import java.nio.charset.StandardCharsets
import java.util.Base64

// PoC: weak encoding method

object EncDec {
    private val SALT = RandomStringUtils.randomAlphabetic(10)

    @JvmStatic
    fun encode(value: String?): String? {
        if (value == null) {
            return null
        }

        var encoded = value.lowercase() + SALT
        encoded = revert(encoded)
        encoded = hexEncode(encoded)
        return base64Encode(encoded)
    }

    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun decode(encodedValue: String?): String? {
        if (encodedValue == null) {
            return null
        }

        var decoded = base64Decode(encodedValue)
        decoded = hexDecode(decoded)
        decoded = revert(decoded)
        return decoded.substring(0, decoded.length - SALT.length)
    }

    private fun revert(value: String): String = value.reversed()

    private fun hexEncode(value: String): String {
        val encoded = Hex.encode(value.toByteArray(StandardCharsets.UTF_8))
        return String(encoded)
    }

    private fun hexDecode(value: String): String {
        val decoded = Hex.decode(value)
        return String(decoded)
    }

    private fun base64Encode(value: String): String = Base64.getEncoder().encodeToString(value.toByteArray())

    private fun base64Decode(value: String): String {
        val decoded = Base64.getDecoder().decode(value.toByteArray())
        return String(decoded)
    }
}
