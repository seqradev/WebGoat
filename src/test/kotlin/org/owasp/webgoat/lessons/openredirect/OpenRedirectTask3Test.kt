/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OpenRedirectTask3Test {
    private val task = OpenRedirectTask3()

    @Test
    fun userinfoBypassMarksAssignmentSolved() {
        val result = task.challenge("https://webgoat.local@evil.com", null)

        assertThat(result.assignmentSolved()).isTrue()
        assertThat(result.output).contains("RealHost: evil.com")
        assertThat(result.output).contains("Bypassed flawed normalization")
    }

    @Test
    fun obviousExternalTargetFailsChallenge() {
        val result = task.challenge("https://attacker.example", null)

        assertThat(result.assignmentSolved()).isFalse()
        assertThat(result.output).contains("AppearsInternal: false")
    }

    @Test
    fun tokenOutputIsEscaped() {
        val result = task.challenge("https://webgoat.local@evil.com", "<script>alert(1)</script>")

        assertThat(result.output).contains("Token: &lt;script&gt;alert(1)&lt;/script&gt;")
    }
}
