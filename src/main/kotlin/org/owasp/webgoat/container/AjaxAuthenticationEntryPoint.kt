/*
 * SPDX-FileCopyrightText: Copyright © 2008 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint

class AjaxAuthenticationEntryPoint(
    loginFormUrl: String,
) : LoginUrlAuthenticationEntryPoint(loginFormUrl) {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        if (request.getHeader("x-requested-with") != null) {
            response.sendError(401, authException.message)
        } else {
            super.commence(request, response, authException)
        }
    }
}
