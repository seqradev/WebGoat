/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.deserialization

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.Base64

object SerializationHelper {
    private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()

    @JvmStatic
    fun fromString(s: String): Any {
        val data = Base64.getDecoder().decode(s)
        return ObjectInputStream(ByteArrayInputStream(data)).use { it.readObject() }
    }

    @JvmStatic
    fun toString(o: Serializable): String {
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { it.writeObject(o) }
        return Base64.getEncoder().encodeToString(baos.toByteArray())
    }

    @JvmStatic
    fun show(): String {
        val baos = ByteArrayOutputStream()
        DataOutputStream(baos).use { it.writeLong(-8699352886133051976L) }
        return bytesToHex(baos.toByteArray())
    }

    @JvmStatic
    fun bytesToHex(bytes: ByteArray): String =
        buildString(bytes.size * 2) {
            bytes.forEach { b ->
                val v = b.toInt() and 0xFF
                append(HEX_ARRAY[v ushr 4])
                append(HEX_ARRAY[v and 0x0F])
            }
        }
}
