/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.sqlinjection.mitigation

import org.owasp.webgoat.container.LessonDataSource
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("SqlInjectionMitigations/servers")
class Servers(
    private val dataSource: LessonDataSource,
) {
    data class Server(
        val id: String,
        val hostname: String,
        val ip: String,
        val mac: String,
        val status: String,
        val description: String,
    )

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Throws(Exception::class)
    fun sort(
        @RequestParam column: String,
    ): List<Server> {
        val servers = mutableListOf<Server>()

        dataSource.connection.use { connection ->
            connection
                .prepareStatement(
                    "select id, hostname, ip, mac, status, description from SERVERS where status <> 'out" +
                        " of order' order by " +
                        column,
                ).use { statement ->
                    statement.executeQuery().use { rs ->
                        while (rs.next()) {
                            val server =
                                Server(
                                    rs.getString(1),
                                    rs.getString(2),
                                    rs.getString(3),
                                    rs.getString(4),
                                    rs.getString(5),
                                    rs.getString(6),
                                )
                            servers.add(server)
                        }
                    }
                }
        }
        return servers
    }

    companion object {
        private val log = LoggerFactory.getLogger(Servers::class.java)
    }
}
