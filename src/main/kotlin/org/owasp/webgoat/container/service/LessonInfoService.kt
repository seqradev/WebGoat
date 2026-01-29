/*
 * SPDX-FileCopyrightText: Copyright © 2015 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.service

import org.owasp.webgoat.container.lessons.LessonInfoModel
import org.owasp.webgoat.container.lessons.LessonName
import org.owasp.webgoat.container.session.Course
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class LessonInfoService(
    private val course: Course,
) {
    @GetMapping(path = ["/service/lessoninfo.mvc/{lesson}"])
    @ResponseBody
    fun getLessonInfo(
        @PathVariable("lesson") lessonName: LessonName,
    ): LessonInfoModel {
        val lesson =
            requireNotNull(course.getLessonByName(lessonName)) {
                "Lesson not found: $lessonName"
            }
        return LessonInfoModel(lesson.getTitle(), false, false, false)
    }
}
