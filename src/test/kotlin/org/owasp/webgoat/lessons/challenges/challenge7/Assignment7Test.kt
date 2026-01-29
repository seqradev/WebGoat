/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.challenges.challenge7

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.client.RestTemplate

class Assignment7Test : LessonTest() {
    @MockitoBean
    private lateinit var restTemplate: RestTemplate

    @Value("\${webwolf.mail.url}")
    private lateinit var webWolfMailURL: String

    @Test
    @DisplayName("Reset password test")
    fun resetPasswordTest() {
        var result = mockMvc.perform(get("$RESET_PASSWORD_PATH/any"))
        result.andExpect(status().`is`(equalTo(HttpStatus.I_AM_A_TEAPOT.value())))

        result =
            mockMvc.perform(get("$RESET_PASSWORD_PATH/${Assignment7.ADMIN_PASSWORD_LINK}"))
        result.andExpect(status().`is`(equalTo(HttpStatus.ACCEPTED.value())))
    }

    @Test
    @DisplayName("Send password reset link test")
    fun sendPasswordResetLinkTest() {
        val result =
            mockMvc.perform(
                post(CHALLENGE_PATH)
                    .param("email", "webgoat@webgoat-cloud.net"),
            )
        result.andExpect(status().isOk())
        result.andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    @DisplayName("git test")
    fun gitTest() {
        val result = mockMvc.perform(get(GIT_PATH))
        result.andExpect(content().contentType("application/zip"))
    }

    companion object {
        private const val CHALLENGE_PATH = "/challenge/7"
        private const val RESET_PASSWORD_PATH = "$CHALLENGE_PATH/reset-password"
        private const val GIT_PATH = "$CHALLENGE_PATH/.git"
    }
}
