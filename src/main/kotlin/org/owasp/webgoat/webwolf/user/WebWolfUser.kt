/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf.user

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "WEB_GOAT_USER")
class WebWolfUser : UserDetails {
    @Id
    private var username: String? = null

    private var password: String? = null

    @Transient
    private var user: User? = null

    protected constructor()

    constructor(username: String, password: String) {
        this.username = username
        this.password = password
        createUser()
    }

    fun createUser() {
        this.user = User(username, password, authorities)
    }

    override fun getAuthorities(): Collection<GrantedAuthority> = emptyList()

    override fun getUsername(): String? = username

    override fun getPassword(): String? = password

    override fun isAccountNonExpired(): Boolean = user?.isAccountNonExpired ?: true

    override fun isAccountNonLocked(): Boolean = user?.isAccountNonLocked ?: true

    override fun isCredentialsNonExpired(): Boolean = user?.isCredentialsNonExpired ?: true

    override fun isEnabled(): Boolean = user?.isEnabled ?: true
}
