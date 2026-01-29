/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.challenges

import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class Flags {
    private val flags: Map<Int, Flag> =
        (1..9).associateWith { Flag(it, UUID.randomUUID().toString()) }

    fun getFlag(flagNumber: Int): Flag? = flags[flagNumber]
}
