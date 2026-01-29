/*
 * SPDX-FileCopyrightText: Copyright © 2022 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.lessons

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.session.Course
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.Assert
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

@Configuration
class CourseConfiguration(
    private val lessons: List<Lesson>,
    private val assignments: List<AssignmentEndpoint>,
    @Value("\${server.servlet.context-path}") contextPath: String,
) {
    private val contextPath: String = if (contextPath == "/") "" else contextPath

    private fun attachToLessonInParentPackage(
        assignmentEndpoint: AssignmentEndpoint,
        packageName: String,
    ) {
        if (packageName == "org.owasp.webgoat.lessons") {
            throw IllegalStateException(
                "No lesson found for assignment: '${assignmentEndpoint.javaClass.simpleName}'",
            )
        }
        lessons
            .firstOrNull { it.javaClass.packageName == packageName }
            ?.let { it.addAssignment(toAssignment(assignmentEndpoint)) }
            ?: attachToLessonInParentPackage(
                assignmentEndpoint,
                packageName.substring(0, packageName.lastIndexOf(".")),
            )
    }

    /**
     * For each assignment endpoint, find the lesson in the same package or if not found, find the
     * lesson in the parent package
     */
    private fun attachToLesson(assignmentEndpoint: AssignmentEndpoint) {
        val assignmentPackageName = assignmentEndpoint.javaClass.packageName
        lessons
            .firstOrNull { it.javaClass.packageName == assignmentPackageName }
            ?.let { it.addAssignment(toAssignment(assignmentEndpoint)) }
            ?: attachToLessonInParentPackage(
                assignmentEndpoint,
                assignmentPackageName.substring(0, assignmentPackageName.lastIndexOf(".")),
            )
    }

    private fun toAssignment(endpoint: AssignmentEndpoint): Assignment =
        Assignment(
            endpoint.javaClass.simpleName,
            getPath(endpoint.javaClass),
            getHints(endpoint.javaClass),
        )

    @Bean
    fun course(): Course {
        assignments.forEach { attachToLesson(it) }

        // Check if all assignments are attached to a lesson
        val assignmentsAttachedToLessons = lessons.sumOf { it.assignments.size }
        Assert.isTrue(
            assignmentsAttachedToLessons == assignments.size,
            "Not all assignments are attached to a lesson, please check the configuration. The" +
                " following assignments are not attached to any lesson: ${findDiff()}",
        )
        return Course(lessons)
    }

    private fun findDiff(): List<String> {
        val matchedToLessons =
            lessons.flatMap { it.assignments }.map { it.name }
        val allAssignments = assignments.map { it.javaClass.simpleName }

        return allAssignments.toMutableList().apply { removeAll(matchedToLessons) }
    }

    private fun getPath(e: Class<out AssignmentEndpoint>): String =
        e.methods
            .filter { methodReturnTypeIsOfTypeAttackResult(it) }
            .firstNotNullOfOrNull { getMapping(it) }
            ?.let { contextPath + it }
            ?: throw IllegalStateException(
                "Assignment endpoint: $e has no mapping like @GetMapping/@PostMapping etc," +
                    "with return type 'AttackResult' or 'ResponseEntity<AttackResult>' " +
                    "please consider adding one",
            )

    private fun methodReturnTypeIsOfTypeAttackResult(m: Method): Boolean {
        if (m.returnType == AttackResult::class.java) {
            return true
        }
        val genericType = m.genericReturnType
        if (genericType is ParameterizedType) {
            return genericType.actualTypeArguments[0] == AttackResult::class.java
        }
        return false
    }

    private fun getMapping(m: Method): String? {
        var paths: Array<String>? = null
        // Find the path, either it is @GetMapping("/attack") of GetMapping(path = "/attack") both are
        // valid, we need to consider both
        when {
            m.getAnnotation(RequestMapping::class.java) != null -> {
                val annotation = m.getAnnotation(RequestMapping::class.java)
                paths = annotation.value + annotation.path
            }
            m.getAnnotation(PostMapping::class.java) != null -> {
                val annotation = m.getAnnotation(PostMapping::class.java)
                paths = annotation.value + annotation.path
            }
            m.getAnnotation(GetMapping::class.java) != null -> {
                val annotation = m.getAnnotation(GetMapping::class.java)
                paths = annotation.value + annotation.path
            }
            m.getAnnotation(PutMapping::class.java) != null -> {
                val annotation = m.getAnnotation(PutMapping::class.java)
                paths = annotation.value + annotation.path
            }
        }
        return if (paths == null) {
            null
        } else {
            paths.firstOrNull { it.isNotEmpty() } ?: ""
        }
    }

    private fun getHints(e: Class<out AssignmentEndpoint>): List<String> =
        if (e.isAnnotationPresent(AssignmentHints::class.java)) {
            e.getAnnotationsByType(AssignmentHints::class.java)[0].value.toList()
        } else {
            emptyList()
        }
}
