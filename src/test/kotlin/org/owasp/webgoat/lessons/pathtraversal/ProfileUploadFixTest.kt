/*
 * SPDX-FileCopyrightText: Copyright © 2020 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.pathtraversal

import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.WithWebGoatUser
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.io.File

@WithWebGoatUser
class ProfileUploadFixTest : LessonTest() {
    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Test
    fun solve() {
        val profilePicture =
            MockMultipartFile(
                "uploadedFileFix",
                "../picture.jpg",
                "text/plain",
                "an image".toByteArray(),
            )

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .multipart("/PathTraversal/profile-upload-fix")
                    .file(profilePicture)
                    .param("fullNameFix", "..././John Doe"),
            ).andExpect(status().`is`(200))
            .andExpect(jsonPath("$.assignment", CoreMatchers.equalTo("ProfileUploadFix")))
            .andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(true)))
    }

    @Test
    fun normalUpdate() {
        val profilePicture =
            MockMultipartFile(
                "uploadedFileFix",
                "picture.jpg",
                "text/plain",
                "an image".toByteArray(),
            )

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .multipart("/PathTraversal/profile-upload-fix")
                    .file(profilePicture)
                    .param("fullNameFix", "John Doe"),
            ).andExpect(status().`is`(200))
            .andExpect(
                jsonPath(
                    "$.feedback",
                    CoreMatchers.containsString("test\\${File.separator}John Doe"),
                ),
            ).andExpect(jsonPath("$.lessonCompleted", CoreMatchers.`is`(false)))
    }
}
