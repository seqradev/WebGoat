/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container

import org.owasp.webgoat.container.users.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.web.SecurityFilterChain

/** Security configuration for WebGoat. */
@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    private val userDetailsService: UserService,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/favicon.ico",
                        "/css/**",
                        "/images/**",
                        "/js/**",
                        "/fonts/**",
                        "/plugins/**",
                        "/registration",
                        "/register.mvc",
                        "/actuator/**",
                    ).permitAll()
                    .anyRequest()
                    .authenticated()
            }.formLogin { login ->
                login
                    .loginPage("/login")
                    .defaultSuccessUrl("/welcome.mvc", true)
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .permitAll()
            }.oauth2Login { oidc ->
                oidc.defaultSuccessUrl("/login-oauth.mvc")
                oidc.loginPage("/login")
            }.logout { logout ->
                logout.deleteCookies("JSESSIONID").invalidateHttpSession(true)
            }.csrf { csrf ->
                csrf.disable()
            }.headers { headers ->
                headers.disable()
            }.exceptionHandling { handling ->
                handling.authenticationEntryPoint(AjaxAuthenticationEntryPoint("/login"))
            }.build()

    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) = auth.userDetailsService(userDetailsService)

    @Bean
    @Primary
    fun userDetailsServiceBean(): UserDetailsService = userDetailsService

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager =
        authenticationConfiguration.authenticationManager

    @Bean
    fun passwordEncoder(): NoOpPasswordEncoder = NoOpPasswordEncoder.getInstance() as NoOpPasswordEncoder
}
