/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf.requests

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import java.util.concurrent.Callable

@Controller
@RequestMapping("/landing/**")
class LandingPage {
    private val log = LoggerFactory.getLogger(LandingPage::class.java)

    @RequestMapping(
        method =
            [
                RequestMethod.POST,
                RequestMethod.GET,
                RequestMethod.DELETE,
                RequestMethod.PATCH,
                RequestMethod.PUT,
            ],
    )
    fun ok(request: HttpServletRequest): Callable<ResponseEntity<*>> =
        Callable {
            log.trace("Incoming request for: {}", request.requestURL)
            ResponseEntity.ok().build<Any>()
        }
}
