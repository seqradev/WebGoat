/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.webwolfintroduction

import org.owasp.webgoat.container.CurrentUsername
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

@RestController
class LandingAssignment(
    @Value("\${webwolf.landingpage.url}") private val landingPageUrl: String,
) : AssignmentEndpoint {
    @PostMapping("/WebWolf/landing")
    @ResponseBody
    fun click(
        uniqueCode: String,
        @CurrentUsername username: String?,
    ): AttackResult =
        if (username?.reversed() == uniqueCode) {
            success(this).build()
        } else {
            failed(this).feedback("webwolf.landing_wrong").build()
        }

    @GetMapping("/WebWolf/landing/password-reset")
    fun openPasswordReset(
        @CurrentUsername username: String?,
    ): ModelAndView {
        val modelAndView = ModelAndView()
        modelAndView.addObject("webwolfLandingPageUrl", landingPageUrl.replace("//landing", "/landing"))
        modelAndView.addObject("uniqueCode", username?.reversed())
        modelAndView.viewName = "lessons/webwolfintroduction/templates/webwolfPasswordReset.html"
        return modelAndView
    }
}
