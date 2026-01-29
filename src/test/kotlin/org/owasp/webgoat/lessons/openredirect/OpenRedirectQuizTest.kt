/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OpenRedirectQuizTest {
    private val quiz = OpenRedirectQuiz()

    @Test
    fun correctAnswersSolveQuiz() {
        val result =
            quiz.submit(
                arrayOf("Solution 0"),
                arrayOf("Solution 2"),
                arrayOf("Solution 0"),
                arrayOf("Solution 0"),
            )

        assertThat(result.assignmentSolved()).isTrue()
    }

    @Test
    fun incorrectAnswerKeepsQuizUnsolvedAndUpdatesProgress() {
        val result =
            quiz.submit(
                arrayOf("Solution 0"),
                arrayOf("Solution 1"),
                arrayOf("Solution 0"),
                arrayOf("Solution 0"),
            )

        assertThat(result.assignmentSolved()).isFalse()
        assertThat(quiz.results()).containsExactly(true, false, true, true)
    }
}
