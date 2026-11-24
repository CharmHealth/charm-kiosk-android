package com.charm.ehr.kiosk.presentation.composable


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun MDCustomIconCheckBox(
    optionString: String,
    isChecked: Boolean,
    onValueChange: (Boolean) -> Unit,
    hasChildren: Boolean,
    enable: Boolean = true,
    textStyle: TextStyle = MdTheme.typography.regular14TextStyle,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    textAlign: TextAlign? = null
) {
    var rowModifier = Modifier
        .heightIn(min = MdTheme.dimension.minHeightForSelectable)
        .height(IntrinsicSize.Min)
        .fillMaxWidth()
    if (enable) {
        rowModifier = rowModifier.toggleable(
            value = isChecked,
            onValueChange = { onValueChange(!isChecked) },
            role = Role.Checkbox
        )
    }
    Row(
        modifier = rowModifier, horizontalArrangement = horizontalArrangement
    ) {
        Column {
            val unselectedIcon =
                if (hasChildren) R.drawable.ic_checkbox_unselected_with_plus else R.drawable.ic_checkbox_unselected
            val tint = if (enable) {
                if (isChecked) {
                    MdColors.BrandPink
                } else {
                    MdColors.Black40
                }

            } else MdColors.LightGray
            val icon = if (isChecked) R.drawable.ic_checkbox_tick_selected else unselectedIcon
            Icon(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(20.dp)
                    .height(20.dp),
                painter = painterResource(id = icon), contentDescription = null, tint = tint
            )
            if (hasChildren && isChecked) {
                MDVerticalLine(
                    Modifier
                        .weight(1f)
                        .padding(start = 9.dp), 0.5.dp, MdColors.Black60
                )
            }
        }
        MDTextView(
            value = optionString,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 8.dp, bottom = 6.dp, top = 4.dp),
            style = textStyle,
            textAlign = textAlign
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ViewCheckBox() {
    MdTheme {
        var test by remember { mutableStateOf(true) }
        MDCustomIconCheckBox(
            "Testing 1",
            test,
            onValueChange = { isChecked ->
                test = isChecked
            },
            false, true, textStyle = MdTheme.typography.regular14TextStyle
        )
    }

}