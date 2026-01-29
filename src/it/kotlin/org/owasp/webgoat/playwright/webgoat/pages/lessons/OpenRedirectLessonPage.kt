/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.playwright.webgoat.pages.lessons

import com.microsoft.playwright.APIResponse
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class OpenRedirectLessonPage(
    page: Page,
) : LessonPage(page) {
    fun solveTask1(url: String) {
        page.locator("input[id=\"t1url\"]").fill(url)
        submitTask("/OpenRedirect/task1")
    }

    fun task1Output(): Locator = taskOutput("/OpenRedirect/task1")

    fun solveTask2(url: String) {
        page.locator("input[id=\"t2url\"]").fill(url)
        submitTask("/OpenRedirect/task2")
    }

    fun task2Output(): Locator = taskOutput("/OpenRedirect/task2")

    fun solveTask3(
        target: String,
        token: String?,
    ) {
        page.locator("#t3target").fill(target)
        token?.let { page.locator("#t3token").fill(it) }
        submitTask("/OpenRedirect/task3")
    }

    fun task3Output(): Locator = taskOutput("/OpenRedirect/task3")

    fun solveTask4(payload: String) {
        page.locator("#t4target").fill(payload)
        submitTask("/OpenRedirect/task4")
    }

    fun autofillTask4() {
        page.locator("#task4-autofill").click()
        submitTask("/OpenRedirect/task4")
    }

    fun task4Output(): Locator = taskOutput("/OpenRedirect/task4")

    fun submitMitigation(url: String) {
        page.locator("#mitigationUrl").fill(url)
        submitTask("/OpenRedirect/mitigation")
    }

    fun mitigationOutput(): Locator = taskOutput("/OpenRedirect/mitigation")

    fun invokeSafeRedirect(destId: Int): APIResponse = page.context().request().get("/OpenRedirect/safe?destId=$destId")

    fun solveQuiz() {
        page.waitForSelector("#q_container input[name='question_0_solution']")
        page.locator("#question_0_0_input").check()
        page.locator("#question_1_2_input").check()
        page.locator("#question_2_0_input").check()
        page.locator("#question_3_0_input").check()

        page.locator("#quiz-form input[type='SUBMIT']").click()
        page.waitForSelector("#q_container .quiz_question.correct:nth-of-type(4)")
    }

    private fun submitTask(actionSuffix: String) {
        page.locator("form[action\$='$actionSuffix'] button[type='submit']").click()
    }

    private fun taskOutput(actionSuffix: String): Locator =
        page.locator("form[action\$='$actionSuffix'] ~ .attack-output")

    fun quizContainer(): Locator = page.locator("#q_container")

    fun solvedAssignments(): Locator = page.locator(".attack-link.solved-true")
}
