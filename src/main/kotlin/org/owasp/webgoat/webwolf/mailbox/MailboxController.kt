/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf.mailbox

import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

@RestController
class MailboxController(
    private val mailboxRepository: MailboxRepository,
) {
    @GetMapping("/mail")
    fun mail(
        authentication: Authentication?,
        model: Model,
    ): ModelAndView {
        val username = authentication?.name ?: "anonymous"
        val emails = mailboxRepository.findByRecipientOrderByTimeDesc(username)
        model.addAttribute("username", username)
        return ModelAndView().apply {
            viewName = "mailbox"
            if (emails.isNotEmpty()) {
                addObject("total", emails.size)
                addObject("emails", emails)
            }
        }
    }

    @PostMapping("/mail")
    @ResponseStatus(HttpStatus.CREATED)
    fun sendEmail(
        @RequestBody email: Email,
    ) {
        mailboxRepository.save(email)
    }

    @DeleteMapping("/mail")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun deleteAllMail() = mailboxRepository.deleteAll()
}
