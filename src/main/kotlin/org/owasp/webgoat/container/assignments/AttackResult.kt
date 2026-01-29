/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.assignments

import org.apache.commons.text.StringEscapeUtils.escapeJson
import org.owasp.webgoat.container.i18n.PluginMessages

class AttackResult(
    @get:JvmName("isLessonCompleted")
    val lessonCompleted: Boolean,
    val feedback: String?,
    val feedbackArgs: Array<Any?>?,
    val output: String?,
    val outputArgs: Array<Any?>?,
    val assignment: String?,
    @get:JvmName("isAttemptWasMade")
    val attemptWasMade: Boolean,
) {
    fun assignmentSolved(): Boolean = lessonCompleted

    fun apply(pluginMessages: PluginMessages): AttackResult =
        AttackResult(
            lessonCompleted = lessonCompleted,
            feedback = escapeJson(pluginMessages.getMessage(feedback, feedback, feedbackArgs)),
            feedbackArgs = null,
            output = escapeJson(pluginMessages.getMessage(output, output, outputArgs)),
            outputArgs = null,
            assignment = assignment,
            attemptWasMade = attemptWasMade,
        )
}
