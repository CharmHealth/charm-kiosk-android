package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun MdErrorText(text: String) {
    Text(text = text,
        modifier = Modifier
            .padding(top = 4.dp)
            .fillMaxWidth(),
        style = MdTheme.typography.medium14TextStyle.copy(color = MdColors.RedMatt)
    )
}