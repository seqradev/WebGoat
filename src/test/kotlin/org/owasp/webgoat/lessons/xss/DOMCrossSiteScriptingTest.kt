/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xss

import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class DOMCrossSiteScriptingTest : LessonTest() {
    @Test
    fun success() {
        mockMvc
            .perform(
                post("/CrossSiteScripting/phone-home-xss")
                    .header("webgoat-requested-by", "dom-xss-vuln")
                    .param("param1", "42")
                    .param("param2", "24"),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun failure() {
        mockMvc
            .perform(
                post("/CrossSiteScripting/phone-home-xss")
                    .header("webgoat-requested-by", "wrong-value")
                    .param("param1", "22")
                    .param("param2", "20"),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
    }
}
