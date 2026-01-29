/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.users

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserForm(
    @field:NotNull
    @field:Size(min = 6, max = 45)
    @field:Pattern(regexp = "[a-z0-9-]*", message = "can only contain lowercase letters, digits, and -")
    var username: String? = null,
    @field:NotNull
    @field:Size(min = 6, max = 10)
    var password: String? = null,
    @field:NotNull
    @field:Size(min = 6, max = 10)
    var matchingPassword: String? = null,
    @field:NotNull
    var agree: String? = null,
)
