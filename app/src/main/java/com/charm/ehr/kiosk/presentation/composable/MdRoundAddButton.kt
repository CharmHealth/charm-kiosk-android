package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun MdRoundAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize : Dp = 26.dp,
    elevation: Dp = 1.dp,
    isAdd: Boolean=true
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(buttonSize)
            .clickable(enabled = true, onClick = onClick)
    ) {
//        FloatingActionButton(
//            onClick = { onClick() },
//            modifier = Modifier.size(buttonSize),
//            containerColor = Color.Transparent,
//            shape = CircleShape,
//            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = elevation)
//        ) {
        Image(
            painter = if(isAdd) painterResource(R.drawable.ic_add_with_bg) else painterResource(R.drawable.ic_edit_with_bg),
            contentDescription = "Add",
            modifier = Modifier.padding(4.dp),
        )
//        }
    }
}

@Preview(showBackground = true)
@Composable
fun MdRoundAddButtonPreview() {
    MdTheme{
        Row(
            modifier = Modifier
                .height(100.dp)
                .width(100.dp)
                .background(MdColors.MdSurfaceColor),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MdRoundAddButton(
                onClick = {},
                modifier = Modifier.fillMaxHeight().width(50.dp), buttonSize = 28.dp
            )
        }
    }
}
