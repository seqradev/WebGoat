/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.controller

import jakarta.servlet.http.HttpServletRequest
import org.owasp.webgoat.container.session.Course
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

@Controller
class StartLesson(
    private val course: Course,
) {
    @GetMapping(value = ["*.lesson"], produces = ["text/html"])
    fun lessonPage(request: HttpServletRequest): ModelAndView {
        val model = ModelAndView("lesson_content")
        val path = request.requestURL.toString() // we now got /a/b/c/AccessControlMatrix.lesson
        val lessonName = path.substringAfterLast('/').removeSuffix(".lesson")

        course.lessons
            .firstOrNull { it.getId() == lessonName }
            ?.let { request.setAttribute("lesson", it) }

        return model
    }
}
