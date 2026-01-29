/*
 * SPDX-FileCopyrightText: Copyright © 2023 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.passwordreset

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class ResetLinkAssignmentTest : LessonTest() {
    @Value("\${webwolf.host}")
    private lateinit var webWolfHost: String

    @Value("\${webwolf.port}")
    private lateinit var webWolfPort: String

    @Autowired
    private lateinit var resourceLoader: ResourceLoader

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Test
    fun wrongResetLink() {
        val mvcResult =
            mockMvc
                .perform(
                    MockMvcRequestBuilders.get("/PasswordReset/reset/reset-password/{link}", "test"),
                ).andExpect(status().isOk)
                .andExpect(view().name("lessons/passwordreset/templates/password_link_not_found.html"))
                .andReturn()
        val viewName = requireNotNull(mvcResult.modelAndView?.viewName)
        Assertions
            .assertThat(resourceLoader.getResource(viewName))
            .isNotNull()
    }

    @Test
    fun changePasswordWithoutPasswordShouldReturnPasswordForm() {
        val mvcResult =
            mockMvc
                .perform(MockMvcRequestBuilders.post("/PasswordReset/reset/change-password"))
                .andExpect(status().isOk)
                .andExpect(view().name("lessons/passwordreset/templates/password_reset.html"))
                .andReturn()
        val viewName = requireNotNull(mvcResult.modelAndView?.viewName)
        Assertions
            .assertThat(resourceLoader.getResource(viewName))
            .isNotNull()
    }

    @Test
    fun changePasswordWithoutLinkShouldReturnPasswordLinkNotFound() {
        val mvcResult =
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/PasswordReset/reset/change-password")
                        .param("password", "new_password"),
                ).andExpect(status().isOk)
                .andExpect(view().name("lessons/passwordreset/templates/password_link_not_found.html"))
                .andReturn()
        val viewName = requireNotNull(mvcResult.modelAndView?.viewName)
        Assertions
            .assertThat(resourceLoader.getResource(viewName))
            .isNotNull()
    }

    @Test
    fun knownLinkShouldReturnPasswordResetPage() {
        // Create a reset link
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/PasswordReset/ForgotPassword/create-password-reset-link")
                    .param("email", ResetLinkAssignment.TOM_EMAIL)
                    .header(HttpHeaders.HOST, "$webWolfHost:$webWolfPort"),
            ).andExpect(status().isOk)
        Assertions.assertThat(ResetLinkAssignment.resetLinks).isNotEmpty()

        // With a known link you should be
        val mvcResult =
            mockMvc
                .perform(
                    MockMvcRequestBuilders.get(
                        "/PasswordReset/reset/reset-password/{link}",
                        ResetLinkAssignment.resetLinks[0],
                    ),
                ).andExpect(status().isOk)
                .andExpect(view().name("lessons/passwordreset/templates/password_reset.html"))
                .andReturn()

        val viewName = requireNotNull(mvcResult.modelAndView?.viewName)
        Assertions
            .assertThat(resourceLoader.getResource(viewName))
            .isNotNull()
    }
}
