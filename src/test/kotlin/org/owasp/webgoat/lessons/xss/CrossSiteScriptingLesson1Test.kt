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

class CrossSiteScriptingLesson1Test : LessonTest() {
    @Test
    fun success() {
        mockMvc
            .perform(post(CONTEXT_PATH).param("checkboxAttack1", "value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun failure() {
        mockMvc
            .perform(post(CONTEXT_PATH))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
    }

    companion object {
        private const val CONTEXT_PATH = "/CrossSiteScripting/attack1"
    }
}
