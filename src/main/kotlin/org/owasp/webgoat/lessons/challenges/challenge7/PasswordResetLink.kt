/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.challenges.challenge7

import java.util.Random
import kotlin.system.exitProcess

/**
 * WARNING: DO NOT CHANGE FILE WITHOUT CHANGING .git contents
 */
class PasswordResetLink {
    fun createPasswordReset(
        username: String,
        key: String,
    ): String {
        val random = Random()
        if (username.equals("admin", ignoreCase = true)) {
            // Admin has a fix reset link
            random.setSeed(key.length.toLong())
        }
        return scramble(random, scramble(random, scramble(random, MD5.getHashString(username))))
    }

    companion object {
        @JvmStatic
        fun scramble(
            random: Random,
            inputString: String,
        ): String {
            val a = inputString.toCharArray()
            for (i in a.indices) {
                val j = random.nextInt(a.size)
                val temp = a[i]
                a[i] = a[j]
                a[j] = temp
            }
            return String(a)
        }

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size != 2) {
                println("Need a username and key")
                exitProcess(1)
            }
            val username = args[0]
            val key = args[1]
            println("Generation password reset link for $username")
            println("Created password reset link: ${PasswordResetLink().createPasswordReset(username, key)}")
        }
    }
}
