/*
 * SPDX-FileCopyrightText: Copyright © 2022 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.lessons

class LessonMenuItem {
    var name: String? = null
    var type: LessonMenuItemType? = null
    var children: MutableList<LessonMenuItem> = mutableListOf()
    var isComplete: Boolean = false
    var link: String? = null
    var ranking: Int = 0

    fun addChild(child: LessonMenuItem) = children.add(child)

    override fun toString(): String = "Name: $name | Type: $type | "
}
