/*
 * SPDX-FileCopyrightText: Copyright © 2023 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.owasp.webgoat.container.asciidoc.EnvironmentExposure
import org.springframework.core.env.Environment
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

class UserInterceptor : HandlerInterceptor {
    private val env: Environment? = EnvironmentExposure.getEnv()

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean = true

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?,
    ) {
        modelAndView?.let { mv ->
            SecurityContextHolder.getContext().authentication?.let { authentication ->
                mv.model["username"] = authentication.name
            }
            val githubClientId = env?.getProperty("spring.security.oauth2.client.registration.github.client-id")
            mv.model["oauth"] = githubClientId != null && githubClientId != "dummy"
        }
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        // Do nothing
    }
}
