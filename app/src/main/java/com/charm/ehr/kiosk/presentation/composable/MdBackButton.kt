package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R

@Composable
fun MdBackButton(onBackClick: () -> Unit) {
    IconButton(onClick = onBackClick, modifier = Modifier.size(48.dp)) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_down),
            contentDescription = "Back",
            tint = Color.Black,
            modifier = Modifier.rotate(90f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MdBackButtonPreview() {
    MdBackButton(onBackClick = {})
}
