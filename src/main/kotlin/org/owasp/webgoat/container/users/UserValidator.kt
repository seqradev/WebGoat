/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.users

import org.springframework.stereotype.Component
import org.springframework.validation.Errors
import org.springframework.validation.Validator

@Component
class UserValidator(
    private val userRepository: UserRepository,
) : Validator {
    override fun supports(clazz: Class<*>): Boolean = UserForm::class.java == clazz

    override fun validate(
        target: Any,
        errors: Errors,
    ) {
        val userForm = target as UserForm

        if (userRepository.findByUsername(userForm.username ?: "") != null) {
            errors.rejectValue("username", "username.duplicate")
        }

        if (userForm.matchingPassword != userForm.password) {
            errors.rejectValue("matchingPassword", "password.diff")
        }
    }
}
