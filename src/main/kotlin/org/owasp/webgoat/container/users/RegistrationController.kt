/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.users

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import java.util.UUID

@Controller
class RegistrationController(
    private val userValidator: UserValidator,
    private val userService: UserService,
) {
    private val log = LoggerFactory.getLogger(RegistrationController::class.java)

    @GetMapping("/registration")
    fun showForm(userForm: UserForm): String = "registration"

    @PostMapping("/register.mvc")
    fun registration(
        @ModelAttribute("userForm") @Valid userForm: UserForm,
        bindingResult: BindingResult,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): String {
        userValidator.validate(userForm, bindingResult)

        if (bindingResult.hasErrors()) {
            return "registration"
        }

        // Logout current user if any
        SecurityContextHolder.getContext().authentication?.let { auth ->
            SecurityContextLogoutHandler().logout(request, response, auth)
        }

        val username = requireNotNull(userForm.username) { "Username is required" }
        val password = requireNotNull(userForm.password) { "Password is required" }
        userService.addUser(username, password)
        request.login(username, password)

        return "redirect:/attack"
    }

    @GetMapping("/login-oauth.mvc")
    fun registrationOAUTH(
        authentication: Authentication,
        request: HttpServletRequest,
    ): String {
        log.info("register oauth user in database")
        userService.addUser(authentication.name, UUID.randomUUID().toString())
        return "redirect:/welcome.mvc"
    }
}
