/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.DriverManagerDataSource
import java.util.function.Function
import javax.sql.DataSource

@Configuration
class DatabaseConfiguration(
    private val properties: DataSourceProperties,
) {
    @Bean
    @Primary
    fun dataSource(): DataSource =
        DriverManagerDataSource().apply {
            setDriverClassName(properties.driverClassName)
            url = properties.url
            username = properties.username
            password = properties.password
        }

    /**
     * Define 2 Flyway instances, 1 for WebGoat itself which it uses for internal storage like users
     * and 1 for lesson specific tables we use. This way we clean the data in the lesson database
     * quite easily see [org.owasp.webgoat.container.service.RestartLessonService.restartLesson]
     * for how we clean the lesson related tables.
     */
    @Bean(initMethod = "migrate")
    fun flyWayContainer(): Flyway =
        Flyway
            .configure()
            .configuration(mapOf("driver" to properties.driverClassName))
            .dataSource(dataSource())
            .schemas("container")
            .locations("db/container")
            .load()

    @Bean
    fun flywayLessons(): Function<String, Flyway> =
        Function { schema ->
            Flyway
                .configure()
                .configuration(mapOf("driver" to properties.driverClassName))
                .schemas(schema)
                .cleanDisabled(false)
                .dataSource(dataSource())
                .locations("lessons")
                .load()
        }

    @Bean
    fun lessonDataSource(dataSource: DataSource): LessonDataSource = LessonDataSource(dataSource)
}
