package nl.jacobras.codeobserver.util.ui.text

import nl.jacobras.codeobserver.dto.GitHash

@Suppress("MagicNumber")
internal fun GitHash.excerpt() = value.take(7)