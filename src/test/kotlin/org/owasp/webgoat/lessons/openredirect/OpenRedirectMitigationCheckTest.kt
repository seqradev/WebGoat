/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OpenRedirectMitigationCheckTest {
    private val assignment = OpenRedirectMitigationCheck()

    @Test
    fun externalUrlReportsMitigationSuccess() {
        val result = assignment.check("https://attacker.example")

        assertThat(result.assignmentSolved()).isTrue()
        assertThat(result.output)
            .contains("Attempted external host: attacker.example blocked")
            .contains("safe internal path")
    }

    @Test
    fun internalHostIsRejected() {
        val result = assignment.check("https://webgoat.local/home")

        assertThat(result.assignmentSolved()).isFalse()
        assertThat(result.output).contains("This host is internal")
    }

    @Test
    fun relativeUrlIsRejected() {
        val result = assignment.check("/relative")

        assertThat(result.assignmentSolved()).isFalse()
        assertThat(result.output).contains("Provide an absolute external URL")
    }
}
