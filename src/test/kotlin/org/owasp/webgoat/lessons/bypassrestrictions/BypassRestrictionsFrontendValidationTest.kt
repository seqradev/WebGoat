/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.bypassrestrictions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class BypassRestrictionsFrontendValidationTest : LessonTest() {
    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Test
    fun noChangesShouldNotPassTheLesson() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/BypassRestrictions/frontendValidation")
                    .param("field1", "abc")
                    .param("field2", "123")
                    .param("field3", "abc ABC 123")
                    .param("field4", "seven")
                    .param("field5", "01101")
                    .param("field6", "90201 1111")
                    .param("field7", "301-604-4882")
                    .param("error", "2"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
    }

    @Test
    fun bypassAllFieldShouldPass() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/BypassRestrictions/frontendValidation")
                    .param("field1", "abcd")
                    .param("field2", "1234")
                    .param("field3", "abc \$ABC 123")
                    .param("field4", "ten")
                    .param("field5", "01101AA")
                    .param("field6", "90201 1111AA")
                    .param("field7", "301-604-4882\$\$")
                    .param("error", "0"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun notBypassingAllFieldShouldNotPass() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/BypassRestrictions/frontendValidation")
                    .param("field1", "abc")
                    .param("field2", "1234")
                    .param("field3", "abc \$ABC 123")
                    .param("field4", "ten")
                    .param("field5", "01101AA")
                    .param("field6", "90201 1111AA")
                    .param("field7", "301-604-4882AA")
                    .param("error", "0"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
    }
}
