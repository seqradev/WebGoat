/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OpenRedirectTask1Test {
    private val task = OpenRedirectTask1()

    @Test
    fun externalAbsoluteUrlMarksAssignmentSolved() {
        val result = task.simulate("https://evil.example")

        assertThat(result.assignmentSolved()).isTrue()
        assertThat(result.output).contains("Would redirect to: https://evil.example")
    }

    @Test
    fun internalHostIsRejected() {
        val result = task.simulate("https://webgoat.local/dashboard")

        assertThat(result.assignmentSolved()).isFalse()
        assertThat(result.output).contains("Internal host")
    }

    @Test
    fun nonAbsoluteUrlFailsWithHelpfulMessage() {
        val result = task.simulate("/relative/path")

        assertThat(result.assignmentSolved()).isFalse()
        assertThat(result.output).contains("Needs absolute URL with http/https")
    }
}
