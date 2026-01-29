/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.passwordreset

import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.informationMessage
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SecurityQuestionAssignment(
    private val triedQuestions: TriedQuestions,
) : AssignmentEndpoint {
    @PostMapping("/PasswordReset/SecurityQuestions")
    @ResponseBody
    fun completed(
        @RequestParam question: String,
    ): AttackResult {
        val answer = QUESTIONS[question]
        if (answer != null) {
            triedQuestions.incr(question)
            if (triedQuestions.isComplete()) {
                return success(this).output("<b>$answer</b>").build()
            }
        }
        return informationMessage(this)
            .feedback("password-questions-one-successful")
            .output(answer ?: "Unknown question, please try again...")
            .build()
    }

    companion object {
        private val QUESTIONS =
            mapOf(
                "What is your favorite animal?" to
                    "The answer can easily be guessed and figured out through social media.",
                "In what year was your mother born?" to "Can  be easily guessed.",
                "What was the time you were born?" to
                    "This may first seem like a good question, but you most likely dont know the exact time, so" +
                    " it might be hard to remember.",
                "What is the name of the person you first kissed?" to
                    "Can be figured out through social media, or even guessed by trying the most common" +
                    " names.",
                "What was the house number and street name you lived in as a child?" to
                    "Answer can be figured out through social media, or worse it might be your current" +
                    " address.",
                "In what town or city was your first full time job?" to
                    "In times of LinkedIn and Facebook, the answer can be figured out quite easily.",
                "In what city were you born?" to "Easy to figure out through social media.",
                "What was the last name of your favorite teacher in grade three?" to
                    "Most people would probably not know the answer to that.",
                "What is the name of a college/job you applied to but didn't attend?" to
                    "It might not be easy to remember and an hacker could just try some company's/colleges in" +
                    " your area.",
                "What are the last 5 digits of your drivers license?" to
                    "Is subject to change, and the last digit of your driver license might follow a specific" +
                    " pattern. (For example your birthday).",
                "What was your childhood nickname?" to "Not all people had a nickname.",
                "Who was your childhood hero?" to
                    "Most Heroes we had as a child where quite obvious ones, like Superman for example.",
                "On which wrist do you wear your watch?" to
                    "There are only to possible real answers, so really easy to guess.",
                "What is your favorite color?" to "Can easily be guessed.",
            )
    }
}
