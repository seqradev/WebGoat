/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.asciidoc

import org.asciidoctor.ast.PhraseNode
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.InlineMacroProcessor

class WebGoatTmpDirMacro : InlineMacroProcessor {
    constructor(macroName: String) : super(macroName)

    constructor(macroName: String, config: Map<String, Any>) : super(macroName, config)

    override fun process(
        contentNode: StructuralNode,
        target: String,
        attributes: Map<String, Any>,
    ): PhraseNode {
        val env = EnvironmentExposure.getEnv()?.getProperty("webgoat.server.directory")

        // see
        // https://discuss.asciidoctor.org/How-to-create-inline-macro-producing-HTML-In-AsciidoctorJ-td8313.html for why quoted is used
        return createPhraseNode(contentNode, "quoted", env)
    }
}
