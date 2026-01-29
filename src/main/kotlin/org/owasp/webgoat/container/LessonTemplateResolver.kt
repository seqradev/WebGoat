/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container

import org.slf4j.LoggerFactory
import org.springframework.core.io.ResourceLoader
import org.thymeleaf.IEngineConfiguration
import org.thymeleaf.templateresolver.FileTemplateResolver
import org.thymeleaf.templateresource.ITemplateResource
import org.thymeleaf.templateresource.StringTemplateResource
import java.nio.charset.StandardCharsets

/**
 * Dynamically resolve a lesson. In the html file this can be invoked as:
 * ```
 * <div th:case="true" th:replace="lesson:__${lesson.class.simpleName}__"></div>
 * ```
 *
 * Thymeleaf will invoke this resolver based on the prefix and this implementation will resolve
 * the html in the plugins directory
 */
class LessonTemplateResolver(
    private val resourceLoader: ResourceLoader,
) : FileTemplateResolver() {
    private val log = LoggerFactory.getLogger(LessonTemplateResolver::class.java)
    private val resources = mutableMapOf<String, ByteArray>()

    init {
        setResolvablePatterns(setOf("$PREFIX*"))
    }

    override fun computeTemplateResource(
        configuration: IEngineConfiguration,
        ownerTemplate: String?,
        template: String,
        resourceName: String,
        characterEncoding: String?,
        templateResolutionAttributes: MutableMap<String, Any>?,
    ): ITemplateResource {
        val templateName = resourceName.substring(PREFIX.length)
        val resource = resources[templateName] ?: loadAndCache(templateName)

        return if (resource == null) {
            StringTemplateResource("Unable to find lesson HTML: $templateName")
        } else {
            StringTemplateResource(String(resource, StandardCharsets.UTF_8))
        }
    }

    private fun loadAndCache(templateName: String): ByteArray? =
        try {
            val resource = resourceLoader.getResource("classpath:/$templateName").inputStream.readAllBytes()
            resources[templateName] = resource
            resource
        } catch (e: Exception) {
            log.error(
                "Unable to find lesson HTML: '{}', does the name of HTML file name match the lesson class name?",
                templateName,
            )
            null
        }

    companion object {
        private const val PREFIX = "lesson:"
    }
}
