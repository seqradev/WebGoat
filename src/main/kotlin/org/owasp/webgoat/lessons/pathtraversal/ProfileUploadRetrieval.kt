/*
 * SPDX-FileCopyrightText: Copyright © 2020 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.pathtraversal

import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.RandomUtils
import org.owasp.webgoat.container.CurrentUsername
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.token.Sha512DigestUtils
import org.springframework.util.FileCopyUtils
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.Files
import java.util.Base64

@RestController
@AssignmentHints(
    "path-traversal-profile-retrieve.hint1",
    "path-traversal-profile-retrieve.hint2",
    "path-traversal-profile-retrieve.hint3",
    "path-traversal-profile-retrieve.hint4",
    "path-traversal-profile-retrieve.hint5",
    "path-traversal-profile-retrieve.hint6",
)
class ProfileUploadRetrieval(
    @Value("\${webgoat.server.directory}") webGoatHomeDirectory: String,
) : AssignmentEndpoint {
    private val catPicturesDirectory: File = File(webGoatHomeDirectory, "/PathTraversal/cats")

    init {
        catPicturesDirectory.mkdirs()
    }

    @PostConstruct
    fun initAssignment() {
        for (i in 1..10) {
            try {
                ClassPathResource("lessons/pathtraversal/images/cats/$i.jpg").inputStream.use { inputStream ->
                    FileCopyUtils.copy(inputStream, FileOutputStream(File(catPicturesDirectory, "$i.jpg")))
                }
            } catch (e: Exception) {
                log.error("Unable to copy pictures: ${e.message}")
            }
        }
        val secretDirectory = catPicturesDirectory.parentFile.parentFile
        try {
            Files.writeString(
                secretDirectory.toPath().resolve("path-traversal-secret.jpg"),
                "You found it submit the SHA-512 hash of your username as answer",
            )
        } catch (e: IOException) {
            log.error("Unable to write secret in: {}", secretDirectory, e)
        }
    }

    @PostMapping("/PathTraversal/random")
    @ResponseBody
    fun execute(
        @RequestParam(value = "secret", required = false) secret: String?,
        @CurrentUsername username: String,
    ): AttackResult =
        if (Sha512DigestUtils.shaHex(username).equals(secret, ignoreCase = true)) {
            success(this).build()
        } else {
            failed(this).build()
        }

    @GetMapping("/PathTraversal/random-picture")
    @ResponseBody
    fun getProfilePicture(request: HttpServletRequest): ResponseEntity<*> {
        val queryParams = request.queryString
        if (queryParams != null && (queryParams.contains("..") || queryParams.contains("/"))) {
            return ResponseEntity
                .badRequest()
                .body("Illegal characters are not allowed in the query params")
        }
        try {
            val id = request.getParameter("id")
            val catPicture =
                File(catPicturesDirectory, (id ?: RandomUtils.nextInt(1, 11).toString()) + ".jpg")

            if (catPicture.name.lowercase().contains("path-traversal-secret.jpg")) {
                return ResponseEntity
                    .ok()
                    .contentType(MediaType.parseMediaType(MediaType.IMAGE_JPEG_VALUE))
                    .body(FileCopyUtils.copyToByteArray(catPicture))
            }
            if (catPicture.exists()) {
                return ResponseEntity
                    .ok()
                    .contentType(MediaType.parseMediaType(MediaType.IMAGE_JPEG_VALUE))
                    .location(URI("/PathTraversal/random-picture?id=${catPicture.name}"))
                    .body(Base64.getEncoder().encode(FileCopyUtils.copyToByteArray(catPicture)))
            }
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .location(URI("/PathTraversal/random-picture?id=${catPicture.name}"))
                .body(
                    StringUtils
                        .arrayToCommaDelimitedString(catPicture.parentFile.listFiles())
                        .toByteArray(),
                )
        } catch (e: IOException) {
            log.error("Image not found", e)
        } catch (e: URISyntaxException) {
            log.error("Image not found", e)
        }

        return ResponseEntity.badRequest().build<Any>()
    }

    companion object {
        private val log = LoggerFactory.getLogger(ProfileUploadRetrieval::class.java)
    }
}
