/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.playwright.webgoat.lessons

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.lessons.LessonName
import org.owasp.webgoat.playwright.webgoat.PlaywrightTest
import org.owasp.webgoat.playwright.webgoat.helpers.Authentication
import org.owasp.webgoat.playwright.webgoat.pages.lessons.LessonPage

class SqlInjectionAdvancedUITest : PlaywrightTest() {
    private lateinit var lessonPage: LessonPage

    @BeforeEach
    fun navigateToLesson(browser: Browser) {
        val lessonName = LessonName("SqlInjectionAdvanced")
        val page = Authentication.sylvester(browser)

        lessonPage = LessonPage(page)
        lessonPage.resetLesson(lessonName)
        lessonPage.open(lessonName)
    }

    @Test
    @DisplayName("Login as Tom with incorrect password")
    fun loginAsTomWithIncorrectPassword() {
        lessonPage.navigateTo(5)
        val page = lessonPage.page
        page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Login")).click()
        page.locator("[name='username_login']").fill("tom")
        page.locator("[name='password_login']").fill("test")
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Log In")).click()

        assertThat(lessonPage.assignmentOutput)
            .containsText("Wrong username or password. Try again.")
    }

    @Test
    @DisplayName("Login as Tom with correct password")
    fun loginAsTomWithCorrectPassword() {
        lessonPage.navigateTo(5)
        val page = lessonPage.page
        page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Login")).click()
        page.locator("[name='username_login']").fill("tom")
        page.locator("[name='password_login']").fill("thisisasecretfortomonly")
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Log In")).click()

        lessonPage.isAssignmentSolved(5)
    }

    @Test
    @DisplayName("Register as Tom should show error that Tom already exists")
    fun registerAsTomShouldDisplayError() {
        lessonPage.navigateTo(5)
        val page = lessonPage.page
        page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Register")).click()
        page.locator("[name='username_reg']").fill("tom")
        page.locator("[name='email_reg']").fill("tom@tom.org")
        page.locator("[name='password_reg']").fill("test")
        page.locator("[name='confirm_password_reg']").fill("test")
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Register Now")).click()

        assertThat(lessonPage.assignmentOutput).containsText("User tom already exists")
    }

    @Test
    @DisplayName("Using SQL Injection to register as Tom to guess the password and the guess is correct")
    fun startGuessingCorrect() {
        lessonPage.navigateTo(5)
        val page = lessonPage.page
        page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Register")).click()
        page.locator("[name='username_reg']").fill("tom' AND substring(password,1,1)='t")
        page.locator("[name='email_reg']").fill("tom@tom.org")
        page.locator("[name='password_reg']").fill("test")
        page.locator("[name='confirm_password_reg']").fill("test")
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Register Now")).click()

        assertThat(lessonPage.assignmentOutput)
            .containsText("User tom' AND substring(password,1,1)='t already exists")
    }

    @Test
    @DisplayName("Using SQL Injection to register as Tom to guess the password and the guess is incorrect")
    fun startGuessingIncorrect() {
        lessonPage.navigateTo(5)
        val page = lessonPage.page
        page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Register")).click()
        page.locator("[name='username_reg']").fill("tom' AND substring(password,1,1)='a")
        page.locator("[name='email_reg']").fill("tom@tom.org")
        page.locator("[name='password_reg']").fill("test")
        page.locator("[name='confirm_password_reg']").fill("test")
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Register Now")).click()

        assertThat(lessonPage.assignmentOutput)
            .containsText("User tom' AND substring(password,1,1)='a created, please proceed to the login page.")
    }

    @Test
    @DisplayName("Should display correct hints")
    fun shouldDisplayCorrectHints() {
        lessonPage.navigateTo(5)
        val page = lessonPage.page
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Show hints")).click()
        assertThat(lessonPage.assignmentOutput).containsText("Look at the different")
    }
}
