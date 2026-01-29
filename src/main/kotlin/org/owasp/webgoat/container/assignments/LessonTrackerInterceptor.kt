/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.assignments

import org.owasp.webgoat.container.session.Course
import org.owasp.webgoat.container.users.UserProgress
import org.owasp.webgoat.container.users.UserProgressRepository
import org.owasp.webgoat.container.users.WebGoatUser
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@RestControllerAdvice
class LessonTrackerInterceptor(
    private val course: Course,
    private val userProgressRepository: UserProgressRepository,
) : ResponseBodyAdvice<Any> {
    override fun supports(
        methodParameter: MethodParameter,
        clazz: Class<out HttpMessageConverter<*>>,
    ): Boolean = true

    override fun beforeBodyWrite(
        body: Any?,
        methodParameter: MethodParameter,
        mediaType: MediaType,
        aClass: Class<out HttpMessageConverter<*>>,
        serverHttpRequest: ServerHttpRequest,
        serverHttpResponse: ServerHttpResponse,
    ): Any? {
        if (body is AttackResult) {
            trackProgress(body)
        }
        return body
    }

    private fun trackProgress(attackResult: AttackResult) {
        val user = SecurityContextHolder.getContext().authentication.principal as WebGoatUser
        val username = realUsername(user)

        val userProgress = userProgressRepository.findByUser(username) ?: UserProgress(username)
        val assignmentName = requireNotNull(attackResult.assignment) { "Assignment name cannot be null" }
        val lesson =
            requireNotNull(course.getLessonByAssignment(assignmentName)) {
                "Lesson not found for assignment $assignmentName"
            }

        if (attackResult.assignmentSolved()) {
            userProgress.assignmentSolved(lesson, assignmentName)
        } else {
            userProgress.assignmentFailed(lesson)
        }
        userProgressRepository.save(userProgress)
    }

    private fun realUsername(user: WebGoatUser): String =
        // maybe we shouldn't hard code this with just csrf- prefix for now it works
        user.username?.removePrefix("csrf-") ?: ""
}
