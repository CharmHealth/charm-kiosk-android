package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.charm.ehr.kiosk.common.util.UIUtil
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun MDTextView(
    modifier: Modifier = Modifier,
    value: String,
    style: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    color: Color = Color.Unspecified
) {
    Text(
        value,
        modifier,
        style = style,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        color = color
    )
}

@Composable
fun MDTextView(
    modifier: Modifier = Modifier,
    value: AnnotatedString,
    style: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    color: Color = Color.Unspecified
) {
    Text(
        value,
        modifier,
        style = style,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        color = color
    )
}

@Composable
fun MdCustomLabel(
    text: String,
    isMandatory: Boolean = false,
    isError: Boolean = false,
    isFocused: Boolean = false,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    textStyle: TextStyle = MdTheme.typography.medium14TextStyle,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    enabled: Boolean
) {
    val labelColor = UIUtil.getLabelColor(isError, isFocused, textStyle,enabled)
    Text(
        text = UIUtil.addMandatoryStar(
            notes = text,
            color = Color.Red,
            isMandatory = isMandatory,
            fontSize = textStyle.fontSize,
            fontWeight = textStyle.fontWeight ?: FontWeight.Normal,
            fontFamily = textStyle.fontFamily ?: FontFamily.Default,
            fontColor = labelColor
        ),
        modifier = modifier,
        style = textStyle,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        color = labelColor
    )
}



