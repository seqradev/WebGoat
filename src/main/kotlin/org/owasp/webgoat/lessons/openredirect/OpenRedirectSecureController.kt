/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

/**
 * Demonstrates a safer redirect pattern using server-side id mapping instead of raw user URLs.
 * Not part of the scored assignments – just for experimentation.
 */
@Controller
class OpenRedirectSecureController {
    @GetMapping("/OpenRedirect/safe")
    fun safe(
        @RequestParam(name = "destId", defaultValue = "1") destId: Int,
    ): ModelAndView = ModelAndView("redirect:${DESTINATIONS.getOrDefault(destId, "/welcome.mvc")}")

    companion object {
        // Use only confirmed existing internal endpoints within WebGoat
        // 1 -> welcome page, 2 -> login page, 3 -> logout endpoint
        private val DESTINATIONS =
            mapOf(
                1 to "/welcome.mvc",
                2 to "/login",
                3 to "/logout",
            )
    }
}
