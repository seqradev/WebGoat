/*
 * SPDX-FileCopyrightText: Copyright © 2008 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.users

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Version
import org.owasp.webgoat.container.lessons.Lesson

@Entity
class LessonProgress() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long? = null

    var lessonName: String? = null
        protected set

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    private val assignments: MutableSet<AssignmentProgress> = HashSet()

    var numberOfAttempts: Int = 0
        protected set

    @Version
    private var version: Int? = null

    constructor(lesson: Lesson) : this() {
        lessonName = lesson.getId()
        assignments.addAll(lesson.assignments.map { AssignmentProgress(it) })
    }

    private fun getAssignment(name: String): AssignmentProgress? = assignments.find { it.hasSameName(name) }

    /**
     * Mark an assignment as solved
     *
     * @param solvedAssignment the assignment which the user solved
     */
    fun assignmentSolved(solvedAssignment: String) {
        getAssignment(solvedAssignment)?.solved()
    }

    /**
     * @return did they user solved all solvedAssignments for the lesson?
     */
    val isLessonSolved: Boolean
        get() = assignments.all { it.isSolved }

    /** Increase the number attempts to solve the lesson */
    fun incrementAttempts() {
        numberOfAttempts++
    }

    /** Reset the tracker. We do not reset the number of attempts here! */
    fun reset() {
        assignments.forEach { it.reset() }
    }

    /**
     * @return list containing all the assignments solved or not
     */
    fun getLessonOverview(): Map<AssignmentProgress, Boolean> = assignments.associateWith { it.isSolved }

    fun numberOfSolvedAssignments(): Long = assignments.count { it.isSolved }.toLong()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LessonProgress) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
