/*
 * SPDX-FileCopyrightText: Copyright © 2022 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.lessons

/**
 * Wrapper class for the name of a lesson. This class is used to ensure that the lesson name is not
 * null and does not contain the ".lesson" suffix. The front-end passes the lesson name as a string
 * to the back-end, which then creates a new LessonName object with the lesson name as a parameter.
 * The constructor of the LessonName class checks if the lesson name is null and removes the
 * ".lesson" suffix if it is present.
 */
class LessonName(
    lessonName: String,
) {
    private val normalizedName: String = lessonName.removeSuffix(".lesson")

    fun lessonName(): String = normalizedName

    override fun toString(): String = normalizedName

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LessonName) return false
        return normalizedName == other.normalizedName
    }

    override fun hashCode(): Int = normalizedName.hashCode()
}
