/*
 * SPDX-FileCopyrightText: Copyright © 2008 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.session

import org.owasp.webgoat.container.lessons.Category
import org.owasp.webgoat.container.lessons.Lesson
import org.owasp.webgoat.container.lessons.LessonName

class Course(
    var lessons: List<Lesson>,
) {
    /**
     * Gets the categories attribute of the Course object
     *
     * @return The categories value
     */
    val categories: List<Category>
        get() = lessons.map { it.getCategory() }.distinct().sorted()

    /**
     * Gets the firstLesson attribute of the Course object
     *
     * @return The firstLesson value
     */
    val firstLesson: Lesson
        get() {
            // Category 0 is the admin function. We want the first real category
            // to be returned. This is normally the General category and the Http Basics lesson
            return getLessons(categories[0])[0]
        }

    val totalOfLessons: Int
        get() = lessons.size

    val totalOfAssignments: Int
        get() = lessons.sumOf { it.assignments.size }

    /**
     * Getter for the field lessons.
     *
     * @param category a [org.owasp.webgoat.container.lessons.Category] object.
     * @return a [java.util.List] object.
     */
    fun getLessons(category: Category): List<Lesson> = lessons.filter { it.getCategory() == category }

    fun getLessonByName(lessonName: LessonName): Lesson? = lessons.firstOrNull { it.getName() == lessonName }

    fun getLessonByAssignment(assignmentName: String): Lesson? =
        lessons.firstOrNull { lesson ->
            lesson.assignments.any { it.name == assignmentName }
        }
}
