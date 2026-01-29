/*
 * SPDX-FileCopyrightText: Copyright © 2022 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.service

import org.owasp.webgoat.container.CurrentUsername
import org.owasp.webgoat.container.lessons.Assignment
import org.owasp.webgoat.container.lessons.LessonName
import org.owasp.webgoat.container.session.Course
import org.owasp.webgoat.container.users.UserProgressRepository
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class LessonProgressService(
    private val userProgressRepository: UserProgressRepository,
    private val course: Course,
) {
    @GetMapping(value = ["/service/lessonoverview.mvc/{lesson}"])
    @ResponseBody
    fun lessonOverview(
        @PathVariable("lesson") lessonName: LessonName,
        @CurrentUsername username: String?,
    ): List<LessonOverview> {
        val userProgress =
            requireNotNull(userProgressRepository.findByUser(username)) {
                "User progress not found for user: $username"
            }
        val lesson =
            requireNotNull(course.getLessonByName(lessonName)) {
                "Lesson not found: $lessonName"
            }
        return userProgress
            .getLessonProgress(lesson)
            .getLessonOverview()
            .mapNotNull { (assignmentProgress, solved) ->
                assignmentProgress.assignment?.let { assignment ->
                    LessonOverview(assignment, solved)
                }
            }
    }

    // Jackson does not really like returning a map of <Assignment, Boolean> directly, see
    // http://stackoverflow.com/questions/11628698/can-we-make-object-as-key-in-map-when-using-json
    // so creating intermediate object is the easiest solution
    data class LessonOverview(
        val assignment: Assignment,
        val solved: Boolean,
    )
}
