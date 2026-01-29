/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.playwright.webgoat.lessons

import com.microsoft.playwright.Browser
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.owasp.webgoat.container.lessons.LessonName
import org.owasp.webgoat.playwright.webgoat.PlaywrightTest
import org.owasp.webgoat.playwright.webgoat.helpers.Authentication
import org.owasp.webgoat.playwright.webgoat.pages.lessons.HttpBasicsLessonPage

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class HttpBasicsLessonUITest : PlaywrightTest() {
    private lateinit var lessonPage: HttpBasicsLessonPage

    @BeforeEach
    fun navigateToLesson(browser: Browser) {
        val lessonName = LessonName("HttpBasics")
        val page = Authentication.sylvester(browser)

        lessonPage = HttpBasicsLessonPage(page)
        lessonPage.resetLesson(lessonName)
        lessonPage.open(lessonName)
    }

    @Test
    @Order(1)
    fun shouldShowDefaultPage() {
        assertThat(lessonPage.getTitle()).hasText("HTTP Basics")
        Assertions.assertThat(lessonPage.noAssignmentsCompleted()).isTrue()
        Assertions.assertThat(lessonPage.numberOfAssignments()).isEqualTo(2)
    }

    @Test
    @Order(2)
    @DisplayName(
        "When the user enters their name, the server should reverse it then the assignment should be solved",
    )
    fun solvePage2() {
        lessonPage.navigateTo(2)
        lessonPage.enterYourName.fill("John Doe")
        lessonPage.goButton.click()

        assertThat(lessonPage.assignmentOutput)
            .containsText("The server has reversed your name: eoD nhoJ")
        Assertions.assertThat(lessonPage.isAssignmentSolved(2)).isTrue()
    }

    @Test
    @Order(3)
    @DisplayName("When the user enters nothing then the server should display an error message")
    fun invalidPage2() {
        lessonPage.navigateTo(2)
        lessonPage.enterYourName.fill("")
        lessonPage.goButton.click()

        assertThat(lessonPage.assignmentOutput).containsText("Try again, name cannot be empty.")
    }

    @Test
    @Order(4)
    @DisplayName(
        "Given Sylvester solves the first assignment when Tweety logs in then the first assignment should NOT be solved",
    )
    fun shouldNotSolvePage1(browser: Browser) {
        lessonPage.navigateTo(2)
        lessonPage.enterYourName.fill("John Doe")
        lessonPage.goButton.click()

        val tweetyLessonPage = HttpBasicsLessonPage(Authentication.tweety(browser))
        tweetyLessonPage.open(LessonName("HttpBasics"))
        Assertions.assertThat(tweetyLessonPage.noAssignmentsCompleted()).isTrue()
    }
}
