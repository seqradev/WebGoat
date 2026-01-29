/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat

data class ServerUrlConfig(
    val host: String,
    val port: String,
    private val rawContextPath: String,
) {
    val contextPath: String = rawContextPath.replace("/", "")

    val baseUrl: String
        get() = "http://$host:$port"

    fun url(path: String): String = "$fullUrl/$path"

    private val fullUrl: String
        get() = "http://$host:$port/$contextPath"

    companion object {
        @JvmStatic
        fun webGoat(): ServerUrlConfig =
            ServerUrlConfig(
                host = "localhost",
                port = env("WEBGOAT_PORT", "8080"),
                rawContextPath = env("WEBGOAT_CONTEXT", "WebGoat"),
            )

        @JvmStatic
        fun webWolf(): ServerUrlConfig =
            ServerUrlConfig(
                host = "localhost",
                port = env("WEBWOLF_PORT", "9090"),
                rawContextPath = env("WEBWOLF_CONTEXT", "WebWolf"),
            )

        private fun env(
            variableName: String,
            defaultValue: String,
        ): String = System.getenv()[variableName]?.takeIf { it.isNotEmpty() } ?: defaultValue
    }
}
