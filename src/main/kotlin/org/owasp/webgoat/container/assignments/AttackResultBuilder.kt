/*
 * SPDX-FileCopyrightText: Copyright © 2024 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.assignments

class AttackResultBuilder {
    private var assignmentCompleted: Boolean = false
    private var feedbackArgs: Array<Any?>? = null
    private var feedbackResourceBundleKey: String? = null
    private var output: String? = null
    private var outputArgs: Array<Any?>? = null
    private var assignment: AssignmentEndpoint? = null
    private var attemptWasMade: Boolean = false

    fun assignmentCompleted(lessonCompleted: Boolean) =
        apply {
            this.assignmentCompleted = lessonCompleted
        }

    fun feedbackArgs(vararg args: Any?) =
        apply {
            this.feedbackArgs = arrayOf(*args)
        }

    fun feedback(resourceBundleKey: String?) =
        apply {
            this.feedbackResourceBundleKey = resourceBundleKey
        }

    fun output(output: String?) =
        apply {
            this.output = output
        }

    fun outputArgs(vararg args: Any?) =
        apply {
            this.outputArgs = arrayOf(*args)
        }

    fun attemptWasMade() =
        apply {
            this.attemptWasMade = true
        }

    fun build(): AttackResult =
        AttackResult(
            lessonCompleted = assignmentCompleted,
            feedback = feedbackResourceBundleKey,
            feedbackArgs = feedbackArgs,
            output = output,
            outputArgs = outputArgs,
            assignment = assignment?.javaClass?.simpleName,
            attemptWasMade = attemptWasMade,
        )

    fun assignment(assignment: AssignmentEndpoint) =
        apply {
            this.assignment = assignment
        }

    companion object {
        /**
         * Convenience method for create a successful result:
         *
         * - Assignment is set to solved
         * - Feedback message is set to 'assignment.solved'
         *
         * Of course, you can overwrite these values in a specific lesson
         *
         * @param assignment the assignment that was solved
         * @return a builder for creating a result from a lesson
         */
        @JvmStatic
        fun success(assignment: AssignmentEndpoint): AttackResultBuilder =
            AttackResultBuilder()
                .assignmentCompleted(true)
                .attemptWasMade()
                .feedback("assignment.solved")
                .assignment(assignment)

        /**
         * Convenience method for create a failed result:
         *
         * - Assignment is set to not solved
         * - Feedback message is set to 'assignment.not.solved'
         *
         * Of course, you can overwrite these values in a specific lesson
         *
         * @param assignment the assignment that was not solved
         * @return a builder for creating a result from a lesson
         */
        @JvmStatic
        fun failed(assignment: AssignmentEndpoint): AttackResultBuilder =
            AttackResultBuilder()
                .assignmentCompleted(false)
                .attemptWasMade()
                .feedback("assignment.not.solved")
                .assignment(assignment)

        @JvmStatic
        fun informationMessage(assignment: AssignmentEndpoint): AttackResultBuilder =
            AttackResultBuilder()
                .assignmentCompleted(false)
                .assignment(assignment)
    }
}
