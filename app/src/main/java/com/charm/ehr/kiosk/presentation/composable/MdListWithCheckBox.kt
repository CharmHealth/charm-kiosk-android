package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun <T> MdListWithCheckBox(
    items: List<T>, selectedItems: List<T>, onItemSelected: (T) -> Unit, itemLabel: (T) -> String
) {
    LazyColumn {
        items(items) { item ->
            MdListItem(
                label = itemLabel(item), isChecked = selectedItems.contains(item), onCheckedChange = { onItemSelected(item) })
        }
    }
}

@Composable
fun MdListItem(
    label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .padding(vertical = MdTheme.dimension.cardPadding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MdHorizontalSpace(1f)
            Checkbox(
                checked = isChecked, onCheckedChange = onCheckedChange
            )
            MdHorizontalSpace(1f)
            Text(text = label)
            MdHorizontalSpace(1f)
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MdColors.Black5)
        )
    }
}