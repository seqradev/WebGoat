/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.authbypass

class AccountVerificationHelper {
    // this is to aid feedback in the attack process and is not intended to be part of the
    // 'vulnerable' code
    fun didUserLikelylCheat(submittedAnswers: Map<String, String>): Boolean {
        if (submittedAnswers.size == SEC_QUESTION_STORE[VERIFY_USER_ID]?.size) {
            return true
        }

        val storedAnswers = SEC_QUESTION_STORE[VERIFY_USER_ID] ?: return false

        return submittedAnswers.containsKey("secQuestion0") &&
            submittedAnswers["secQuestion0"] == storedAnswers["secQuestion0"] &&
            submittedAnswers.containsKey("secQuestion1") &&
            submittedAnswers["secQuestion1"] == storedAnswers["secQuestion1"]
    }

    // end of cheating check ... the method below is the one of real interest. Can you find the flaw?

    fun verifyAccount(
        userId: Int,
        submittedQuestions: Map<String, String>,
    ): Boolean {
        // short circuit if no questions are submitted
        val storedQuestions = SEC_QUESTION_STORE[VERIFY_USER_ID] ?: return false
        if (submittedQuestions.size != storedQuestions.size) {
            return false
        }

        if (submittedQuestions.containsKey("secQuestion0") &&
            submittedQuestions["secQuestion0"] != storedQuestions["secQuestion0"]
        ) {
            return false
        }

        if (submittedQuestions.containsKey("secQuestion1") &&
            submittedQuestions["secQuestion1"] != storedQuestions["secQuestion1"]
        ) {
            return false
        }

        // else
        return true
    }

    companion object {
        // simulating database storage of verification credentials
        private const val VERIFY_USER_ID = 1223445
        private val USER_SEC_QUESTIONS =
            mapOf(
                "secQuestion0" to "Dr. Watson",
                "secQuestion1" to "Baker Street",
            )

        private val SEC_QUESTION_STORE = mapOf(VERIFY_USER_ID to USER_SEC_QUESTIONS)
    }
}
