/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.i18n

import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.core.io.support.ResourcePatternResolver
import java.util.Properties

/**
 * Message resource bundle for plugins.
 */
class PluginMessages(
    messages: Messages,
    private val language: Language,
    private val resourcePatternResolver: ResourcePatternResolver,
) : ReloadableResourceBundleMessageSource() {
    init {
        setParentMessageSource(messages)
        setBasename("WebGoatLabels")
    }

    protected override fun refreshProperties(
        filename: String,
        propHolder: PropertiesHolder?,
    ): PropertiesHolder {
        val properties = Properties()
        val lastModified = System.currentTimeMillis()

        try {
            resourcePatternResolver
                .getResources("classpath:/lessons/**/i18n/WebGoatLabels$PROPERTIES_SUFFIX")
                .forEach { resource ->
                    val sourcePath = resource.uri.toString().removeSuffix(PROPERTIES_SUFFIX)
                    super.refreshProperties(sourcePath, propHolder).properties?.let {
                        properties.putAll(it)
                    }
                }
        } catch (e: Exception) {
            logger.error("Unable to read plugin message", e)
        }

        return PropertiesHolder(properties, lastModified)
    }

    fun getMessages(): Properties = getMergedProperties(language.locale).properties ?: Properties()

    fun getMessage(code: String?): String? =
        if (code == null) null else getMessage(code, null as Array<Any?>?, language.locale)

    fun getMessage(
        code: String?,
        args: Array<Any?>?,
    ): String? = if (code == null) null else getMessage(code, args, language.locale)

    fun getMessage(
        code: String?,
        defaultValue: String?,
    ): String? =
        if (code == null) {
            null
        } else {
            super.getMessage(code, null, defaultValue, language.locale)
        }

    // For calls with explicit Object[] parameter (can be null)
    fun getMessage(
        code: String?,
        defaultValue: String?,
        args: Array<Any?>?,
    ): String? =
        if (code == null) {
            null
        } else {
            super.getMessage(code, args, defaultValue, language.locale)
        }

    // For inline arguments - signature differs by having a required first arg
    fun getMessage(
        code: String?,
        defaultValue: String?,
        firstArg: Any,
        vararg moreArgs: Any,
    ): String? {
        if (code == null) return null
        val allArgs = arrayOf(firstArg, *moreArgs)
        return super.getMessage(code, allArgs, defaultValue, language.locale)
    }

    companion object {
        private const val PROPERTIES_SUFFIX = ".properties"
    }
}
