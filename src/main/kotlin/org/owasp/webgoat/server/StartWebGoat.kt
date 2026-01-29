/*
 * SPDX-FileCopyrightText: Copyright © 2022 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.server

import org.owasp.webgoat.container.WebGoat
import org.owasp.webgoat.webwolf.WebWolf
import org.slf4j.LoggerFactory
import org.springframework.boot.ResourceBanner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationContext
import org.springframework.core.io.ClassPathResource

private val log = LoggerFactory.getLogger("org.owasp.webgoat.server.StartWebGoat")

fun main(args: Array<String>) {
    val parentBuilder =
        SpringApplicationBuilder()
            .parent(ParentConfig::class.java)
            .web(WebApplicationType.NONE)

    parentBuilder
        .child(WebWolf::class.java)
        .banner(ResourceBanner(ClassPathResource("banner-webwolf.txt")))
        .web(WebApplicationType.SERVLET)
        .run(*args)

    val webGoatContext =
        parentBuilder
            .child(WebGoat::class.java)
            .banner(ResourceBanner(ClassPathResource("banner-webgoat.txt")))
            .web(WebApplicationType.SERVLET)
            .run(*args)

    printStartUpMessage(webGoatContext)
}

private fun printStartUpMessage(webGoatContext: ApplicationContext) {
    with(webGoatContext.environment) {
        val url = getProperty("webgoat.url")
        val sslEnabled = getProperty("server.ssl.enabled", Boolean::class.java) == true
        val displayUrl = if (sslEnabled) url?.replace("http", "https") else url
        log.warn("Please browse to {} to start using WebGoat...", displayUrl)
    }
}
