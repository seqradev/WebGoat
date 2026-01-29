/*
 * SPDX-FileCopyrightText: Copyright © 2008 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.lessons

enum class Category(
    private val categoryName: String,
) {
    INTRODUCTION("Introduction"),
    GENERAL("General"),

    A1("(A1) Broken Access Control"),
    A2("(A2) Cryptographic Failures"),
    A3("(A3) Injection"),

    A5("(A5) Security Misconfiguration"),
    A6("(A6) Vuln & Outdated Components"),
    A7("(A7) Identity & Auth Failure"),
    A8("(A8) Software & Data Integrity"),
    A9("(A9) Security Logging Failures"),
    A10("(A10) Server-side Request Forgery"),

    CLIENT_SIDE("Client side"),

    CHALLENGE("Challenges"),
    ;

    fun getName(): String = categoryName

    override fun toString(): String = getName()
}
