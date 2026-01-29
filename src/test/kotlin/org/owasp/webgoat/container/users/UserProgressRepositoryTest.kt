/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.users

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.owasp.webgoat.container.lessons.Assignment
import org.owasp.webgoat.container.lessons.Category
import org.owasp.webgoat.container.lessons.Lesson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("webgoat-test")
class UserProgressRepositoryTest {
    private class TestLesson : Lesson() {
        override fun getDefaultCategory(): Category = Category.CLIENT_SIDE

        override fun getTitle(): String = "test"

        init {
            assignments =
                mutableListOf(
                    Assignment("test1", "test1", emptyList()),
                    Assignment("test2", "test2", emptyList()),
                )
        }
    }

    @Autowired
    private lateinit var userProgressRepository: UserProgressRepository

    @Test
    fun saveUserTracker() {
        var userProgress = UserProgress(USER)
        userProgressRepository.save(userProgress)

        userProgress = requireNotNull(userProgressRepository.findByUser(USER))

        assertThat(userProgress.getLessonProgress(TestLesson())).isNotNull()
    }

    @Test
    fun solvedAssignmentsShouldBeSaved() {
        var userProgress = UserProgress(USER)
        val lesson = TestLesson()
        userProgress.getLessonProgress(lesson)
        userProgress.assignmentFailed(lesson)
        userProgress.assignmentFailed(lesson)
        userProgress.assignmentSolved(lesson, "test1")
        userProgress.assignmentSolved(lesson, "test2")
        userProgressRepository.saveAndFlush(userProgress)

        userProgress = requireNotNull(userProgressRepository.findByUser(USER))

        assertThat(userProgress.numberOfAssignmentsSolved()).isEqualTo(2)
    }

    @Test
    fun saveAndLoadShouldHaveCorrectNumberOfAttempts() {
        var userProgress = UserProgress(USER)
        val lesson = TestLesson()
        userProgress.getLessonProgress(lesson)
        userProgress.assignmentFailed(lesson)
        userProgress.assignmentFailed(lesson)
        userProgressRepository.saveAndFlush(userProgress)

        userProgress = requireNotNull(userProgressRepository.findByUser(USER))
        userProgress.assignmentFailed(lesson)
        userProgress.assignmentFailed(lesson)
        userProgressRepository.saveAndFlush(userProgress)

        assertThat(userProgress.getLessonProgress(lesson).numberOfAttempts).isEqualTo(4)
    }

    companion object {
        private const val USER = "user"
    }
}
