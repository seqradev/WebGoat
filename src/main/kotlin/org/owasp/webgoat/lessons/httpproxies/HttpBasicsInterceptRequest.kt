/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.httpproxies

import jakarta.servlet.http.HttpServletRequest
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class HttpBasicsInterceptRequest : AssignmentEndpoint {
    @RequestMapping(
        path = ["/HttpProxies/intercept-request"],
        method = [RequestMethod.POST, RequestMethod.GET],
    )
    @ResponseBody
    fun completed(
        @RequestHeader(value = "x-request-intercepted", required = false) headerValue: Boolean?,
        @RequestParam(value = "changeMe", required = false) paramValue: String?,
        request: HttpServletRequest,
    ): AttackResult =
        when {
            HttpMethod.POST.matches(request.method) ->
                failed(this).feedback("http-proxies.intercept.failure").build()
            headerValue == true && "Requests are tampered easily".equals(paramValue, ignoreCase = true) ->
                success(this).feedback("http-proxies.intercept.success").build()
            else -> failed(this).feedback("http-proxies.intercept.failure").build()
        }
}
