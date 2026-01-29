/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

@Controller
class Welcome {
    /**
     * welcome.
     *
     * @param request a [jakarta.servlet.http.HttpServletRequest] object.
     * @return a [org.springframework.web.servlet.ModelAndView] object.
     */
    @GetMapping(path = ["welcome.mvc"])
    fun welcome(request: HttpServletRequest): ModelAndView {
        // set the welcome attribute
        // this is so the attack servlet does not also
        // send them to the welcome page
        request.session.apply {
            if (getAttribute(WELCOMED) == null) {
                setAttribute(WELCOMED, "true")
            }
        }

        // go ahead and send them to webgoat (skip the welcome page)
        return ModelAndView("forward:/attack?start=true")
    }

    companion object {
        private const val WELCOMED = "welcomed"
    }
}
