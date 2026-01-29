/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.report

import org.owasp.webgoat.container.CurrentUsername
import org.owasp.webgoat.container.i18n.PluginMessages
import org.owasp.webgoat.container.session.Course
import org.owasp.webgoat.container.users.UserProgressRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ReportCardController(
    private val userProgressRepository: UserProgressRepository,
    private val course: Course,
    private val pluginMessages: PluginMessages,
) {
    @GetMapping(path = ["/service/reportcard.mvc"], produces = ["application/json"])
    @ResponseBody
    fun reportCard(
        @CurrentUsername username: String?,
    ): ReportCard {
        val userProgress =
            requireNotNull(userProgressRepository.findByUser(username)) {
                "User progress not found for user: $username"
            }
        val lessonStatistics =
            course.lessons.map { lesson ->
                val lessonTracker = userProgress.getLessonProgress(lesson)
                LessonStatistics(
                    name = pluginMessages.getMessage(lesson.getTitle()) ?: "",
                    solved = lessonTracker.isLessonSolved,
                    numberOfAttempts = lessonTracker.numberOfAttempts,
                )
            }
        return ReportCard(
            totalNumberOfLessons = course.totalOfLessons,
            totalNumberOfAssignments = course.totalOfAssignments,
            numberOfAssignmentsSolved = userProgress.numberOfAssignmentsSolved(),
            numberOfLessonsSolved = userProgress.numberOfLessonsSolved(),
            lessonStatistics = lessonStatistics,
        )
    }

    data class ReportCard(
        val totalNumberOfLessons: Int,
        val totalNumberOfAssignments: Int,
        val numberOfAssignmentsSolved: Long,
        val numberOfLessonsSolved: Long,
        val lessonStatistics: List<LessonStatistics>,
    )

    data class LessonStatistics(
        val name: String,
        val solved: Boolean,
        val numberOfAttempts: Int,
    )
}
