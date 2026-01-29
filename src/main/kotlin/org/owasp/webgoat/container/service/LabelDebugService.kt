/*
 * SPDX-FileCopyrightText: Copyright © 2014 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.service

import org.owasp.webgoat.container.session.LabelDebugger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class LabelDebugService(
    private val labelDebugger: LabelDebugger,
) {
    /**
     * Checks if debugging of labels is enabled or disabled
     *
     * @return a [ResponseEntity] object.
     */
    @RequestMapping(path = [URL_DEBUG_LABELS_MVC], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun checkDebuggingStatus(): ResponseEntity<Map<String, Any>> {
        log.debug("Checking label debugging, it is {}", labelDebugger.isEnabled)
        val result = createResponse(labelDebugger.isEnabled)
        return ResponseEntity(result, HttpStatus.OK)
    }

    /**
     * Sets the enabled flag on the label debugger to the given parameter
     *
     * @param enabled [LabelDebugger] object
     * @return a [ResponseEntity] object.
     */
    @RequestMapping(
        value = [URL_DEBUG_LABELS_MVC],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        params = [KEY_ENABLED],
    )
    @ResponseBody
    fun setDebuggingStatus(
        @RequestParam("enabled") enabled: Boolean,
    ): ResponseEntity<Map<String, Any>> {
        log.debug("Setting label debugging to {} ", labelDebugger.isEnabled)
        val result = createResponse(enabled)
        labelDebugger.isEnabled = enabled
        return ResponseEntity(result, HttpStatus.OK)
    }

    /**
     * @param enabled [LabelDebugger] object
     * @return a [Map] object.
     */
    private fun createResponse(enabled: Boolean): Map<String, Any> = mapOf(KEY_SUCCESS to true, KEY_ENABLED to enabled)

    companion object {
        private const val URL_DEBUG_LABELS_MVC = "/service/debug/labels.mvc"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_SUCCESS = "success"
        private val log = LoggerFactory.getLogger(LabelDebugService::class.java)
    }
}
