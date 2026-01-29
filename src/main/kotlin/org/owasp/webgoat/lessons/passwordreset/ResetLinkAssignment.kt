/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.passwordreset

import org.owasp.webgoat.container.CurrentUsername
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.owasp.webgoat.lessons.passwordreset.resetlink.PasswordChangeForm
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

@RestController
@AssignmentHints(
    "password-reset-hint1",
    "password-reset-hint2",
    "password-reset-hint3",
    "password-reset-hint4",
    "password-reset-hint5",
    "password-reset-hint6",
)
class ResetLinkAssignment : AssignmentEndpoint {
    @PostMapping("/PasswordReset/reset/login")
    @ResponseBody
    fun login(
        @RequestParam password: String,
        @RequestParam email: String,
        @CurrentUsername username: String?,
    ): AttackResult {
        if (TOM_EMAIL == email) {
            val passwordTom = usersToTomPassword.getOrDefault(username, PASSWORD_TOM_9)
            if (passwordTom == PASSWORD_TOM_9) {
                return failed(this).feedback("login_failed").build()
            } else if (passwordTom == password) {
                return success(this).build()
            }
        }
        return failed(this).feedback("login_failed.tom").build()
    }

    @GetMapping("/PasswordReset/reset/reset-password/{link}")
    fun resetPassword(
        @PathVariable(value = "link") link: String,
        model: Model,
    ): ModelAndView {
        val modelAndView = ModelAndView()
        if (resetLinks.contains(link)) {
            val form = PasswordChangeForm()
            form.resetLink = link
            model.addAttribute("form", form)
            modelAndView.addObject("form", form)
            modelAndView.viewName =
                VIEW_FORMATTER.format("password_reset") // Display html page for changing password
        } else {
            modelAndView.viewName = VIEW_FORMATTER.format("password_link_not_found")
        }
        return modelAndView
    }

    @PostMapping("/PasswordReset/reset/change-password")
    fun changePassword(
        @ModelAttribute("form") form: PasswordChangeForm,
        bindingResult: BindingResult,
        @CurrentUsername username: String?,
    ): ModelAndView {
        val modelAndView = ModelAndView()
        if (form.password.isNullOrBlank()) {
            bindingResult.rejectValue("password", "not.empty")
        }
        if (bindingResult.hasErrors()) {
            modelAndView.viewName = VIEW_FORMATTER.format("password_reset")
            return modelAndView
        }
        if (!resetLinks.contains(form.resetLink)) {
            modelAndView.viewName = VIEW_FORMATTER.format("password_link_not_found")
            return modelAndView
        }
        if (checkIfLinkIsFromTom(form.resetLink, username)) {
            form.password?.let { usersToTomPassword[username] = it }
        }
        modelAndView.viewName = VIEW_FORMATTER.format("success")
        return modelAndView
    }

    private fun checkIfLinkIsFromTom(
        resetLinkFromForm: String?,
        username: String?,
    ): Boolean {
        val resetLink = userToTomResetLink.getOrDefault(username, "unknown")
        return resetLink == resetLinkFromForm
    }

    companion object {
        private const val VIEW_FORMATTER = "lessons/passwordreset/templates/%s.html"

        const val PASSWORD_TOM_9: String = "somethingVeryRandomWhichNoOneWillEverTypeInAsPasswordForTom"
        const val TOM_EMAIL: String = "tom@webgoat-cloud.org"

        @JvmField
        val userToTomResetLink: MutableMap<String?, String> = mutableMapOf()

        @JvmField
        val usersToTomPassword: MutableMap<String?, String> = mutableMapOf()

        @JvmField
        val resetLinks: MutableList<String> = mutableListOf()

        const val TEMPLATE: String =
            """Hi, you requested a password reset link, please use this <a target='_blank'
 href='http://%s/WebGoat/PasswordReset/reset/reset-password/%s'>link</a> to reset your
 password.

If you did not request this password change you can ignore this message.
If you have any comments or questions, please do not hesitate to reach us at
 support@webgoat-cloud.org

Kind regards,
Team WebGoat
"""
    }
}
