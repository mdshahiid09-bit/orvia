package com.orvia.shahid.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orvia.shahid.app.ui.theme.BrandAccent
import com.orvia.shahid.app.ui.theme.BrandCyan

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    fontSizeMultiplier: Float = 1.0f
) {
    val blocks = parseMarkdownBlocks(markdown)

    Column(modifier = modifier.fillMaxWidth()) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Code -> {
                    CodeBlockView(
                        code = block.content,
                        language = block.language
                    )
                }
                is MarkdownBlock.Heading -> {
                    val size = when (block.level) {
                        1 -> 20.sp * fontSizeMultiplier
                        2 -> 17.sp * fontSizeMultiplier
                        else -> 15.sp * fontSizeMultiplier
                    }
                    Text(
                        text = buildAnnotatedInlineMarkdown(block.text, textColor),
                        fontSize = size,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                is MarkdownBlock.Table -> {
                    TableView(rows = block.rows, textColor = textColor)
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = buildAnnotatedInlineMarkdown(block.text, textColor),
                        fontSize = 14.5.sp * fontSizeMultiplier,
                        lineHeight = 21.sp * fontSizeMultiplier,
                        color = textColor,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

sealed class MarkdownBlock {
    data class Paragraph(val text: String) : MarkdownBlock()
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class Code(val language: String, val content: String) : MarkdownBlock()
    data class Table(val rows: List<List<String>>) : MarkdownBlock()
}

private fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = text.lines()
    var idx = 0

    while (idx < lines.size) {
        val line = lines[idx]

        // Code block start
        if (line.trim().startsWith("```")) {
            val lang = line.trim().removePrefix("```").trim()
            val codeBuilder = StringBuilder()
            idx++
            while (idx < lines.size && !lines[idx].trim().startsWith("```")) {
                codeBuilder.append(lines[idx]).append("\n")
                idx++
            }
            blocks.add(MarkdownBlock.Code(lang, codeBuilder.toString().trimEnd()))
            idx++
            continue
        }

        // Heading
        if (line.startsWith("#")) {
            val level = line.takeWhile { it == '#' }.length
            val headingText = line.drop(level).trim()
            blocks.add(MarkdownBlock.Heading(level, headingText))
            idx++
            continue
        }

        // Table detection (| col1 | col2 |)
        if (line.trim().startsWith("|") && line.trim().endsWith("|")) {
            val tableRows = mutableListOf<List<String>>()
            while (idx < lines.size && lines[idx].trim().startsWith("|") && lines[idx].trim().endsWith("|")) {
                val rowLine = lines[idx].trim()
                // Ignore separator row (|---|---|)
                if (!rowLine.contains("---")) {
                    val cells = rowLine.split("|")
                        .filterIndexed { index, _ -> index != 0 && index != rowLine.split("|").lastIndex }
                        .map { it.trim() }
                    if (cells.isNotEmpty()) {
                        tableRows.add(cells)
                    }
                }
                idx++
            }
            if (tableRows.isNotEmpty()) {
                blocks.add(MarkdownBlock.Table(tableRows))
            }
            continue
        }

        // Paragraph
        val paraBuilder = StringBuilder()
        while (idx < lines.size && !lines[idx].trim().startsWith("```") && !lines[idx].startsWith("#") &&
            !(lines[idx].trim().startsWith("|") && lines[idx].trim().endsWith("|"))
        ) {
            paraBuilder.append(lines[idx]).append("\n")
            idx++
        }
        val para = paraBuilder.toString().trim()
        if (para.isNotEmpty()) {
            blocks.add(MarkdownBlock.Paragraph(para))
        }
    }
    return blocks
}

@Composable
private fun TableView(rows: List<List<String>>, textColor: Color) {
    if (rows.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(1.dp, Color(0xFF332952), RoundedCornerShape(8.dp))
    ) {
        rows.forEachIndexed { rIdx, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (rIdx == 0) Color(0xFF221A3D) else Color.Transparent)
                    .border(0.5.dp, Color(0xFF2D234A))
                    .padding(8.dp)
            ) {
                row.forEach { cell ->
                    Box(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cell,
                            fontSize = 12.5.sp,
                            fontWeight = if (rIdx == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (rIdx == 0) BrandAccent else textColor
                        )
                    }
                }
            }
        }
    }
}

private fun buildAnnotatedInlineMarkdown(raw: String, baseColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val text = raw

        // Regex for bold, italic, and inline code
        val inlineRegex = Regex("(\\*\\*[^*]+\\*\\*)|(\\*[^*]+\\*)|(`[^`]+`)|(\\[[^\\]]+\\]\\([^\\)]+\\))")
        val matches = inlineRegex.findAll(text).toList()

        for (match in matches) {
            if (match.range.first > cursor) {
                append(text.substring(cursor, match.range.first))
            }

            val value = match.value
            when {
                value.startsWith("**") && value.endsWith("**") -> {
                    val content = value.removeSurrounding("**")
                    val start = length
                    append(content)
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor), start, length)
                }
                value.startsWith("*") && value.endsWith("*") -> {
                    val content = value.removeSurrounding("*")
                    val start = length
                    append(content)
                    addStyle(SpanStyle(fontStyle = FontStyle.Italic, color = baseColor), start, length)
                }
                value.startsWith("`") && value.endsWith("`") -> {
                    val content = value.removeSurrounding("`")
                    val start = length
                    append(content)
                    addStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0x33B388FF),
                            color = BrandCyan
                        ),
                        start,
                        length
                    )
                }
                value.startsWith("[") && value.contains("](") -> {
                    val title = value.substringAfter("[").substringBefore("]")
                    val start = length
                    append(title)
                    addStyle(SpanStyle(color = BrandCyan, fontWeight = FontWeight.Medium), start, length)
                }
                else -> append(value)
            }
            cursor = match.range.last + 1
        }

        if (cursor < text.length) {
            append(text.substring(cursor))
        }

        addStyle(SpanStyle(color = baseColor), 0, length)
    }
}
