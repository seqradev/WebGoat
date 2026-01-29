/*
 * SPDX-FileCopyrightText: Copyright © 2021 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.spoofcookie

import jakarta.servlet.http.Cookie
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.stream.Stream

class SpoofCookieAssignmentTest : LessonTest() {
    @Test
    @DisplayName("Lesson completed")
    fun success() {
        val cookie = Cookie(COOKIE_NAME, "NjI2MTcwNGI3YTQxNGE1OTU2NzQ2ZDZmNzQ=")

        val result =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .post(LOGIN_CONTEXT_PATH)
                    .cookie(cookie)
                    .param("username", "")
                    .param("password", ""),
            )

        result.andExpect(status().isOk)
        result.andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(true)))
    }

    @Test
    @DisplayName("Valid credentials login without authentication cookie")
    fun validLoginWithoutCookieTest() {
        val username = "webgoat"
        val password = "webgoat"

        val result =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .post(LOGIN_CONTEXT_PATH)
                    .param("username", username)
                    .param("password", password),
            )

        result.andExpect(status().isOk)
        result.andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(false)))
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        result.andExpect(cookie().value(COOKIE_NAME, not(emptyString())))
    }

    @ParameterizedTest
    @MethodSource("providedCookieValues")
    @DisplayName(
        "Tests different invalid/valid -but not solved- cookie flow scenarios: " +
            "1.- Invalid encoded cookie sent. " +
            "2.- Valid cookie login (not tom) sent. " +
            "3.- Valid cookie with not known username sent ",
    )
    fun cookieLoginNotSolvedFlow(cookieValue: String) {
        val cookie = Cookie(COOKIE_NAME, cookieValue)
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post(LOGIN_CONTEXT_PATH)
                    .cookie(cookie)
                    .param("username", "")
                    .param("password", ""),
            ).andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(false)))
    }

    @Test
    @DisplayName("UnsatisfiedServletRequestParameterException test for missing username")
    fun invalidLoginWithUnsatisfiedServletRequestParameterExceptionOnUsernameMissing() {
        mockMvc
            .perform(MockMvcRequestBuilders.post(LOGIN_CONTEXT_PATH).param("password", "anypassword"))
            .andExpect(status().is4xxClientError)
    }

    @Test
    @DisplayName("UnsatisfiedServletRequestParameterException test for missing password")
    fun invalidLoginWithUnsatisfiedServletRequestParameterExceptionOnPasswordMissing() {
        mockMvc
            .perform(MockMvcRequestBuilders.post(LOGIN_CONTEXT_PATH).param("username", "webgoat"))
            .andExpect(status().is4xxClientError)
    }

    @Test
    @DisplayName("Invalid blank credentials login")
    fun invalidLoginWithBlankCredentials() {
        val result =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .post(LOGIN_CONTEXT_PATH)
                    .param("username", "")
                    .param("password", ""),
            )

        result.andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(false)))
    }

    @Test
    @DisplayName("Invalid blank password login")
    fun invalidLoginWithBlankPassword() {
        val result =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .post(LOGIN_CONTEXT_PATH)
                    .param("username", "webgoat")
                    .param("password", ""),
            )

        result.andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(false)))
    }

    @Test
    @DisplayName("cheater test")
    fun cheat() {
        val result =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .post(LOGIN_CONTEXT_PATH)
                    .param("username", "tom")
                    .param("password", "apasswordfortom"),
            )

        result.andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(false)))
    }

    @Test
    @DisplayName("Invalid login with tom username")
    fun invalidTomLogin() {
        val result =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .post(LOGIN_CONTEXT_PATH)
                    .param("username", "tom")
                    .param("password", ""),
            )

        result.andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(false)))
    }

    @Test
    @DisplayName("Erase authentication cookie")
    fun eraseAuthenticationCookie() {
        mockMvc
            .perform(MockMvcRequestBuilders.get(ERASE_COOKIE_CONTEXT_PATH))
            .andExpect(status().isOk)
            .andExpect(cookie().maxAge(COOKIE_NAME, 0))
            .andExpect(cookie().value(COOKIE_NAME, ""))
    }

    companion object {
        private const val COOKIE_NAME = "spoof_auth"
        private const val LOGIN_CONTEXT_PATH = "/SpoofCookie/login"
        private const val ERASE_COOKIE_CONTEXT_PATH = "/SpoofCookie/cleanup"

        @JvmStatic
        fun providedCookieValues(): Stream<Arguments> =
            Stream.of(
                Arguments.of("NjI2MTcwNGI3YTQxNGE1OTUNzQ2ZDZmNzQ="),
                Arguments.of("NjI2MTcwNGI3YTQxNGE1OTU2NzQ3NDYxNmY2NzYyNjU3Nw=="),
                Arguments.of("NmQ0NjQ1Njc0NjY4NGY2Mjc0NjQ2YzY1Njc2ZTYx"),
            )
    }
}
