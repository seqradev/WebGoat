/*
 * SPDX-FileCopyrightText: Copyright © 2015 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.lessons

data class LessonInfoModel(
    val lessonTitle: String,
    val hasSource: Boolean,
    val hasSolution: Boolean,
    val hasPlan: Boolean,
)
