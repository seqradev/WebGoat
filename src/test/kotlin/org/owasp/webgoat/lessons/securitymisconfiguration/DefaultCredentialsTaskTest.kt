/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.securitymisconfiguration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultCredentialsTaskTest {
    private lateinit var task: DefaultCredentialsTask

    @BeforeEach
    fun setUp() {
        task = DefaultCredentialsTask()
    }

    @Test
    fun shouldFailWhenUsernameOrPasswordMissing() {
        val result = task.login("", "admin")

        assertThat(result.assignmentSolved()).isFalse()
        assertThat(result.feedback).isEqualTo("securitymisconfiguration.task1.failure.blank")
    }

    @Test
    fun shouldFailWithWrongCredentials() {
        val result = task.login("admin", "wrong")

        assertThat(result.assignmentSolved()).isFalse()
        assertThat(result.feedback).isEqualTo("securitymisconfiguration.task1.failure.invalid")
    }

    @Test
    fun shouldSucceedWithDefaultCredentials() {
        val result = task.login("admin", "admin")

        assertThat(result.assignmentSolved()).isTrue()
        assertThat(result.feedback).isEqualTo("securitymisconfiguration.task1.success")
    }
}
