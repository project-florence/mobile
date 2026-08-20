package com.florence.app.presentation.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/**
 * Hafif, bağımlılıksız yerel markdown render'ı.
 *
 * Desteklenen bloklar:
 *  - Başlıklar: `#`, `##`, `###`
 *  - Kalın `**text**`, italik `*text*`, satır içi kod `` `text` ``
 *  - Liste maddeleri: `- `, `* ` ve `1. ` (ardışık listeler tek blokta)
 *  - Kod bloğu: ``` ile sarılı çok satırlı
 *  - Paragraf boşlukları (boş satırla ayrılmış bloklar)
 *  - Yatay çizgi: `---`
 *
 * Desteklenmeyen/algılanamayan sözdizimi sessizce düz metin olarak render edilir.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(markdown) { parseBlocks(markdown) }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        blocks.forEach { block ->
            when (block) {
                is Block.Heading -> Text(
                    text = parseInline(block.text),
                    style = when (block.level) {
                        1 -> MaterialTheme.typography.titleLarge
                        2 -> MaterialTheme.typography.titleMedium
                        else -> MaterialTheme.typography.titleSmall
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                is Block.Paragraph -> Text(
                    text = parseInline(block.text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                is Block.ListBlock -> BlockList(block)
                is Block.Code -> Text(
                    text = block.content,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                )
                is Block.Rule -> HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun BlockList(block: Block.ListBlock) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        block.items.forEach { (marker, text) ->
            Row {
                Text(
                    text = "$marker  ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = parseInline(text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

// ---- Block modeli ----

private sealed interface Block {
    data class Heading(val level: Int, val text: String) : Block
    data class Paragraph(val text: String) : Block
    data class ListBlock(val items: List<Pair<String, String>>) : Block
    data class Code(val content: String) : Block
    object Rule : Block
}

private val headingRegex = Regex("^(#{1,3})\\s+(.*)$")
private val orderedListRegex = Regex("^\\s*(\\d+)[.)]\\s+(.*)$")
private val unorderedListRegex = Regex("^\\s*[-*]\\s+(.*)$")

/** Markdown'ı satır bazlı olarak bloklara ayırır. */
private fun parseBlocks(markdown: String): List<Block> {
    val blocks = mutableListOf<Block>()
    val lines = markdown.split("\n")
    var i = 0

    while (i < lines.size) {
        val raw = lines[i]
        val line = raw.trim()

        when {
            // Kod bloğu
            line.startsWith("```") -> {
                val buf = StringBuilder()
                i++
                var closed = false
                while (i < lines.size) {
                    if (lines[i].trim().startsWith("```")) { closed = true; break }
                    buf.append(lines[i]).append('\n')
                    i++
                }
                blocks.add(Block.Code(buf.toString().trimEnd('\n')))
                if (closed) i++ else i = lines.size
            }

            // Başlık
            headingRegex.matchEntire(raw)?.let { m ->
                val level = m.groupValues[1].length
                blocks.add(Block.Heading(level, m.groupValues[2]))
                i++
                true
            } ?: false -> {
            }

            // Yatay çizgi
            line == "---" || line == "***" || line == "___" -> {
                blocks.add(Block.Rule)
                i++
            }

            // Liste
            isListLine(raw) -> {
                val items = mutableListOf<Pair<String, String>>()
                while (i < lines.size && isListLine(lines[i])) {
                    val l = lines[i].trimStart()
                    val marker = orderedListRegex.find(l)?.groupValues?.get(1) ?: "*"
                    val text = orderedListRegex.find(l)?.groupValues?.get(2)
                        ?: unorderedListRegex.find(l)?.groupValues?.get(1)
                        ?: ""
                    items.add(Pair("$marker.", text.trim()))
                    i++
                }
                blocks.add(Block.ListBlock(items))
            }

            // Boş satır -> paragraf ayracı (render aşamasında spacedBy halleder)
            line.isEmpty() -> {
                i++
            }

            // Paragraf: ardışık dolu satırları birleştir
            else -> {
                val buf = StringBuilder(raw.trim())
                i++
                while (i < lines.size) {
                    val next = lines[i].trim()
                    if (next.isEmpty() || next.startsWith("#") ||
                        next.startsWith("```") || isListLine(lines[i]) ||
                        next == "---" || next == "***" || next == "___"
                    ) break
                    if (buf.isNotEmpty()) buf.append(' ').append(next)
                    else buf.append(next)
                    i++
                }
                blocks.add(Block.Paragraph(buf.toString()))
            }
        }
    }
    return blocks
}

private fun isListLine(line: String): Boolean {
    val t = line.trimStart()
    return unorderedListRegex.matches(t) || orderedListRegex.matches(t)
}

// ---- Satır içi biçimlendirme ----

/**
 * Kalın `**`, italik `*` ve satır içi kod `` ` `` işaretlerini span'lere çevirir.
 * Eşleşmeyen işaretler olduğu gibi bırakılır.
 */
private fun parseInline(text: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        val c = text[i]
        when {
            c == '*' && i + 1 < text.length && text[i + 1] == '*' -> {
                val end = text.indexOf("**", i + 2)
                if (end > i + 1) {
                    val inner = text.substring(i + 2, end)
                    val boldStyle = SpanStyle(fontWeight = FontWeight.Bold)
                    if (inner.contains('*')) {
                        // İç içe kalın+italik
                        parseBoldItalic(inner, boldStyle)
                    } else {
                        withStyle(boldStyle) { append(inner) }
                    }
                    i = end + 2
                } else {
                    append(c); i++
                }
            }
            c == '*' -> {
                val end = text.indexOf('*', i + 1)
                if (end > i + 1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append(c); i++
                }
            }
            c == '`' -> {
                val end = text.indexOf('`', i + 1)
                if (end > i + 1) {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append(c); i++
                }
            }
            else -> {
                append(c); i++
            }
        }
    }
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.parseBoldItalic(
    inner: String,
    outerStyle: SpanStyle,
) {
    var j = 0
    while (j < inner.length) {
        val c = inner[j]
        if (c == '*') {
            val end = inner.indexOf('*', j + 1)
            if (end > j + 1) {
                withStyle(outerStyle + SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(inner.substring(j + 1, end))
                }
                j = end + 1
            } else {
                withStyle(outerStyle) { append(c) }
                j++
            }
        } else {
            withStyle(outerStyle) { append(c) }
            j++
        }
    }
}
