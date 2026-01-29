/*
 * SPDX-FileCopyrightText: Copyright © 2008 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.users

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import org.owasp.webgoat.container.lessons.Lesson
import org.slf4j.LoggerFactory

@Entity
class UserProgress() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long? = null

    @Column(name = "username")
    private var user: String? = null

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    private val lessonProgress: MutableSet<LessonProgress> = HashSet()

    constructor(user: String) : this() {
        this.user = user
    }

    /**
     * Returns an existing lesson progress or create a new one based on the lesson
     *
     * @param lesson the lesson
     * @return a lesson tracker created if not already present
     */
    fun getLessonProgress(lesson: Lesson): LessonProgress =
        lessonProgress.find { it.lessonName == lesson.getId() }
            ?: LessonProgress(lesson).also { lessonProgress.add(it) }

    fun assignmentSolved(
        lesson: Lesson,
        assignmentName: String,
    ) {
        val progress = getLessonProgress(lesson)
        progress.incrementAttempts()
        progress.assignmentSolved(assignmentName)
    }

    fun assignmentFailed(lesson: Lesson) {
        val progress = getLessonProgress(lesson)
        progress.incrementAttempts()
    }

    fun reset(al: Lesson) {
        val progress = getLessonProgress(al)
        progress.reset()
    }

    fun numberOfLessonsSolved(): Long = lessonProgress.count { it.isLessonSolved }.toLong()

    fun numberOfAssignmentsSolved(): Long = lessonProgress.sumOf { it.numberOfSolvedAssignments() }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserProgress) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    companion object {
        private val log = LoggerFactory.getLogger(UserProgress::class.java)
    }
}
