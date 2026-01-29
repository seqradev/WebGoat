/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.service

import org.owasp.webgoat.container.i18n.Messages
import org.owasp.webgoat.container.i18n.PluginMessages
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.util.Properties

@RestController
class LabelService(
    private val messages: Messages,
    private val pluginMessages: PluginMessages,
) {
    @GetMapping(path = [URL_LABELS_MVC], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun fetchLabels(): ResponseEntity<Properties> {
        val allProperties =
            Properties().apply {
                putAll(messages.getMessages())
                putAll(pluginMessages.getMessages())
            }
        return ResponseEntity(allProperties, HttpStatus.OK)
    }

    companion object {
        const val URL_LABELS_MVC = "/service/labels.mvc"
        private val log = LoggerFactory.getLogger(LabelService::class.java)
    }
}
