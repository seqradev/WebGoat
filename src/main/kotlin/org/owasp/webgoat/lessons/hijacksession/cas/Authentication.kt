/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.hijacksession.cas

import java.security.Principal

class Authentication private constructor(
    private val _name: String?,
    val credentials: Any?,
    var id: String?,
) : Principal {
    var isAuthenticated: Boolean = false
        internal set

    // getName() can return null for compatibility with original Java code
    override fun getName(): String? = _name

    override fun toString(): String =
        "Authentication(authenticated=$isAuthenticated, name=$_name, credentials=$credentials, id=$id)"

    class AuthenticationBuilder {
        private var name: String? = null
        private var credentials: Any? = null
        private var id: String? = null

        fun name(name: String?) = apply { this.name = name }

        fun credentials(credentials: Any?) = apply { this.credentials = credentials }

        fun id(id: String?) = apply { this.id = id }

        fun build(): Authentication = Authentication(name, credentials, id)

        override fun toString(): String =
            "Authentication.AuthenticationBuilder(name=$name, credentials=$credentials, id=$id)"
    }

    companion object {
        @JvmStatic
        fun builder(): AuthenticationBuilder = AuthenticationBuilder()
    }
}
