/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.playwright.webgoat

import com.microsoft.playwright.Browser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.owasp.webgoat.playwright.webgoat.helpers.Authentication
import org.owasp.webgoat.playwright.webgoat.pages.RegistrationPage
import org.owasp.webgoat.playwright.webgoat.pages.WebGoatLoginPage

class RegistrationUITest : PlaywrightTest() {
    @Test
    @DisplayName("Should register a new user while logged in as other user")
    fun registerWhileLoggedIn(browser: Browser) {
        val page = Authentication.tweety(browser)
        val loginPage = WebGoatLoginPage(page)
        loginPage.open()
        loginPage.login(Authentication.getTweety().name, Authentication.getTweety().password)

        val newUsername = "newuser${System.currentTimeMillis()}"
        val password = "password123"
        val registrationPage = RegistrationPage(page)
        registrationPage.open()
        registrationPage.register(newUsername, password)

        assertThat(page.content()).contains(newUsername)
    }

    @Test
    @DisplayName("Should register a new user")
    fun registerNewUser(browser: Browser) {
        val page = browser.newContext(Browser.NewContextOptions().setLocale("en-US")).newPage()
        val registrationPage = RegistrationPage(page)
        registrationPage.open()

        val newUsername = "newuser${System.currentTimeMillis()}"
        val password = "password123"
        registrationPage.register(newUsername, password)

        assertThat(page.content()).contains(newUsername)
    }
}
