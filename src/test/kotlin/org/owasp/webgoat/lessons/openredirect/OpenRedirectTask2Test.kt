/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OpenRedirectTask2Test {
    private val task = OpenRedirectTask2()

    @Test
    fun substringBypassWithExternalHostSucceeds() {
        val result = task.simulate("https://webgoat.org.attacker.com/path")

        assertThat(result.assignmentSolved()).isTrue()
        assertThat(result.output).contains("Bypassed naive filter")
    }

    @Test
    fun allowedHostKeepsAssignmentFailed() {
        val result = task.simulate("https://webgoat.org/profile")

        assertThat(result.assignmentSolved()).isFalse()
        assertThat(result.output).contains("Host still allowed")
    }

    @Test
    fun missingKeywordFailsValidation() {
        val result = task.simulate("https://attacker.example")

        assertThat(result.assignmentSolved()).isFalse()
        assertThat(result.output).contains("Must contain 'webgoat'")
    }
}
