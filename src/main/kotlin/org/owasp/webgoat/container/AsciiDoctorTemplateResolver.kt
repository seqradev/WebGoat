/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.Options
import org.owasp.webgoat.container.asciidoc.OperatingSystemMacro
import org.owasp.webgoat.container.asciidoc.UsernameMacro
import org.owasp.webgoat.container.asciidoc.WebGoatTmpDirMacro
import org.owasp.webgoat.container.asciidoc.WebGoatVersionMacro
import org.owasp.webgoat.container.asciidoc.WebWolfMacro
import org.owasp.webgoat.container.asciidoc.WebWolfRootMacro
import org.owasp.webgoat.container.i18n.Language
import org.slf4j.LoggerFactory
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpHeaders
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import org.thymeleaf.IEngineConfiguration
import org.thymeleaf.templateresolver.FileTemplateResolver
import org.thymeleaf.templateresource.ITemplateResource
import org.thymeleaf.templateresource.StringTemplateResource
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringWriter
import java.util.Locale

/**
 * Thymeleaf resolver for AsciiDoc used in the lesson, can be used as follows inside a lesson file:
 *
 * <code>
 * <div th:replace="~{doc:AccessControlMatrix_plan.adoc}"></div>
 * </code>
 */
class AsciiDoctorTemplateResolver(
    private val language: Language,
    private val resourceLoader: ResourceLoader,
) : FileTemplateResolver() {
    private val log = LoggerFactory.getLogger(AsciiDoctorTemplateResolver::class.java)

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
        log.debug("template used: {}", templateName)
        return try {
            getInputStream(templateName).use { inputStream ->
                val extensionRegistry = asciidoctor.javaExtensionRegistry()
                extensionRegistry.inlineMacro("webWolfLink", WebWolfMacro::class.java)
                extensionRegistry.inlineMacro("webWolfRootLink", WebWolfRootMacro::class.java)
                extensionRegistry.inlineMacro("webGoatVersion", WebGoatVersionMacro::class.java)
                extensionRegistry.inlineMacro("webGoatTempDir", WebGoatTmpDirMacro::class.java)
                extensionRegistry.inlineMacro("operatingSystem", OperatingSystemMacro::class.java)
                extensionRegistry.inlineMacro("username", UsernameMacro::class.java)

                val writer = StringWriter()
                asciidoctor.convert(InputStreamReader(inputStream), writer, createAttributes())
                StringTemplateResource(writer.buffer.toString())
            }
        } catch (e: Exception) {
            StringTemplateResource("<div>Unable to find documentation for: $templateName </div>")
        }
    }

    private fun getInputStream(templateName: String): InputStream {
        log.debug("locale: {}", language.locale.language)
        val computedResourceName = computeResourceName(templateName, language.locale.language)
        val localizedResource = resourceLoader.getResource("classpath:/$computedResourceName")
        return if (localizedResource.isReadable) {
            log.debug("localized file exists")
            localizedResource.inputStream
        } else {
            log.debug("using english template")
            resourceLoader.getResource("classpath:/$templateName").inputStream
        }
    }

    private fun computeResourceName(
        resourceName: String,
        language: String,
    ): String =
        (if (language == "en") resourceName else resourceName.replace(".adoc", "_$language.adoc"))
            .also {
                log.debug("computed local file name: {}", it)
                log.debug("file exists: {}", resourceLoader.getResource("classpath:/$it").isReadable)
            }

    private fun createAttributes(): Options =
        Options
            .builder()
            .attributes(
                Attributes
                    .builder()
                    .attribute("source-highlighter", "coderay")
                    .attribute("backend", "xhtml")
                    .attribute("lang", determineLanguage())
                    .attribute("icons", Attributes.FONT_ICONS)
                    .build(),
            ).build()

    private fun determineLanguage(): String {
        val request =
            (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request

        val browserLocale =
            request.session.getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME) as? Locale

        return browserLocale
            ?.also { log.debug("browser locale {}", it) }
            ?.language
            ?: request
                .getHeader(HttpHeaders.ACCEPT_LANGUAGE)
                ?.also { log.debug("browser locale {}", it) }
                ?.substring(0, 2)
            ?: "en".also { log.debug("browser default english") }
    }

    companion object {
        private val asciidoctor: Asciidoctor = Asciidoctor.Factory.create()
        private const val PREFIX = "doc:"
    }
}
