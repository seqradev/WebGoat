/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.lessons

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Transient

@Entity
class Assignment private constructor() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    var name: String? = null
        protected set

    var path: String? = null
        protected set

    @Transient
    var hints: List<String> = emptyList()
        protected set

    constructor(name: String) : this(name, name, emptyList())

    constructor(name: String, path: String, hints: List<String>) : this() {
        if (path == "" || path == "/" || path == "/WebGoat/") {
            throw IllegalStateException(
                "The path of assignment '$name' overrides WebGoat endpoints, " +
                    "please choose a path within the scope of the lesson",
            )
        }
        this.name = name
        this.path = path
        this.hints = hints
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Assignment) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
