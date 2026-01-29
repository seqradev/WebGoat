/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.mitigation

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SqlInjectionLesson13Test : LessonTest() {
    @Test
    fun knownAccountShouldDisplayData() {
        mockMvc
            .perform(
                get("/SqlInjectionMitigations/servers").param("column", "id"),
            ).andExpect(status().isOk)
    }

    @Test
    fun addressCorrectShouldOrderByHostname() {
        mockMvc
            .perform(
                get("/SqlInjectionMitigations/servers")
                    .param(
                        "column",
                        "CASE WHEN (SELECT ip FROM servers WHERE hostname='webgoat-prd') LIKE '104.%'" +
                            " THEN hostname ELSE id END",
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].hostname", `is`("webgoat-acc")))
    }

    @Test
    fun addressCorrectShouldOrderByHostnameUsingSubstr() {
        mockMvc
            .perform(
                get("/SqlInjectionMitigations/servers")
                    .param(
                        "column",
                        "case when (select ip from servers where hostname='webgoat-prd' and" +
                            " substr(ip,1,1) = '1') IS NOT NULL then hostname else id end",
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].hostname", `is`("webgoat-acc")))

        mockMvc
            .perform(
                get("/SqlInjectionMitigations/servers")
                    .param(
                        "column",
                        "case when (select ip from servers where hostname='webgoat-prd' and" +
                            " substr(ip,2,1) = '0') IS NOT NULL then hostname else id end",
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].hostname", `is`("webgoat-acc")))

        mockMvc
            .perform(
                get("/SqlInjectionMitigations/servers")
                    .param(
                        "column",
                        "case when (select ip from servers where hostname='webgoat-prd' and" +
                            " substr(ip,3,1) = '4') IS NOT NULL then hostname else id end",
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].hostname", `is`("webgoat-acc")))
    }

    @Test
    fun addressIncorrectShouldOrderByIdUsingSubstr() {
        mockMvc
            .perform(
                get("/SqlInjectionMitigations/servers")
                    .param(
                        "column",
                        "case when (select ip from servers where hostname='webgoat-prd' and" +
                            " substr(ip,1,1) = '9') IS NOT NULL then hostname else id end",
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].hostname", `is`("webgoat-dev")))
    }

    @Test
    fun trueShouldSortByHostname() {
        mockMvc
            .perform(
                get("/SqlInjectionMitigations/servers")
                    .param("column", "(case when (true) then hostname else id end)"),
            ).andExpect(status().isOk)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].hostname", `is`("webgoat-acc")))
    }

    @Test
    fun falseShouldSortById() {
        mockMvc
            .perform(
                get("/SqlInjectionMitigations/servers")
                    .param("column", "(case when (true) then hostname else id end)"),
            ).andExpect(status().isOk)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].hostname", `is`("webgoat-acc")))
    }

    @Test
    fun addressIncorrectShouldOrderByHostname() {
        mockMvc
            .perform(
                get("/SqlInjectionMitigations/servers")
                    .param(
                        "column",
                        "CASE WHEN (SELECT ip FROM servers WHERE hostname='webgoat-prd') LIKE '192.%'" +
                            " THEN hostname ELSE id END",
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].hostname", `is`("webgoat-dev")))
    }

    @Test
    fun postingCorrectAnswerShouldPassTheLesson() {
        mockMvc
            .perform(
                post("/SqlInjectionMitigations/attack12a")
                    .param("ip", "104.130.219.202"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(true)))
    }

    @Test
    fun postingWrongAnswerShouldNotPassTheLesson() {
        mockMvc
            .perform(
                post("/SqlInjectionMitigations/attack12a")
                    .param("ip", "192.168.219.202"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.lessonCompleted", `is`(false)))
    }
}
