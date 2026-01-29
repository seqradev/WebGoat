/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.playwright.webgoat.pages.lessons

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.assertj.core.api.Assertions
import org.owasp.webgoat.container.lessons.LessonName
import org.owasp.webgoat.playwright.webgoat.PlaywrightTest

open class LessonPage(
    val page: Page,
) {
    fun navigateTo(pageNumber: Int) {
        page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("$pageNumber")).click()
    }

    open fun open(lessonName: LessonName) {
        page.navigate(PlaywrightTest.webGoatUrl("start.mvc#lesson/${lessonName.lessonName()}"))
    }

    /**
     * Force a reload for the UI to response, this is normally done by a JavaScript reloading every 5
     * seconds
     */
    fun refreshPage() {
        page.reload()
    }

    fun resetLesson(lessonName: LessonName) {
        Assertions
            .assertThat(
                page
                    .request()
                    .get(PlaywrightTest.webGoatUrl("service/restartlesson.mvc/$lessonName"))
                    .ok(),
            ).isTrue()
        refreshPage()
    }

    fun numberOfAssignments(): Int =
        page.locator(".attack-link.solved-false").count() +
            page.locator(".attack-link.solved-true").count()

    fun isAssignmentSolved(pageNumber: Int): Boolean {
        val solvedAssignments = page.locator(".attack-link.solved-true")
        solvedAssignments.waitFor()
        return solvedAssignments.all().any { it.textContent() == "$pageNumber" }
    }

    fun noAssignmentsCompleted(): Boolean = page.locator(".attack-link.solved-true").count() == 0

    val assignmentOutput: Locator
        get() = page.locator("#lesson-content-wrapper")

    val hintsOutput: Locator
        get() = page.locator("#lesson-hint")
}
