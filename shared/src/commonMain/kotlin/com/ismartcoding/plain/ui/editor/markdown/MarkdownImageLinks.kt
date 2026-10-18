package com.ismartcoding.plain.ui.editor.markdown

import com.ismartcoding.plain.ui.markdown.model.State
import com.ismartcoding.plain.ui.markdown.utils.HTML_IMG_TAG_REGEX
import com.ismartcoding.plain.ui.markdown.utils.resolveImageLink
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode

internal fun extractImageLinksFromMarkdown(state: State.Success): List<String> {
    val links = mutableListOf<String>()
    fun visit(node: ASTNode) {
        when (node.type) {
            MarkdownElementTypes.IMAGE -> node.resolveImageLink(state.content, state.referenceLinkHandler)
                ?.takeIf { it.isNotEmpty() }?.let(links::add)
            MarkdownTokenTypes.HTML_TAG, MarkdownElementTypes.HTML_BLOCK -> {
                HTML_IMG_TAG_REGEX.findAll(node.getTextInNode(state.content)).forEach { match ->
                    match.groupValues.drop(2).firstOrNull { it.isNotEmpty() }?.let(links::add)
                }
            }
            MarkdownElementTypes.CODE_FENCE, MarkdownElementTypes.CODE_BLOCK, MarkdownElementTypes.CODE_SPAN -> Unit
            else -> node.children.forEach(::visit)
        }
    }
    visit(state.node)
    return links
}
