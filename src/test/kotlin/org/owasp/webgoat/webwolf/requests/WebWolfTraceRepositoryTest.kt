/*
 * SPDX-FileCopyrightText: Copyright © 2023 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf.requests

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.actuate.web.exchanges.HttpExchange
import java.net.URI

class WebWolfTraceRepositoryTest {
    @Test
    @DisplayName("When a user hits a file upload it should be recorded")
    fun shouldAddFilesRequest() {
        val httpExchange = mock<HttpExchange>()
        val request = mock<HttpExchange.Request>()
        `when`(httpExchange.request).thenReturn(request)
        `when`(request.uri).thenReturn(URI.create("http://localhost:9090/files/test1234/test.jpg"))
        val repository = WebWolfTraceRepository()

        repository.add(httpExchange)

        assertThat(repository.findAll()).hasSize(1)
    }

    @Test
    @DisplayName("When a user hits file upload page ('/files') it should be recorded")
    fun shouldAddNotAddFilesRequestOverview() {
        val httpExchange = mock<HttpExchange>()
        val request = mock<HttpExchange.Request>()
        `when`(httpExchange.request).thenReturn(request)
        `when`(request.uri).thenReturn(URI.create("http://localhost:9090/files"))
        val repository = WebWolfTraceRepository()

        repository.add(httpExchange)

        assertThat(repository.findAll()).hasSize(0)
    }
}
