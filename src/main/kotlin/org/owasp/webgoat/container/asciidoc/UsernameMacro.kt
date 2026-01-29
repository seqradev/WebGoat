/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.asciidoc

import org.asciidoctor.ast.PhraseNode
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.InlineMacroProcessor
import org.owasp.webgoat.container.users.WebGoatUser
import org.springframework.security.core.context.SecurityContextHolder

class UsernameMacro : InlineMacroProcessor {
    constructor(macroName: String) : super(macroName)

    constructor(macroName: String, config: Map<String, Any>) : super(macroName, config)

    override fun process(
        contentNode: StructuralNode,
        target: String,
        attributes: Map<String, Any>,
    ): PhraseNode {
        val auth = SecurityContextHolder.getContext().authentication
        val username = (auth.principal as? WebGoatUser)?.username ?: "unknown"

        // see
        // https://discuss.asciidoctor.org/How-to-create-inline-macro-producing-HTML-In-AsciidoctorJ-td8313.html for why quoted is used
        return createPhraseNode(contentNode, "quoted", username)
    }
}
