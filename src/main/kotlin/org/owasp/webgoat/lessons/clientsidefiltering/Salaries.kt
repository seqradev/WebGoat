/*
 * SPDX-FileCopyrightText: Copyright © 2016 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.clientsidefiltering

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.util.FileCopyUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.xml.sax.InputSource
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpressionException
import javax.xml.xpath.XPathFactory

@RestController
class Salaries(
    @Value("\${webgoat.user.directory}") private val webGoatHomeDirectory: String,
) {
    @PostConstruct
    fun copyFiles() {
        val classPathResource = ClassPathResource("lessons/employees.xml")
        val targetDirectory = File(webGoatHomeDirectory, "/ClientSideFiltering")
        if (!targetDirectory.exists()) {
            targetDirectory.mkdir()
        }
        try {
            FileCopyUtils.copy(
                classPathResource.inputStream,
                FileOutputStream(File(targetDirectory, "employees.xml")),
            )
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @GetMapping("clientSideFiltering/salaries")
    @ResponseBody
    fun invoke(): List<Map<String, Any>> {
        val d = File(webGoatHomeDirectory, "ClientSideFiltering/employees.xml")
        val factory = XPathFactory.newInstance()
        val path = factory.newXPath()
        val columns = 5
        val json = mutableListOf<MutableMap<String, Any>>()
        var employeeJson: MutableMap<String, Any> = mutableMapOf()

        try {
            FileInputStream(d).use { inputStream ->
                val inputSource = InputSource(inputStream)

                val expression =
                    "/Employees/Employee/UserID | " +
                        "/Employees/Employee/FirstName | " +
                        "/Employees/Employee/LastName | " +
                        "/Employees/Employee/SSN | " +
                        "/Employees/Employee/Salary "

                val nodes =
                    path.evaluate(
                        expression,
                        inputSource,
                        XPathConstants.NODESET,
                    ) as org.w3c.dom.NodeList

                for (i in 0 until nodes.length) {
                    if (i % columns == 0) {
                        employeeJson = mutableMapOf()
                        json.add(employeeJson)
                    }
                    val node = nodes.item(i)
                    employeeJson[node.nodeName] = node.textContent
                }
            }
        } catch (e: XPathExpressionException) {
            log.error("Unable to parse xml", e)
        } catch (e: IOException) {
            log.error("Unable to read employees.xml at location: '{}'", d)
        }
        return json
    }

    companion object {
        private val log = LoggerFactory.getLogger(Salaries::class.java)
    }
}
