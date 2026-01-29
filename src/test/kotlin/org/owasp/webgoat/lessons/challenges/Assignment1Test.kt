/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.challenges

import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.owasp.webgoat.lessons.challenges.challenge1.ImageServlet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.net.InetAddress

class Assignment1Test : LessonTest() {
    @Autowired
    private lateinit var flags: Flags

    @Test
    fun success() {
        val addr = InetAddress.getLocalHost()
        val host = addr.hostAddress
        mockMvc
            .perform(
                post("/challenge/1")
                    .header("X-Forwarded-For", host)
                    .param("username", "admin")
                    .param(
                        "password",
                        SolutionConstants.PASSWORD.replace(
                            "1234",
                            String.format("%04d", ImageServlet.PINCODE),
                        ),
                    ),
            ).andExpect(jsonPath("$.feedback", containsString("flag: ${flags.getFlag(1)}")))
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun wrongPassword() {
        mockMvc
            .perform(
                post("/challenge/1")
                    .param("username", "admin")
                    .param("password", "wrong"),
            ).andExpect(jsonPath("$.feedback", `is`(messages.getMessage("assignment.not.solved"))))
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
    }
}
