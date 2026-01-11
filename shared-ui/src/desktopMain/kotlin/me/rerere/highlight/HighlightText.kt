package me.rerere.highlight

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

data class HighlightTextColorPalette(
    val keyword: Color,
    val string: Color,
    val number: Color,
    val comment: Color,
    val function: Color,
    val operator: Color,
    val punctuation: Color,
    val className: Color,
    val property: Color,
    val boolean: Color,
    val variable: Color,
    val tag: Color,
    val attrName: Color,
    val attrValue: Color,
    val fallback: Color,
) {
    companion object {
        val Default = HighlightTextColorPalette(
            keyword = Color(0xFFCC7832),
            string = Color(0xFF6A8759),
            number = Color(0xFF6897BB),
            comment = Color(0xFF808080),
            function = Color(0xFFFFC66D),
            operator = Color(0xFFCC7832),
            punctuation = Color(0xFFCC7832),
            className = Color(0xFFCB772F),
            property = Color(0xFFCB772F),
            boolean = Color(0xFF6897BB),
            variable = Color(0xFF6A8759),
            tag = Color(0xFFE8BF6A),
            attrName = Color(0xFFBABABA),
            attrValue = Color(0xFF6A8759),
            fallback = Color(0xFF808080),
        )
    }
}

@Composable
fun HighlightText(
    code: String,
    language: String,
    modifier: Modifier = Modifier,
    colors: HighlightTextColorPalette = HighlightTextColorPalette.Default,
    fontSize: TextUnit = 12.sp,
    fontFamily: FontFamily = FontFamily.Monospace,
    fontStyle: FontStyle = FontStyle.Normal,
    fontWeight: FontWeight = FontWeight.Normal,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
) {
    Text(
        modifier = modifier,
        text = AnnotatedString(code),
        fontSize = fontSize,
        fontFamily = fontFamily,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
    )
}

fun AnnotatedString.Builder.buildHighlightText(
    token: HighlightToken,
    colors: HighlightTextColorPalette,
) {
    when (token) {
        is HighlightToken.Plain -> append(token.content)
        is HighlightToken.Token.StringContent -> append(token.content)
        is HighlightToken.Token.StringListContent -> token.content.forEach { append(it) }
        is HighlightToken.Token.Nested -> token.content.forEach { buildHighlightText(it, colors) }
    }
}
