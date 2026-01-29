/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container

import org.owasp.webgoat.container.i18n.Language
import org.owasp.webgoat.container.i18n.Messages
import org.owasp.webgoat.container.i18n.PluginMessages
import org.owasp.webgoat.container.session.LabelDebugger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.ViewResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import org.thymeleaf.IEngineConfiguration
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver
import org.thymeleaf.spring6.view.ThymeleafViewResolver
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.FileTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver
import org.thymeleaf.templateresource.ITemplateResource
import org.thymeleaf.templateresource.StringTemplateResource
import java.nio.charset.StandardCharsets

/** Configuration for Spring MVC */
@Configuration
class MvcConfiguration(
    private val lessonScanner: LessonResourceScanner,
) : WebMvcConfigurer {
    private val log = LoggerFactory.getLogger(MvcConfiguration::class.java)

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/login").setViewName("login")
        registry.addViewController("/lesson_content").setViewName("lesson_content")
        registry.addViewController("/start.mvc").setViewName("main_new")
    }

    @Bean
    fun viewResolver(thymeleafTemplateEngine: SpringTemplateEngine): ViewResolver =
        ThymeleafViewResolver().apply {
            templateEngine = thymeleafTemplateEngine
            characterEncoding = StandardCharsets.UTF_8.displayName()
        }

    /**
     * Responsible for loading lesson templates based on Thymeleaf, for example:
     *
     * <div th:include="/lessons/spoofcookie/templates/spoofcookieform.html" id="content"></div>
     */
    @Bean
    fun lessonThymeleafTemplateResolver(resourceLoader: ResourceLoader): ITemplateResolver =
        object : FileTemplateResolver() {
            override fun computeTemplateResource(
                configuration: IEngineConfiguration,
                ownerTemplate: String?,
                template: String,
                resourceName: String,
                characterEncoding: String?,
                templateResolutionAttributes: MutableMap<String, Any>?,
            ): ITemplateResource? =
                try {
                    resourceLoader.getResource("classpath:$resourceName").inputStream.use { inputStream ->
                        StringTemplateResource(String(inputStream.readAllBytes(), StandardCharsets.UTF_8))
                    }
                } catch (e: Exception) {
                    null
                }
        }.apply {
            order = 1
        }

    /** Loads all normal WebGoat specific Thymeleaf templates */
    @Bean
    fun springThymeleafTemplateResolver(applicationContext: ApplicationContext): ITemplateResolver =
        SpringResourceTemplateResolver().apply {
            prefix = "classpath:/webgoat/templates/"
            suffix = ".html"
            templateMode = TemplateMode.HTML
            order = 2
            characterEncoding = UTF8
            setApplicationContext(applicationContext)
        }

    /** Loads the html for the complete lesson, see lesson_content.html */
    @Bean
    fun lessonTemplateResolver(resourceLoader: ResourceLoader): LessonTemplateResolver =
        LessonTemplateResolver(resourceLoader).apply {
            order = 0
            isCacheable = false
            characterEncoding = UTF8
        }

    /** Loads the lesson asciidoc. */
    @Bean
    fun asciiDoctorTemplateResolver(
        language: Language,
        resourceLoader: ResourceLoader,
    ): AsciiDoctorTemplateResolver {
        log.debug("template locale {}", language)
        return AsciiDoctorTemplateResolver(language, resourceLoader).apply {
            isCacheable = false
            order = 1
            characterEncoding = UTF8
        }
    }

    @Bean
    fun thymeleafTemplateEngine(
        springThymeleafTemplateResolver: ITemplateResolver,
        lessonTemplateResolver: LessonTemplateResolver,
        asciiDoctorTemplateResolver: AsciiDoctorTemplateResolver,
        lessonThymeleafTemplateResolver: ITemplateResolver,
    ): SpringTemplateEngine =
        SpringTemplateEngine().apply {
            enableSpringELCompiler = true
            addDialect(SpringSecurityDialect())
            setTemplateResolvers(
                setOf(
                    lessonTemplateResolver,
                    asciiDoctorTemplateResolver,
                    lessonThymeleafTemplateResolver,
                    springThymeleafTemplateResolver,
                ),
            )
        }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // WebGoat internal
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/webgoat/static/css/")
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/webgoat/static/js/")
        registry.addResourceHandler("/plugins/**").addResourceLocations("classpath:/webgoat/static/plugins/")
        registry.addResourceHandler("/fonts/**").addResourceLocations("classpath:/webgoat/static/fonts/")

        // WebGoat lessons
        registry
            .addResourceHandler("/images/**")
            .addResourceLocations(*lessonScanner.applyPattern("classpath:/lessons/%s/images/").toTypedArray())
        registry
            .addResourceHandler("/lesson_js/**")
            .addResourceLocations(*lessonScanner.applyPattern("classpath:/lessons/%s/js/").toTypedArray())
        registry
            .addResourceHandler("/lesson_css/**")
            .addResourceLocations(*lessonScanner.applyPattern("classpath:/lessons/%s/css/").toTypedArray())
        registry
            .addResourceHandler("/lesson_templates/**")
            .addResourceLocations(*lessonScanner.applyPattern("classpath:/lessons/%s/templates/").toTypedArray())
        registry
            .addResourceHandler("/video/**")
            .addResourceLocations(*lessonScanner.applyPattern("classpath:/lessons/%s/video/").toTypedArray())
    }

    @Bean
    fun pluginMessages(
        messages: Messages,
        language: Language,
        resourcePatternResolver: ResourcePatternResolver,
    ): PluginMessages =
        PluginMessages(messages, language, resourcePatternResolver).apply {
            setDefaultEncoding("UTF-8")
            setBasenames("i18n/WebGoatLabels")
            setFallbackToSystemLocale(false)
        }

    @Bean
    fun language(localeResolver: LocaleResolver): Language = Language(localeResolver)

    @Bean
    fun localeResolver(): LocaleResolver = SessionLocaleResolver()

    @Bean
    fun localeChangeInterceptor(): LocaleChangeInterceptor =
        LocaleChangeInterceptor().apply {
            paramName = "lang"
        }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(localeChangeInterceptor())
        registry.addInterceptor(UserInterceptor())
    }

    @Bean
    fun messageSource(language: Language): Messages =
        Messages(language).apply {
            setDefaultEncoding("UTF-8")
            setBasename("classpath:i18n/messages")
            setFallbackToSystemLocale(false)
        }

    @Bean
    fun labelDebugger(): LabelDebugger = LabelDebugger()

    companion object {
        private const val UTF8 = "UTF-8"
    }
}
