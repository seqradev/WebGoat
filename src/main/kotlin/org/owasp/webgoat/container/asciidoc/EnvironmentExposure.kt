/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.asciidoc

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

/**
 * Make environment available in the asciidoc code (which you cannot inject because it is handled by
 * the framework)
 */
@Component
class EnvironmentExposure : ApplicationContextAware {
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }

    companion object {
        private var context: ApplicationContext? = null

        @JvmStatic
        fun getEnv(): Environment? = context?.environment
    }
}
