/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.service

import org.owasp.webgoat.container.lessons.Hint
import org.owasp.webgoat.container.session.Course
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class HintService(
    course: Course,
) {
    private val allHints: List<Hint> =
        course.lessons
            .flatMap { lesson -> lesson.assignments }
            .flatMap { assignment -> createHint(assignment) }

    /**
     * Returns hints for current lesson
     *
     * @return a [List] object.
     */
    @GetMapping(path = [URL_HINTS_MVC], produces = ["application/json"])
    @ResponseBody
    fun getHints(): List<Hint> = allHints

    private fun createHint(a: org.owasp.webgoat.container.lessons.Assignment): List<Hint> =
        a.hints.map { h -> Hint(h, a.path) }

    companion object {
        const val URL_HINTS_MVC = "/service/hint.mvc"
    }
}
