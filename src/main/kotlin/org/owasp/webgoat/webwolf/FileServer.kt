/*
 * SPDX-FileCopyrightText: Copyright © 2017 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.webwolf

import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone

/** Controller for uploading a file */
@Controller
class FileServer(
    @Value("\${webwolf.fileserver.location}") private val fileLocation: String,
    @Value("\${server.address}") private val server: String,
    @Value("\${server.servlet.context-path}") private val contextPath: String,
    @Value("\${server.port}") private val port: Int,
) {
    private val log = LoggerFactory.getLogger(FileServer::class.java)

    @RequestMapping(
        path = ["/file-server-location"],
        consumes = [MediaType.ALL_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE],
    )
    @ResponseBody
    fun getFileLocation(): String = fileLocation

    @PostMapping(value = ["/fileupload"])
    @Throws(IOException::class)
    fun importFile(
        @RequestParam("file") multipartFile: MultipartFile,
        authentication: Authentication,
    ): ModelAndView {
        val username = authentication.name
        val destinationDir = File(fileLocation, username)
        destinationDir.mkdirs()
        val originalFilename = requireNotNull(multipartFile.originalFilename) { "Filename is required" }
        // DO NOT use multipartFile.transferTo(), see
        // https://stackoverflow.com/questions/60336929/java-nio-file-nosuchfileexception-when-file-transferto-is-called
        multipartFile.inputStream.use { inputStream ->
            val destinationFile = destinationDir.toPath().resolve(originalFilename)
            Files.deleteIfExists(destinationFile)
            Files.copy(inputStream, destinationFile)
        }
        log.debug("File saved to {}", File(destinationDir, originalFilename))

        return ModelAndView(
            RedirectView("files", true),
            ModelMap().addAttribute("uploadSuccess", "File uploaded successful"),
        )
    }

    @GetMapping(value = ["/files"])
    fun getFiles(
        request: HttpServletRequest,
        authentication: Authentication?,
        timezone: TimeZone,
    ): ModelAndView {
        val username = authentication?.name ?: "anonymous"
        val destinationDir = File(fileLocation, username)

        val modelAndView =
            ModelAndView().apply {
                viewName = "files"
            }
        val changeIndicatorFile = File(destinationDir, "${username}_changed")
        if (changeIndicatorFile.exists()) {
            modelAndView.addObject("uploadSuccess", request.getParameter("uploadSuccess"))
        }
        changeIndicatorFile.delete()

        data class UploadedFile(
            val name: String,
            val size: String,
            val link: String,
            val creationTime: String,
        )

        val uploadedFiles =
            destinationDir
                .listFiles { file -> file.isFile }
                ?.map { file ->
                    UploadedFile(
                        name = file.name,
                        size = FileUtils.byteCountToDisplaySize(file.length()),
                        link = "files/$username/${file.name}",
                        creationTime = getCreationTime(timezone, file),
                    )
                }?.sortedByDescending { it.creationTime }
                .orEmpty()

        modelAndView.addObject("files", uploadedFiles)
        modelAndView.addObject("webwolf_url", "http://$server:$port$contextPath")
        return modelAndView
    }

    private fun getCreationTime(
        timezone: TimeZone,
        file: File,
    ): String =
        try {
            val creationTime = Files.getAttribute(file.toPath(), "creationTime") as FileTime
            val zonedDateTime: ZonedDateTime = creationTime.toInstant().atZone(timezone.toZoneId())
            DATE_TIME_FORMATTER.format(zonedDateTime)
        } catch (e: IOException) {
            "unknown"
        }

    companion object {
        private val DATE_TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}
