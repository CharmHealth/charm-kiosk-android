package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun MdTopToolBar(headingText: String, onBackClick: () -> Unit,showBackIcon: Boolean=true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(vertical = 8.dp, horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(showBackIcon){
            MdBackButton(onBackClick = onBackClick)
            MdHorizontalSpace(1.5f)
        } else {
            MdHorizontalSpace(3f)
        }

        Text(
            text = headingText,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
            style = MdTheme.typography.semiBold20TextStyle
        )
    }
}

@Composable
@Preview(showBackground = true)
fun MdTopToolBarPreview() {
    MdTheme {
        MdTopToolBar("Heading text", onBackClick = {

        })
    }
}

@Composable
@Preview(showBackground = true)
fun MdTopToolBarWithoutHeadingPreview() {
    MdTheme {
        MdTopToolBar(headingText = "Heading text",
            onBackClick = {

        },
            showBackIcon = false)

    }
}