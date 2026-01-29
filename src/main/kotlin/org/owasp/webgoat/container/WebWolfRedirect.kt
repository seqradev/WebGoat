/*
 * SPDX-FileCopyrightText: Copyright © 2022 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container

import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

@Controller
class WebWolfRedirect(
    private val applicationContext: ApplicationContext,
) {
    @GetMapping("/WebWolf")
    fun openWebWolf(): ModelAndView =
        applicationContext.environment
            .getProperty("webwolf.url")
            .let { url -> ModelAndView("redirect:$url/home") }
}
