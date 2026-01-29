/*
 * SPDX-FileCopyrightText: Copyright © 2020 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.pathtraversal

import org.apache.commons.io.FilenameUtils
import org.owasp.webgoat.container.CurrentUsername
import org.owasp.webgoat.container.assignments.AssignmentEndpoint
import org.owasp.webgoat.container.assignments.AttackResult
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.failed
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.informationMessage
import org.owasp.webgoat.container.assignments.AttackResultBuilder.Companion.success
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.FileCopyUtils
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.util.Base64

open class ProfileUploadBase(
    val webGoatHomeDirectory: String,
) : AssignmentEndpoint {
    protected fun execute(
        file: MultipartFile,
        fullName: String?,
        username: String,
    ): AttackResult {
        if (file.isEmpty) {
            return failed(this).feedback("path-traversal-profile-empty-file").build()
        }
        if (fullName.isNullOrEmpty()) {
            return failed(this).feedback("path-traversal-profile-empty-name").build()
        }

        val uploadDirectory = cleanupAndCreateDirectoryForUser(username)

        return try {
            val uploadedFile = File(uploadDirectory, fullName)
            uploadedFile.createNewFile()
            FileCopyUtils.copy(file.bytes, uploadedFile)

            if (attemptWasMade(uploadDirectory, uploadedFile)) {
                solvedIt(uploadedFile)
            } else {
                informationMessage(this)
                    .feedback("path-traversal-profile-updated")
                    .feedbackArgs(uploadedFile.absoluteFile)
                    .build()
            }
        } catch (e: IOException) {
            failed(this).output(e.message).build()
        }
    }

    protected fun cleanupAndCreateDirectoryForUser(username: String): File {
        val uploadDirectory = File(webGoatHomeDirectory, "/PathTraversal/$username")
        if (uploadDirectory.exists()) {
            FileSystemUtils.deleteRecursively(uploadDirectory)
        }
        Files.createDirectories(uploadDirectory.toPath())
        return uploadDirectory
    }

    private fun attemptWasMade(
        expectedUploadDirectory: File,
        uploadedFile: File,
    ): Boolean =
        expectedUploadDirectory.canonicalPath !=
            uploadedFile.parentFile.canonicalPath

    private fun solvedIt(uploadedFile: File): AttackResult {
        val parentName = uploadedFile.canonicalFile.parentFile.name
        return if (parentName.endsWith("PathTraversal")) {
            success(this).build()
        } else {
            failed(this)
                .attemptWasMade()
                .feedback("path-traversal-profile-attempt")
                .feedbackArgs(uploadedFile.canonicalPath)
                .build()
        }
    }

    open fun getProfilePicture(
        @CurrentUsername username: String,
    ): ResponseEntity<*> =
        ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType(MediaType.IMAGE_JPEG_VALUE))
            .body(getProfilePictureAsBase64(username))

    protected fun getProfilePictureAsBase64(username: String): ByteArray {
        val profilePictureDirectory = File(webGoatHomeDirectory, "/PathTraversal/$username")
        val profileDirectoryFiles = profilePictureDirectory.listFiles()

        return profileDirectoryFiles
            ?.takeIf { it.isNotEmpty() }
            ?.firstOrNull { FilenameUtils.isExtension(it.name, listOf("jpg", "png")) }
            ?.let {
                runCatching {
                    FileInputStream(profileDirectoryFiles[0]).use { inputStream ->
                        Base64.getEncoder().encode(FileCopyUtils.copyToByteArray(inputStream))
                    }
                }.getOrElse { defaultImage() }
            }
            ?: defaultImage()
    }

    protected fun defaultImage(): ByteArray {
        val inputStream = javaClass.getResourceAsStream("/images/account.png")
        return Base64.getEncoder().encode(FileCopyUtils.copyToByteArray(inputStream))
    }
}
