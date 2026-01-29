/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf

import org.owasp.webgoat.container.AjaxAuthenticationEntryPoint
import org.owasp.webgoat.webwolf.user.UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.web.SecurityFilterChain

/** Security configuration for WebWolf. */
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
                    .requestMatchers("/css/**", "/webjars/**", "/favicon.ico", "/js/**", "/images/**")
                    .permitAll()
                auth
                    .requestMatchers(
                        HttpMethod.GET,
                        "/fileupload/**",
                        "/files/**",
                        "/landing/**",
                        "/PasswordReset/**",
                    ).permitAll()
                auth.requestMatchers(HttpMethod.POST, "/files", "/mail", "/requests").permitAll()
                auth.anyRequest().authenticated()
            }.csrf { csrf ->
                csrf.disable()
            }.formLogin { login ->
                login
                    .loginPage("/login")
                    .failureUrl("/login?error=true")
                    .defaultSuccessUrl("/home", true)
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .permitAll()
            }.oauth2Login { oidc ->
                oidc.defaultSuccessUrl("/home")
            }.logout { logout ->
                logout.deleteCookies("WEBWOLFSESSION").invalidateHttpSession(true)
            }.exceptionHandling { handling ->
                handling.authenticationEntryPoint(AjaxAuthenticationEntryPoint("/login"))
            }.build()

    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userDetailsService)
    }

    @Bean
    fun userDetailsServiceBean(): UserDetailsService = userDetailsService

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager =
        authenticationConfiguration.authenticationManager

    @Bean
    fun passwordEncoder(): NoOpPasswordEncoder = NoOpPasswordEncoder.getInstance() as NoOpPasswordEncoder
}
