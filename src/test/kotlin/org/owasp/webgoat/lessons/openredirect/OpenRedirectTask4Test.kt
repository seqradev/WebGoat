/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OpenRedirectTask4Test {
    private val task = OpenRedirectTask4()

    @Test
    fun doubleEncodingBypassSucceeds() {
        val result = task.doubleDecode("https://webgoat.local%2540evil.com")

        assertThat(result.assignmentSolved()).isTrue()
        assertThat(result.output).contains("Double decode reveals external host")
        assertThat(result.output).contains("2nd host: evil.com")
    }

    @Test
    fun internalHostRemainsFailedWhenSecondDecodeStaysInternal() {
        val result = task.doubleDecode("https://webgoat.local/profile")

        assertThat(result.assignmentSolved()).isFalse()
        assertThat(result.output).contains("Bypass not achieved")
    }
}
