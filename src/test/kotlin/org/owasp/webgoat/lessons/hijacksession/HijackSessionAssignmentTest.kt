/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.hijacksession

import jakarta.servlet.http.Cookie
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.mockito.Mockito.lenient
import org.mockito.kotlin.any
import org.owasp.webgoat.container.plugins.LessonTest
import org.owasp.webgoat.lessons.hijacksession.cas.Authentication
import org.owasp.webgoat.lessons.hijacksession.cas.HijackSessionAuthenticationProvider
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

class HijackSessionAssignmentTest : LessonTest() {
    @MockitoBean
    lateinit var authenticationMock: Authentication

    @MockitoBean
    lateinit var providerMock: HijackSessionAuthenticationProvider

    @Test
    fun testValidCookie() {
        lenient().`when`(authenticationMock.isAuthenticated).thenReturn(true)
        lenient().`when`(providerMock.authenticate(any<Authentication>())).thenReturn(authenticationMock)

        val cookie = Cookie(COOKIE_NAME, "value")

        val result =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .post(LOGIN_CONTEXT_PATH)
                    .cookie(cookie)
                    .param("username", "")
                    .param("password", ""),
            )

        result.andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(true)))
    }

    @Test
    fun testBlankCookie() {
        lenient().`when`(authenticationMock.isAuthenticated).thenReturn(false)
        lenient().`when`(providerMock.authenticate(any<Authentication>())).thenReturn(authenticationMock)
        val result =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .post(LOGIN_CONTEXT_PATH)
                    .param("username", "webgoat")
                    .param("password", "webgoat"),
            )

        result.andExpect(cookie().value(COOKIE_NAME, not(emptyString())))
        result.andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(false)))
    }

    companion object {
        private const val COOKIE_NAME = "hijack_cookie"
        private const val LOGIN_CONTEXT_PATH = "/HijackSession/login"
    }
}
