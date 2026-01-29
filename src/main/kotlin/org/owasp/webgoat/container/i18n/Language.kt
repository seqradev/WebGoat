/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.i18n

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.LocaleResolver
import java.util.Locale

/**
 * Wrapper around the LocaleResolver from Spring so we do not need to bother with passing the
 * HttpRequest object when asking for a Locale.
 */
open class Language(
    private val localeResolver: LocaleResolver,
) {
    open val locale: Locale
        get() =
            localeResolver.resolveLocale(
                (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request,
            )
}
