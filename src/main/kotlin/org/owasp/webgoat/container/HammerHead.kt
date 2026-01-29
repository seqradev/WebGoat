/*
 * SPDX-FileCopyrightText: Copyright © 2008 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container

import org.owasp.webgoat.container.session.Course
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.ModelAndView

@Controller
class HammerHead(
    private val course: Course,
) {
    /** Entry point for WebGoat, redirects to the first lesson found within the course. */
    @RequestMapping(
        path = ["/attack"],
        method = [RequestMethod.GET, RequestMethod.POST],
    )
    fun attack(): ModelAndView = ModelAndView("redirect:start.mvc" + course.firstLesson.getLink())
}
