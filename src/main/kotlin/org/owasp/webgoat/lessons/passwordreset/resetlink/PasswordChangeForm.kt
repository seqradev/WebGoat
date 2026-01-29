/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.passwordreset.resetlink

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

class PasswordChangeForm {
    @field:NotNull
    @field:Size(min = 6, max = 10)
    var password: String? = null

    var resetLink: String? = null
}
