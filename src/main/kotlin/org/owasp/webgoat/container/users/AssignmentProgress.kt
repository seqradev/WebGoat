/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.users

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import org.owasp.webgoat.container.lessons.Assignment
import org.springframework.util.Assert

@Entity
class AssignmentProgress(
    @OneToOne(cascade = [CascadeType.ALL])
    val assignment: Assignment? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long? = null

    @Column(name = "solved")
    var isSolved: Boolean = false
        protected set

    fun hasSameName(name: String): Boolean {
        Assert.notNull(name, "Name cannot be null")
        return assignment?.name == name
    }

    fun solved() {
        isSolved = true
    }

    fun reset() {
        isSolved = false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AssignmentProgress) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
