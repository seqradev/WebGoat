/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.service

import org.owasp.webgoat.container.CurrentUser
import org.owasp.webgoat.container.i18n.Messages
import org.owasp.webgoat.container.users.WebGoatUser
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class SessionService(
    private val restartLessonService: RestartLessonService,
    private val messages: Messages,
) {
    @RequestMapping(path = ["/service/enable-security.mvc"], produces = ["application/json"])
    @ResponseBody
    fun applySecurity(
        @CurrentUser user: WebGoatUser,
    ): String {
        // webSession.toggleSecurity();
        // restartLessonService.restartLesson(user);

        // TODO disabled for now
        // var msg = webSession.isSecurityEnabled() ? "security.enabled" : "security.disabled";
        return messages.getMessage("Not working...") ?: "Not working..."
    }
}
