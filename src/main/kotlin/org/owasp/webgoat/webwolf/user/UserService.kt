/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf.user

import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): WebWolfUser {
        val webGoatUser =
            userRepository.findByUsername(username)
                ?: throw UsernameNotFoundException("User not found")
        webGoatUser.createUser()
        return webGoatUser
    }

    fun addUser(
        username: String,
        password: String,
    ) {
        userRepository.save(WebWolfUser(username, password))
    }
}
