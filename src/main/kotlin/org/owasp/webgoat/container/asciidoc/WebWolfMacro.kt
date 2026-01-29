/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.asciidoc

import org.asciidoctor.ast.PhraseNode
import org.asciidoctor.ast.StructuralNode
import org.asciidoctor.extension.InlineMacroProcessor

/**
 * Usage in asciidoc:
 *
 * webWolfLink:here[] will display a href with here as text
 */
open class WebWolfMacro : InlineMacroProcessor {
    constructor(macroName: String) : super(macroName)

    constructor(macroName: String, config: Map<String, Any>) : super(macroName, config)

    override fun process(
        contentNode: StructuralNode,
        linkText: String,
        attributes: MutableMap<String, Any>,
    ): PhraseNode {
        val env = EnvironmentExposure.getEnv()
        val hostname = env?.getProperty("webwolf.url")
        val target = attributes.getOrDefault("target", "home") as String
        val href = "$hostname/$target"

        // are we using noLink in webWolfLink:landing[noLink]? Then display link with full href
        val displayText =
            if (displayCompleteLinkNoFormatting(attributes)) href else linkText

        val options =
            mutableMapOf<String, Any>(
                "type" to ":link",
                "target" to href,
            )
        attributes["window"] = "_blank"
        return createPhraseNode(contentNode, "anchor", displayText, attributes, options)
    }

    private fun displayCompleteLinkNoFormatting(attributes: Map<String, Any>): Boolean =
        attributes.values.any { it == "noLink" }
}
