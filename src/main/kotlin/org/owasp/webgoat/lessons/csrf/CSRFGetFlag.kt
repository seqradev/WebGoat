/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.csrf

import jakarta.servlet.http.HttpServletRequest
import org.owasp.webgoat.container.i18n.PluginMessages
import org.owasp.webgoat.container.session.LessonSession
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.util.Random

@RestController
class CSRFGetFlag(
    private val userSessionData: LessonSession,
    private val pluginMessages: PluginMessages,
) {
    @PostMapping(
        path = ["/csrf/basic-get-flag"],
        produces = ["application/json"],
    )
    @ResponseBody
    fun invoke(req: HttpServletRequest): Map<String, Any?> {
        val response = mutableMapOf<String, Any?>()

        val host = req.getHeader("host") ?: "NULL"
        val referer = req.getHeader("referer") ?: "NULL"
        val refererArr = referer.split("/")

        if (referer == "NULL") {
            val random = Random()
            userSessionData.setValue("csrf-get-success", random.nextInt(65536))
            response["success"] = true
            if (req.getParameter("csrf") == "true") {
                response["message"] = pluginMessages.getMessage("csrf-get-null-referer.success")
            } else {
                response["message"] = pluginMessages.getMessage("csrf-get-other-referer.success")
            }
            response["flag"] = userSessionData.getValue("csrf-get-success")
        } else if (refererArr[2] == host) {
            response["success"] = false
            response["message"] = "Appears the request came from the original host"
            response["flag"] = null
        } else {
            val random = Random()
            userSessionData.setValue("csrf-get-success", random.nextInt(65536))
            response["success"] = true
            response["message"] = pluginMessages.getMessage("csrf-get-other-referer.success")
            response["flag"] = userSessionData.getValue("csrf-get-success")
        }

        return response
    }
}
