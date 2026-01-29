/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.service

import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.owasp.webgoat.container.lessons.Category
import org.owasp.webgoat.container.lessons.Lesson
import org.owasp.webgoat.container.session.Course
import org.owasp.webgoat.container.users.LessonProgress
import org.owasp.webgoat.container.users.UserProgress
import org.owasp.webgoat.container.users.UserProgressRepository
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

@ExtendWith(MockitoExtension::class)
class LessonMenuServiceTest {
    @Mock
    private lateinit var lessonTracker: LessonProgress

    @Mock
    private lateinit var course: Course

    @Mock
    private lateinit var userTracker: UserProgress

    @Mock
    private lateinit var userTrackerRepository: UserProgressRepository

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        lenient().`when`(lessonTracker.isLessonSolved).thenReturn(false)
        this.mockMvc =
            standaloneSetup(
                LessonMenuService(
                    course,
                    userTrackerRepository,
                    listOf("none"),
                    listOf("none"),
                ),
            ).build()
    }

    @Test
    fun lessonsShouldBeOrdered() {
        val l1 = mock(Lesson::class.java)
        val l2 = mock(Lesson::class.java)
        `when`(l1.getTitle()).thenReturn("ZA")
        `when`(l2.getTitle()).thenReturn("AA")
        `when`(lessonTracker.isLessonSolved).thenReturn(false)
        `when`(course.getLessons(any())).thenReturn(listOf(l1, l2))
        `when`(course.categories).thenReturn(listOf(Category.A1))
        `when`(userTracker.getLessonProgress(any())).thenReturn(lessonTracker)
        `when`(userTrackerRepository.findByUser(anyOrNull())).thenReturn(userTracker)

        mockMvc
            .perform(MockMvcRequestBuilders.get(LessonMenuService.URL_LESSONMENU_MVC))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].children[0].name", `is`("AA")))
            .andExpect(jsonPath("$[0].children[1].name", `is`("ZA")))
    }

    @Test
    fun lessonCompleted() {
        val l1 = mock(Lesson::class.java)
        `when`(l1.getTitle()).thenReturn("ZA")
        `when`(lessonTracker.isLessonSolved).thenReturn(true)
        `when`(course.getLessons(any())).thenReturn(listOf(l1))
        `when`(course.categories).thenReturn(listOf(Category.A1))
        `when`(userTracker.getLessonProgress(any())).thenReturn(lessonTracker)
        `when`(userTrackerRepository.findByUser(anyOrNull())).thenReturn(userTracker)

        mockMvc
            .perform(MockMvcRequestBuilders.get(LessonMenuService.URL_LESSONMENU_MVC))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].children[0].complete", `is`(true)))
    }
}
