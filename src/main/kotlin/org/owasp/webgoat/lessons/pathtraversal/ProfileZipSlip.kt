/*
 * SPDX-FileCopyrightText: Copyright © 2021 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.pathtraversal

import org.owasp.webgoat.container.CurrentUsername
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType.ALL_VALUE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.util.FileCopyUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipFile

@RestController
@AssignmentHints(
    "path-traversal-zip-slip.hint1",
    "path-traversal-zip-slip.hint2",
    "path-traversal-zip-slip.hint3",
    "path-traversal-zip-slip.hint4",
)
class ProfileZipSlip(
    @Value("\${webgoat.server.directory}") webGoatHomeDirectory: String,
) : ProfileUploadBase(webGoatHomeDirectory) {
    @PostMapping(
        value = ["/PathTraversal/zip-slip"],
        consumes = [ALL_VALUE],
        produces = [APPLICATION_JSON_VALUE],
    )
    @ResponseBody
    fun uploadFileHandler(
        @RequestParam("uploadedFileZipSlip") file: MultipartFile,
        @CurrentUsername username: String,
    ): AttackResult {
        val originalFilename = requireNotNull(file.originalFilename) { "Original filename is required" }
        return if (!originalFilename.lowercase().endsWith(".zip")) {
            failed(this).feedback("path-traversal-zip-slip.no-zip").build()
        } else {
            processZipUpload(file, username)
        }
    }

    private fun processZipUpload(
        file: MultipartFile,
        username: String,
    ): AttackResult {
        val tmpZipDirectory = Files.createTempDirectory(username)
        cleanupAndCreateDirectoryForUser(username)
        val currentImage = getProfilePictureAsBase64(username)

        return try {
            val uploadedZipFile = tmpZipDirectory.resolve(file.originalFilename)
            FileCopyUtils.copy(file.bytes, uploadedZipFile.toFile())

            ZipFile(uploadedZipFile.toFile()).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    val f = File(tmpZipDirectory.toFile(), entry.name)
                    zip.getInputStream(entry).use { inputStream ->
                        Files.copy(inputStream, f.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    }
                }
            }

            isSolved(currentImage, getProfilePictureAsBase64(username))
        } catch (e: IOException) {
            failed(this).output(e.message).build()
        }
    }

    private fun isSolved(
        currentImage: ByteArray,
        newImage: ByteArray,
    ): AttackResult =
        if (currentImage.contentEquals(newImage)) {
            failed(this).output("path-traversal-zip-slip.extracted").build()
        } else {
            success(this).output("path-traversal-zip-slip.extracted").build()
        }

    @GetMapping("/PathTraversal/zip-slip/")
    @ResponseBody
    override fun getProfilePicture(
        @CurrentUsername username: String,
    ): ResponseEntity<*> = super.getProfilePicture(username)

    @GetMapping("/PathTraversal/zip-slip/profile-image/{username}")
    @ResponseBody
    fun getProfileImage(
        @PathVariable username: String,
    ): ResponseEntity<*> = ResponseEntity.notFound().build<Any>()

    companion object {
        private val log = LoggerFactory.getLogger(ProfileZipSlip::class.java)
    }
}
