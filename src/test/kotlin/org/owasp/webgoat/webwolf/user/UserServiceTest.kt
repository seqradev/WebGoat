/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf.user

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.core.userdetails.UsernameNotFoundException

@ExtendWith(MockitoExtension::class)
class UserServiceTest {
    @Mock
    private lateinit var mockUserRepository: UserRepository

    @InjectMocks
    private lateinit var sut: UserService

    @Test
    fun testLoadUserByUsername() {
        val username = "guest"
        val password = "123"
        val user = WebWolfUser(username, password)
        `when`(mockUserRepository.findByUsername(username)).thenReturn(user)

        val webGoatUser = sut.loadUserByUsername(username)

        assertThat(username).isEqualTo(webGoatUser.username)
        assertThat(password).isEqualTo(webGoatUser.password)
    }

    @Test
    fun testLoadUserByUsername_NULL() {
        val username = "guest"

        `when`(mockUserRepository.findByUsername(username)).thenReturn(null)

        assertThatExceptionOfType(UsernameNotFoundException::class.java).isThrownBy {
            sut.loadUserByUsername(username)
        }
    }

    @Test
    fun testAddUser() {
        val username = "guest"
        val password = "guest"

        sut.addUser(username, password)

        verify(mockUserRepository, times(1)).save(any(WebWolfUser::class.java))
    }
}
