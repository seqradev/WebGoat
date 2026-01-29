/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.playwright.webgoat.helpers

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.owasp.webgoat.playwright.webgoat.pages.RegistrationPage
import org.owasp.webgoat.playwright.webgoat.pages.WebGoatLoginPage
import org.owasp.webgoat.playwright.webwolf.pages.WebWolfLoginPage

/**
 * Helper class to authenticate users in WebGoat and WebWolf.
 *
 * It provides two users: sylvester and tweety. The users are authenticated by logging in to
 * WebGoat and WebWolf. Once authenticated, the user's authentication token is stored in the browser
 * and reused for subsequent requests.
 */
object Authentication {
    data class User(
        val name: String,
        val password: String,
        val auth: String? = null,
    ) {
        fun loggedIn(): Boolean = auth != null
    }

    private var sylvesterUser = User("sylvester", "sylvester")
    private var tweetyUser = User("tweety", "tweety")

    @JvmStatic
    fun getSylvester(): User = sylvesterUser

    @JvmStatic
    fun getTweety(): User = tweetyUser

    @JvmStatic
    fun sylvester(browser: Browser): Page {
        val user = login(browser, sylvesterUser)
        sylvesterUser = user
        return browser
            .newContext(
                Browser
                    .NewContextOptions()
                    .setLocale("en-US")
                    .setStorageState(user.auth),
            ).newPage()
    }

    @JvmStatic
    fun tweety(browser: Browser): Page {
        val user = login(browser, tweetyUser)
        tweetyUser = user
        return browser
            .newContext(
                Browser
                    .NewContextOptions()
                    .setLocale("en-US")
                    .setStorageState(user.auth),
            ).newPage()
    }

    private fun login(
        browser: Browser,
        user: User,
    ): User {
        if (user.loggedIn()) {
            return user
        }
        val page = browser.newContext(Browser.NewContextOptions().setLocale("en-US")).newPage()
        val registrationPage = RegistrationPage(page)
        registrationPage.open()
        registrationPage.register(user.name, user.password)

        val loginPage = WebGoatLoginPage(page)
        loginPage.open()
        loginPage.login(user.name, user.password)
        assertThat(loginPage.signInButton).not().isVisible()

        val webWolfLoginPage = WebWolfLoginPage(page)
        webWolfLoginPage.open()
        webWolfLoginPage.login(user.name, user.password)
        assertThat(loginPage.signInButton).not().isVisible()

        return User(user.name, user.password, page.context().storageState())
    }
}
