package nl.jacobras.codebaseobserver.util.ui.text

import nl.jacobras.codebaseobserver.dto.GitHash

@Suppress("MagicNumber")
internal fun GitHash.excerpt() = value.take(7)