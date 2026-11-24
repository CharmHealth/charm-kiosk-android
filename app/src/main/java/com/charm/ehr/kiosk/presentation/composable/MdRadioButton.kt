package com.charm.ehr.kiosk.presentation.composable


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme


@Composable
fun MDCustomIconRadio(
    modifier: Modifier = Modifier,
    optionString: String,
    isSelected: Boolean,
    onValueChange: (Boolean) -> Unit,
    isParent: Boolean,
    enable: Boolean = true
) {
    var rowModifier = modifier
        .heightIn(min = MdTheme.dimension.minHeightForSelectable)
        .height(IntrinsicSize.Min)

    if (enable) {
        rowModifier = rowModifier.selectable(
            selected = isSelected,
            onClick = { onValueChange(!isSelected) },
            role = Role.RadioButton
        )
    }
    Row(
        rowModifier
    ) {
        Column {
            val modifierIcon = if (isParent) Modifier.width(32.826.dp) else Modifier.width(22.dp)
            val selectedIcon =
                if (isParent) R.drawable.ic_radio_selected_with_arrow else R.drawable.ic_radio_selected_20dp
            val unSelectedIcon =
                if (isParent) R.drawable.ic_radio_unselected_with_arrow else R.drawable.ic_radio_unselected_20dp
            val tint = if (enable) {
                if (isSelected) MdColors.BrandPink else
                    MdColors.Black40
            } else MdColors.LightGray
            val icon = if (isSelected) selectedIcon else unSelectedIcon
            Icon(
                modifier = modifierIcon
                    .padding(top = 10.dp)
                    .height(20.dp),
                painter = painterResource(id = icon), contentDescription = null, tint = tint
            )
            if (isParent && isSelected) {
                MDVerticalLine(
                    Modifier
                        .weight(1f)
                        .padding(start = 9.9.dp), 0.5.dp, MdColors.Black60
                )
            }
        }
        val textModifier =
            if (isParent) Modifier.padding(
                bottom = 6.dp,
                top = 4.dp,
                start = 8.dp
            ) else Modifier.padding(bottom = 6.dp, top = 4.dp, start = 15.dp)
        MDTextView(
            value = optionString,
            modifier = textModifier.align(Alignment.CenterVertically),
            style = MdTheme.typography.regular14TextStyle
        )
    }
}

//region Preview
@Preview(showBackground = true)
@Composable
fun ViewRadioBox() {
    MdTheme {
        var test by remember { mutableStateOf(true) }
        MDCustomIconRadio(
            Modifier.fillMaxWidth(),
            "test",
            test,
            onValueChange = { isChecked ->
                test = isChecked
            },
            true,
            true
        )
    }

}

@Preview(showBackground = true)
@Composable
fun ViewRadioBox1() {
    MdTheme {
        var test by remember { mutableStateOf(true) }
        MDCustomIconRadio(
            Modifier.fillMaxWidth(),
            "test",
            test,
            onValueChange = { isChecked ->
                test = isChecked
            },
            false,
            true
        )
    }
}
//endregion
