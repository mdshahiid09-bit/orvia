package com.orvia.shahid.app.ui.common

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orvia.shahid.app.R
import com.orvia.shahid.app.ui.theme.BrandAccent
import com.orvia.shahid.app.ui.theme.BrandCyan

@Composable
fun CodeBlockView(
    code: String,
    language: String = "",
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val copiedText = stringResource(R.string.toast_copied)

    val highlightedCode = buildSyntaxHighlightedText(code)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Color(0xFF0F0B1E), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF2E254A), RoundedCornerShape(10.dp))
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF191330), RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.ifBlank { "code" }.lowercase(),
                color = BrandCyan,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )

            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(code))
                    Toast.makeText(context, copiedText, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_copy),
                    contentDescription = stringResource(R.string.copy_message),
                    tint = BrandAccent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Code Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = highlightedCode,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.5.sp,
                lineHeight = 18.sp
            )
        }
    }
}

private fun buildSyntaxHighlightedText(code: String): AnnotatedString {
    val keywords = setOf(
        "val", "var", "fun", "class", "interface", "object", "return", "if", "else", "for", "while",
        "import", "package", "public", "private", "protected", "override", "data", "const", "def",
        "function", "let", "const", "export", "import", "from", "async", "await", "true", "false",
        "null", "struct", "impl", "fn", "type", "select", "from", "where", "int", "string", "bool"
    )

    return buildAnnotatedString {
        append(code)
        val text = code

        // Strings
        val stringRegex = Regex("\"[^\"]*\"|'[^']*'")
        stringRegex.findAll(text).forEach { match ->
            addStyle(SpanStyle(color = Color(0xFF98C379)), match.range.first, match.range.last + 1)
        }

        // Comments
        val commentRegex = Regex("//.*|/\\*[\\s\\S]*?\\*/")
        commentRegex.findAll(text).forEach { match ->
            addStyle(SpanStyle(color = Color(0xFF5C6370)), match.range.first, match.range.last + 1)
        }

        // Keywords
        val wordRegex = Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b")
        wordRegex.findAll(text).forEach { match ->
            if (keywords.contains(match.value)) {
                addStyle(SpanStyle(color = Color(0xFFE06C75), fontWeight = FontWeight.SemiBold), match.range.first, match.range.last + 1)
            }
        }

        // Default text color
        addStyle(SpanStyle(color = Color(0xFFABB2BF)), 0, text.length)
    }
}
