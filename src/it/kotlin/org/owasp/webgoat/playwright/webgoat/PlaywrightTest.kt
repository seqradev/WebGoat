/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.playwright.webgoat

import com.microsoft.playwright.Browser
import com.microsoft.playwright.junit.Options
import com.microsoft.playwright.junit.OptionsFactory
import com.microsoft.playwright.junit.UsePlaywright
import org.owasp.webgoat.ServerUrlConfig

@UsePlaywright(PlaywrightTest.WebGoatOptions::class)
open class PlaywrightTest {
    class WebGoatOptions : OptionsFactory {
        override fun getOptions(): Options =
            Options()
                .setHeadless(true)
                .setContextOptions(contextOptions)
    }

    companion object {
        private val webGoatUrlConfig = ServerUrlConfig.webGoat()
        private val webWolfUrlConfig = ServerUrlConfig.webWolf()

        @JvmStatic
        val contextOptions: Browser.NewContextOptions
            get() =
                Browser
                    .NewContextOptions()
                    .setLocale("en-US")
                    .setBaseURL(webGoatUrlConfig.baseUrl)

        @JvmStatic
        fun webGoatUrl(path: String): String = webGoatUrlConfig.url(path)

        @JvmStatic
        fun webWolfURL(path: String): String = webWolfUrlConfig.url(path)
    }
}
