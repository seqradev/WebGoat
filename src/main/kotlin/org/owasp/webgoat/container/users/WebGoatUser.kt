/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.users

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Transient
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails

@Entity
class WebGoatUser : UserDetails {
    @Id
    private var username: String? = null
    private var password: String? = null
    private var role: String = ROLE_USER

    @Transient
    private var user: User? = null

    protected constructor()

    constructor(username: String, password: String) : this(username, password, ROLE_USER)

    constructor(username: String, password: String, role: String) {
        this.username = username
        this.password = password
        this.role = role
        createUser()
    }

    fun createUser() {
        this.user = User(username, password, authorities)
    }

    override fun getAuthorities(): Collection<GrantedAuthority> = setOf(SimpleGrantedAuthority(role))

    fun getRole(): String = this.role

    override fun getUsername(): String? = this.username

    override fun getPassword(): String? = this.password

    override fun isAccountNonExpired(): Boolean = user?.isAccountNonExpired ?: true

    override fun isAccountNonLocked(): Boolean = user?.isAccountNonLocked ?: true

    override fun isCredentialsNonExpired(): Boolean = user?.isCredentialsNonExpired ?: true

    override fun isEnabled(): Boolean = user?.isEnabled ?: true

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WebGoatUser) return false
        return user == other.user
    }

    override fun hashCode(): Int = user?.hashCode() ?: 0

    companion object {
        const val ROLE_USER = "WEBGOAT_USER"
        const val ROLE_ADMIN = "WEBGOAT_ADMIN"
    }
}
