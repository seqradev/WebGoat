/*
 * SPDX-FileCopyrightText: Copyright © 2024 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container

import org.slf4j.LoggerFactory
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class LessonResourceScanner(
    resourcePatternResolver: ResourcePatternResolver,
) {
    private val log = LoggerFactory.getLogger(LessonResourceScanner::class.java)

    val lessons: Set<String> =
        try {
            resourcePatternResolver
                .getResources("classpath:/lessons/*/*")
                .mapNotNull { resource ->
                    // WG can run as a fat jar or as directly from file system we need to support both so use
                    // the URL
                    val matcher = LESSON_PATTERN.matcher(resource.url.toString())
                    if (matcher.matches()) matcher.group(1) else null
                }.toSet()
                .also { log.debug("Found {} lessons", it.size) }
        } catch (e: IOException) {
            log.warn("No lessons found...")
            emptySet()
        }

    fun applyPattern(pattern: String): List<String> = lessons.map { pattern.format(it) }

    companion object {
        private val LESSON_PATTERN = "^.*/lessons/([^/]*)/.*$".toPattern()
    }
}
