/*
 * SPDX-FileCopyrightText: Copyright © 2020 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.pathtraversal

import org.owasp.webgoat.container.CurrentUsername
import org.owasp.webgoat.container.assignments.AssignmentHints
import org.owasp.webgoat.container.assignments.AttackResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType.ALL_VALUE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@AssignmentHints(
    "path-traversal-profile-fix.hint1",
    "path-traversal-profile-fix.hint2",
    "path-traversal-profile-fix.hint3",
)
class ProfileUploadFix(
    @Value("\${webgoat.server.directory}") webGoatHomeDirectory: String,
) : ProfileUploadBase(webGoatHomeDirectory) {
    @PostMapping(
        value = ["/PathTraversal/profile-upload-fix"],
        consumes = [ALL_VALUE],
        produces = [APPLICATION_JSON_VALUE],
    )
    @ResponseBody
    fun uploadFileHandler(
        @RequestParam("uploadedFileFix") file: MultipartFile,
        @RequestParam(value = "fullNameFix", required = false) fullName: String?,
        @CurrentUsername username: String,
    ): AttackResult = super.execute(file, fullName?.replace("../", "") ?: "", username)

    @GetMapping("/PathTraversal/profile-picture-fix")
    @ResponseBody
    override fun getProfilePicture(
        @CurrentUsername username: String,
    ): ResponseEntity<*> = super.getProfilePicture(username)
}
