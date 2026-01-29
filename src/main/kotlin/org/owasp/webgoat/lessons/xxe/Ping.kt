/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.xxe

import org.owasp.webgoat.container.CurrentUsername
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.io.File
import java.io.FileNotFoundException
import java.io.PrintWriter

class Ping(
    @Value("\${webgoat.user.directory}") private val webGoatHomeDirectory: String,
) {
    private val log = LoggerFactory.getLogger(Ping::class.java)

    @GetMapping
    @ResponseBody
    fun logRequest(
        @RequestHeader("User-Agent") userAgent: String,
        @RequestParam(required = false) text: String?,
        @CurrentUsername username: String?,
    ): String {
        val logLine = "GET $userAgent $text"
        log.debug(logLine)
        val logFile = File(webGoatHomeDirectory, "/XXE/log$username.txt")
        try {
            PrintWriter(logFile).use { pw ->
                pw.println(logLine)
            }
        } catch (e: FileNotFoundException) {
            log.error("Error occurred while writing the logfile", e)
        }
        return ""
    }
}
