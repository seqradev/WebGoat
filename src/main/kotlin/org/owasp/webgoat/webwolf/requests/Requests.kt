/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf.requests

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.web.exchanges.HttpExchange
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import java.time.Instant

/** Controller for fetching all the HTTP requests from WebGoat to WebWolf for a specific user. */
@Controller
@RequestMapping(value = ["/requests"])
class Requests(
    private val traceRepository: WebWolfTraceRepository,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(Requests::class.java)

    private data class Tracert(
        val date: Instant,
        val path: String,
        val json: String,
    )

    @GetMapping
    fun get(authentication: Authentication?): ModelAndView {
        val model = ModelAndView("requests")
        val username = authentication?.name ?: "anonymous"
        val traces =
            traceRepository
                .findAll()
                .filter { allowedTrace(it, username) }
                .map { Tracert(it.timestamp, path(it), toJsonString(it)) }
        model.addObject("traces", traces)

        return model
    }

    private fun allowedTrace(
        t: HttpExchange,
        username: String,
    ): Boolean {
        val req = t.request
        val path = req.uri.path
        val query = req.uri.query
        // do not show certain traces to other users in a classroom setup
        return when {
            path.contains("/files") && !path.contains(username) -> false
            path.contains("/landing") &&
                query?.contains("uniqueCode") == true &&
                !query.contains(username.reversed()) -> false
            else -> true
        }
    }

    private fun path(t: HttpExchange): String = t.request.uri.path

    private fun toJsonString(t: HttpExchange): String =
        try {
            objectMapper.writeValueAsString(t)
        } catch (e: JsonProcessingException) {
            log.error("Unable to create json", e)
            "No request(s) found"
        }
}
