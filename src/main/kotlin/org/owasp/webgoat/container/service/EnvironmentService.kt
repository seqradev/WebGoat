/*
 * SPDX-FileCopyrightText: Copyright © 2022 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.service

import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController("/environment")
class EnvironmentService(
    private val context: ApplicationContext,
) {
    @GetMapping("/server-directory")
    fun homeDirectory(): String? = context.environment.getProperty("webgoat.server.directory")
}
