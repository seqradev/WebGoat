/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.challenges

data class Flag(
    val number: Int,
    val answer: String,
) {
    fun isCorrect(flag: String): Boolean = answer == flag

    override fun toString(): String = answer
}
