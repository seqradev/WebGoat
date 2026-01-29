/*
 * SPDX-FileCopyrightText: Copyright © 2018 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.container.asciidoc

/**
 * Usage in asciidoc:
 *
 * webWolfLink:here[] will display a href with here as text webWolfLink:landing[noLink] will
 * display the complete url, for example: http://WW_HOST:WW_PORT/landing
 */
class WebWolfRootMacro : WebWolfMacro {
    constructor(macroName: String) : super(macroName)

    constructor(macroName: String, config: Map<String, Any>) : super(macroName, config)
}
