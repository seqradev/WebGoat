/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.missingac

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64

// intended to provide a display version of WebGoatUser for admins to view user attributes
class DisplayUser(
    user: User,
    passwordSalt: String,
) {
    val username: String = user.username
    val isAdmin: Boolean = user.isAdmin
    var userHash: String = ""
        private set

    init {
        userHash =
            runCatching { genUserHash(user.username, user.password, passwordSalt) }
                .getOrDefault("Error generating user hash")
    }

    protected fun genUserHash(
        username: String,
        password: String,
        passwordSalt: String,
    ): String {
        val md = MessageDigest.getInstance("SHA-256")
        // salting is good, but static & too predictable ... short too for a salt
        val salted = password + passwordSalt + username
        val hash = md.digest(salted.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(hash)
    }
}
