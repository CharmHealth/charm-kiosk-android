package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun MdHighlightedText(
    text: String,
    searchQuery: String,
    style: TextStyle = MdTheme.typography.medium18TextStyle,
    highlightColor: Color = MdColors.PaleYellow,
    maxLines: Int = Int.MAX_VALUE,
    selectedFontWeight: FontWeight = FontWeight.SemiBold,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = TextAlign.Start,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    middleEllipsis: Boolean = false,
) {
    if (middleEllipsis) {
        val textMeasurer = rememberTextMeasurer()
        BoxWithConstraints(modifier = modifier) {
            val maxWidthPx = constraints.maxWidth
            val displayText = remember(text, maxWidthPx, style) {
                computeMiddleEllipsis(text, textMeasurer, style, maxWidthPx)
            }
            Text(
                text = buildHighlightedString(displayText, searchQuery, highlightColor, selectedFontWeight),
                style = style,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                color = color,
                fontSize = fontSize,
                fontStyle = fontStyle,
                fontWeight = fontWeight,
                fontFamily = fontFamily,
                letterSpacing = letterSpacing,
                textDecoration = textDecoration,
                textAlign = textAlign,
                lineHeight = lineHeight,
                minLines = minLines,
                inlineContent = inlineContent,
                onTextLayout = onTextLayout,
            )
        }
    } else {
        Text(
            text = buildHighlightedString(text, searchQuery, highlightColor, selectedFontWeight),
            style = style,
            modifier = modifier.wrapContentWidth(),
            maxLines = maxLines,
            overflow = overflow,
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            softWrap = softWrap,
            minLines = minLines,
            inlineContent = inlineContent,
            onTextLayout = onTextLayout,
        )
    }
}

private fun buildHighlightedString(
    text: String,
    searchQuery: String,
    highlightColor: Color,
    selectedFontWeight: FontWeight,
) = buildAnnotatedString {
    if (searchQuery.isBlank()) {
        append(text)
    } else {
        val pattern = Regex(Regex.escape(searchQuery), RegexOption.IGNORE_CASE)
        var lastIndex = 0
        for (match in pattern.findAll(text)) {
            append(text.substring(lastIndex, match.range.first))
            withStyle(SpanStyle(background = highlightColor, fontWeight = selectedFontWeight)) {
                append(text.substring(match.range))
            }
            lastIndex = match.range.last + 1
        }
        append(text.substring(lastIndex))
    }
}

private fun computeMiddleEllipsis(
    text: String,
    textMeasurer: TextMeasurer,
    style: TextStyle,
    maxWidthPx: Int,
): String {
    if (maxWidthPx <= 0 || maxWidthPx == Int.MAX_VALUE) return text

    val measure = { str: String ->
        textMeasurer.measure(
            text = AnnotatedString(str),
            style = style,
            overflow = TextOverflow.Visible,
            softWrap = false,
            maxLines = 1,
        ).size.width
    }

    if (measure(text) <= maxWidthPx) return text

    val ellipsis = "…"
    var low = 0
    var high = text.length / 2
    var best = ellipsis

    while (low <= high) {
        val keep = (low + high) / 2
        val candidate = "${text.take(keep)}$ellipsis${text.takeLast(keep)}"
        if (measure(candidate) <= maxWidthPx) {
            best = candidate
            low = keep + 1
        } else {
            high = keep - 1
        }
    }
    return best
}

@Preview(showBackground = true, widthDp = 160)
@Composable
fun MdHighlightedTextPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        MdHighlightedText(
            text = "johndoe@example.com",
            searchQuery = "doe",
            highlightColor = MdColors.PaleYellow
        )
        Spacer(modifier = Modifier.height(8.dp))
        MdHighlightedText(
            text = "verylongemail.address@example.com",
            searchQuery = "example",
            highlightColor = MdColors.PaleYellow,
            middleEllipsis = true,
        )
        Spacer(modifier = Modifier.height(8.dp))
        MdHighlightedText(
            text = "No highlight here",
            searchQuery = "",
            highlightColor = Color.Red
        )
    }
}
