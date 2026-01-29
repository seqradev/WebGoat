/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.report

import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.owasp.webgoat.container.i18n.PluginMessages
import org.owasp.webgoat.container.lessons.Lesson
import org.owasp.webgoat.container.session.Course
import org.owasp.webgoat.container.users.LessonProgress
import org.owasp.webgoat.container.users.UserProgress
import org.owasp.webgoat.container.users.UserProgressRepository
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

// TODO: Rewrite this as end-to-end test this mocks too many classes
@ExtendWith(MockitoExtension::class)
class ReportCardControllerTest {
    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var course: Course

    @Mock
    private lateinit var userTracker: UserProgress

    @Mock
    private lateinit var lesson: Lesson

    @Mock
    private lateinit var lessonTracker: LessonProgress

    @Mock
    private lateinit var userTrackerRepository: UserProgressRepository

    @Mock
    private lateinit var pluginMessages: PluginMessages

    @BeforeEach
    fun setup() {
        this.mockMvc =
            standaloneSetup(ReportCardController(userTrackerRepository, course, pluginMessages))
                .build()
        `when`(pluginMessages.getMessage(anyString())).thenReturn("Test")
    }

    @Test
    @WithMockUser(username = "guest", password = "guest")
    fun withLessons() {
        `when`(lesson.getTitle()).thenReturn("Test")
        `when`(course.totalOfLessons).thenReturn(1)
        `when`(course.totalOfAssignments).thenReturn(10)
        `when`(course.lessons).thenAnswer { listOf(lesson) }
        `when`(userTrackerRepository.findByUser(anyOrNull())).thenReturn(userTracker)
        `when`(userTracker.getLessonProgress(any())).thenReturn(lessonTracker)
        mockMvc
            .perform(MockMvcRequestBuilders.get("/service/reportcard.mvc"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalNumberOfLessons", `is`(1)))
            .andExpect(jsonPath("$.numberOfAssignmentsSolved", `is`(0)))
            .andExpect(jsonPath("$.totalNumberOfAssignments", `is`(10)))
            .andExpect(jsonPath("$.lessonStatistics[0].name", `is`("Test")))
            .andExpect(jsonPath("$.numberOfAssignmentsSolved", `is`(0)))
    }
}
