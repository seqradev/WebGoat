/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf

import jakarta.annotation.PostConstruct
import org.owasp.webgoat.container.UserInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.io.File

@Configuration
class MvcConfiguration(
    @Value("\${webwolf.fileserver.location}") private val fileLocation: String,
) : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/files/**").addResourceLocations("file:///$fileLocation/")
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/webwolf/static/css/")
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/webwolf/static/js/")
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/webwolf/static/images/")
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/login").setViewName("webwolf-login")
        registry.addViewController("/home").setViewName("home")
        registry.addViewController("/").setViewName("home")
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(UserInterceptor())
    }

    @PostConstruct
    fun createDirectory() {
        File(fileLocation).takeUnless { it.exists() }?.mkdirs()
    }
}
