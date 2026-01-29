/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.session

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.owasp.webgoat.container.lessons.Assignment
import org.owasp.webgoat.container.lessons.Lesson
import org.owasp.webgoat.container.users.LessonProgress

class LessonTrackerTest {
    @Test
    fun allAssignmentsSolvedShouldMarkLessonAsComplete() {
        val lesson = mock(Lesson::class.java)
        `when`(lesson.assignments)
            .thenReturn(mutableListOf(Assignment("assignment", "assignment", listOf(""))))
        val lessonTracker = LessonProgress(lesson)
        lessonTracker.assignmentSolved("assignment")

        assertThat(lessonTracker.isLessonSolved).isTrue()
    }

    @Test
    @DisplayName("Given two assignments when only one is solved then lesson is not solved")
    fun noAssignmentsSolvedShouldMarkLessonAsInComplete() {
        val lesson = mock(Lesson::class.java)
        val a1 = Assignment("a1")
        val a2 = Assignment("a2")
        val assignments = mutableListOf(a1, a2)
        `when`(lesson.assignments).thenReturn(assignments)
        val lessonTracker = LessonProgress(lesson)
        lessonTracker.assignmentSolved("a1")

        val lessonOverview = lessonTracker.getLessonOverview()
        assertThat(lessonOverview.values).containsExactlyInAnyOrder(true, false)
    }

    @Test
    fun solvingSameAssignmentShouldNotAddItTwice() {
        val lesson = mock(Lesson::class.java)
        val a1 = Assignment("a1")
        val assignments = mutableListOf(a1)
        `when`(lesson.assignments).thenReturn(assignments)
        val lessonTracker = LessonProgress(lesson)
        lessonTracker.assignmentSolved("a1")
        lessonTracker.assignmentSolved("a1")

        assertThat(lessonTracker.getLessonOverview().size).isEqualTo(1)
    }
}
