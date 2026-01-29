/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xss

import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class StoredXssCommentsTest : LessonTest() {
    @Test
    fun success() {
        val results =
            mockMvc.perform(
                post("/CrossSiteScriptingStored/stored-xss")
                    .content("{\"text\":\"someTextHere<script>webgoat.customjs.phoneHome()</script>MoreTextHere\"}")
                    .contentType(MediaType.APPLICATION_JSON),
            )

        results.andExpect(status().isOk())
        results.andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun failure() {
        val results =
            mockMvc.perform(
                post("/CrossSiteScriptingStored/stored-xss")
                    .content("{\"text\":\"someTextHere<script>alert('Xss')</script>MoreTextHere\"}")
                    .contentType(MediaType.APPLICATION_JSON),
            )

        results.andExpect(status().isOk())
        results.andExpect(jsonPath("$.lessonCompleted", `is`(false)))
    }

    // Ensures it is vulnerable
    @Test
    fun isNotEncoded() {
        // do get to get comments after posting xss payload
        val taintedResults = mockMvc.perform(get("/CrossSiteScriptingStored/stored-xss"))
        val mvcResult = taintedResults.andReturn()
        assert(mvcResult.response.contentAsString.contains("<script>console.warn"))
    }
}
