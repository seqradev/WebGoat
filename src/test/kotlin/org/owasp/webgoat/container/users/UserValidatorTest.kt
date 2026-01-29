/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.users

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.validation.BeanPropertyBindingResult

@ExtendWith(MockitoExtension::class)
class UserValidatorTest {
    @Mock
    private lateinit var userRepository: UserRepository

    @Test
    fun passwordsShouldMatch() {
        val userForm =
            UserForm().apply {
                agree = "true"
                username = "test1234"
                password = "test1234"
                matchingPassword = "test1234"
            }
        val errors = BeanPropertyBindingResult(userForm, "userForm")
        UserValidator(userRepository).validate(userForm, errors)
        assertThat(errors.hasErrors()).isFalse()
    }

    @Test
    fun shouldGiveErrorWhenPasswordsDoNotMatch() {
        val userForm =
            UserForm().apply {
                agree = "true"
                username = "test1234"
                password = "test12345"
                matchingPassword = "test1234"
            }
        val errors = BeanPropertyBindingResult(userForm, "userForm")
        UserValidator(userRepository).validate(userForm, errors)
        assertThat(errors.hasErrors()).isTrue()
        assertThat(errors.getFieldError("matchingPassword")?.code).isEqualTo("password.diff")
    }

    @Test
    fun shouldGiveErrorWhenUserAlreadyExists() {
        val userForm =
            UserForm().apply {
                agree = "true"
                username = "test12345"
                password = "test12345"
                matchingPassword = "test12345"
            }
        `when`(userRepository.findByUsername(anyString()))
            .thenReturn(WebGoatUser("test1245", "password"))
        val errors = BeanPropertyBindingResult(userForm, "userForm")
        UserValidator(userRepository).validate(userForm, errors)
        assertThat(errors.hasErrors()).isTrue()
        assertThat(errors.getFieldError("username")?.code).isEqualTo("username.duplicate")
    }
}
