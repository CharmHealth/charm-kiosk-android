package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

//@Composable
//fun SearchBarWithDropDown(
//    options: List<Pair<String, String>>,
//    onQueryChanged: (String) -> Unit,
//    onTemplateTypeChanged: (Pair<String, String>) -> Unit,
//) {
//
//    var query by remember { mutableStateOf("") }
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(MdTheme.dimension.cardPadding)
//            .border(
//                width = 2.dp, color = Color.Transparent, // Invisible border
//                shape = RoundedCornerShape(24.dp)
//            )
//            .background(color = MdColors.Black10, shape = RoundedCornerShape(24.dp))
//            .padding(4.dp)
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Dropdown Menu with dropdown icon
//            DropdownMenuComponent(
//                onOptionSelected = { onTemplateTypeChanged(it) }, options = options
//            )
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            // Text area with search icon
//            Box(
//                modifier = Modifier
//                    .weight(1f)
//                    .background(color = MdColors.Transparent, shape = RoundedCornerShape(20.dp))
//                    .padding(8.dp),
//                contentAlignment = Alignment.CenterEnd // Align icon to the end
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // BasicTextField with hint
//                    Box(modifier = Modifier.weight(1f)) {
//                        if (query.isEmpty()) {
//                            Text(
//                                text = stringResource(R.string.search),
//                                style = MdTheme.typography.medium16TextStyle.copy(color = Color.Gray)
//                            )
//                        }
//                        BasicTextField(
//                            value = query, onValueChange = {
//                                query = it
//                                onQueryChanged(it)
//                            }, modifier = Modifier.fillMaxWidth(), singleLine = true, textStyle = MdTheme.typography.medium16TextStyle
//                        )
//                    }
//
//                }
//
//                // Cross Icon to clear text
//                if (query.isNotBlank()) {
//                    Image(
//                        imageVector = Icons.Default.Close, contentDescription = "Clear Text", modifier = Modifier
//                            .size(24.dp)
//                            .clickable {
//                                query = ""
//                                onQueryChanged("")
//                            })
//                }
//            }
//        }
//    }
//}

// Dropdown menu composable with dropdown icon
@Composable
fun DropdownMenuComponent(
    onOptionSelected: (Pair<String, String>) -> Unit,
    options: List<Pair<String, String>>,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember {
        mutableStateOf(options.first().second)
    }

    Box(
        modifier = Modifier
            .background(Color.White, shape = RoundedCornerShape(24.dp))
            .clickable { expanded = true }
            .padding(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(modifier = Modifier.width(4.dp))

            Text(text = selectedOption)

            Icon(
                painter = painterResource(R.drawable.ic_arrow_down), contentDescription = "Dropdown Arrow"
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option.first) }, onClick = {
                    selectedOption = option.first
                    onOptionSelected(option)
                    expanded = false
                })
            }
        }
    }
}

@Composable
fun ProviderItem(
    modifier: Modifier = Modifier, itemPOJO: Any,
    itemString: String, isSelected: Boolean,
    onItemClick: (Any) -> Unit, isLast: Boolean = false
) {
    Column(
        Modifier
            .clickable { onItemClick(itemPOJO) }
            .padding(0.dp)) {
        Row(
            modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = isSelected,
                onCheckedChange = onItemClick,
            )

            Text(
                text = itemString,
                modifier = Modifier
                    .padding(start = MdTheme.dimension.halfCardPadding)
                    .weight(1f),
                style = MdTheme.typography.medium16TextStyle
            )

        }
        if (!isLast) {
            Spacer(
                modifier
                    .height(.5.dp)
                    .fillMaxWidth()
                    .padding(
                        horizontal = MdTheme.dimension.cardPadding
                    )
                    .background(MdColors.Black20)
            )
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun SearchBarPreview() {
//    SearchBarWithDropDown(options = listOf("Option 1" to "Option 1", "Option 2" to "Option 2"), onQueryChanged = { query ->
//
//    }, onTemplateTypeChanged = {
//
//    })
//}

@Preview(showBackground = true)
@Composable
fun ProviderItemPreview() {

    Column {

        ProviderItem(
            itemPOJO = Any(),
            itemString = "Sample Item 1",
            isSelected = true,
            onItemClick = {

            }
        )

        ProviderItem(
            itemPOJO = Any(),
            itemString = "Sample Item 2",
            isSelected = true,
            onItemClick = {

            }
        )

        ProviderItem(
            itemPOJO = Any(),
            itemString = "Sample Item 3",
            isSelected = true,
            onItemClick = {

            },
            isLast = true
        )

    }
}