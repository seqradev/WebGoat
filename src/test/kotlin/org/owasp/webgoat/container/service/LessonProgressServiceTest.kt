/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.service

import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.owasp.webgoat.container.lessons.Assignment
import org.owasp.webgoat.container.lessons.Lesson
import org.owasp.webgoat.container.session.Course
import org.owasp.webgoat.container.users.AssignmentProgress
import org.owasp.webgoat.container.users.LessonProgress
import org.owasp.webgoat.container.users.UserProgress
import org.owasp.webgoat.container.users.UserProgressRepository
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockitoExtension::class)
class LessonProgressServiceTest {
    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var lesson: Lesson

    @Mock
    private lateinit var userProgress: UserProgress

    @Mock
    private lateinit var lessonTracker: LessonProgress

    @Mock
    private lateinit var userProgressRepository: UserProgressRepository

    @Mock
    private lateinit var course: Course

    @BeforeEach
    fun setup() {
        val assignment = Assignment("test", "test", listOf())
        val assignmentProgress = AssignmentProgress(assignment)
        `when`(userProgressRepository.findByUser(anyOrNull())).thenReturn(userProgress)
        `when`(userProgress.getLessonProgress(any())).thenReturn(lessonTracker)
        `when`(course.getLessonByName(any())).thenReturn(lesson)
        `when`(lessonTracker.getLessonOverview()).thenReturn(mapOf(assignmentProgress to true))
        this.mockMvc =
            MockMvcBuilders
                .standaloneSetup(LessonProgressService(userProgressRepository, course))
                .build()
    }

    @Test
    fun jsonLessonOverview() {
        this.mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/service/lessonoverview.mvc/test.lesson")
                    .accept(MediaType.APPLICATION_JSON_VALUE),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].assignment.name", `is`("test")))
            .andExpect(jsonPath("$[0].solved", `is`(true)))
    }
}
