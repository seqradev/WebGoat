/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.service

import org.flywaydb.core.Flyway
import org.owasp.webgoat.container.CurrentUser
import org.owasp.webgoat.container.lessons.Initializable
import org.owasp.webgoat.container.lessons.LessonName
import org.owasp.webgoat.container.session.Course
import org.owasp.webgoat.container.users.UserProgressRepository
import org.owasp.webgoat.container.users.WebGoatUser
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.function.Function

@Controller
class RestartLessonService(
    private val course: Course,
    private val userTrackerRepository: UserProgressRepository,
    private val flywayLessons: Function<String, Flyway>,
    private val lessonsToInitialize: List<Initializable>,
) {
    @GetMapping(path = ["/service/restartlesson.mvc/{lesson}"])
    @ResponseStatus(value = HttpStatus.OK)
    fun restartLesson(
        @PathVariable("lesson") lessonName: LessonName,
        @CurrentUser user: WebGoatUser,
    ) {
        val lesson = requireNotNull(course.getLessonByName(lessonName)) { "Lesson not found: $lessonName" }
        val username =
            user.username
                ?: throw IllegalStateException("Username cannot be null")

        val userTracker =
            userTrackerRepository.findByUser(username)
                ?: throw IllegalStateException("User progress not found for user: $username")
        userTracker.reset(lesson)
        userTrackerRepository.save(userTracker)

        val flyway = flywayLessons.apply(username)
        flyway.clean()
        flyway.migrate()

        lessonsToInitialize.forEach { it.initialize(user) }
    }

    companion object {
        private val log = LoggerFactory.getLogger(RestartLessonService::class.java)
    }
}
