/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.i18n

import org.springframework.context.support.ReloadableResourceBundleMessageSource
import java.util.Properties

/**
 * ExposedReloadableResourceMessageBundleSource class. Extends the reloadable message source with a
 * way to get all messages
 */
class Messages(
    private val language: Language,
) : ReloadableResourceBundleMessageSource() {
    /**
     * Gets all messages for presented Locale.
     *
     * @return all messages
     */
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
}
