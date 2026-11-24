package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.domain.OptionData
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun MDYesOrNoComponent(
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    optionList: List<OptionData>,
    onValueChange: (Boolean, OptionData) -> Unit,
    onOtherValueChange: (String, OptionData) -> Unit
) {

    Column(modifier) {
        Row(Modifier.fillMaxWidth()) {
            MDCustomIconRadio(
                Modifier,
                stringResource(R.string.alertDialogTextYes),
                optionList[0].isChecked,
                { isChecked ->
                    onValueChange(isChecked, optionList[0])
                },
                false,
                enable
            )
            MDCustomIconRadio(
                Modifier.padding(start = 16.dp),
                stringResource(R.string.alertDialogTextNo),
                optionList[1].isChecked,
                { isChecked ->
                    onValueChange(isChecked, optionList[1])
                },
                false,
                enable
            )
        }
        if (optionList[0].isChecked && optionList[0].isOther) {
            MdOutlinedTextField(
                modifier = Modifier
                    .padding(
                        bottom = MdTheme.dimension.cardPadding,
                    )
                    .fillMaxWidth()
                    .padding(0.dp),
                value = optionList[0].otherValue ?: "",
                onValueChange = {
                    onOtherValueChange(it, optionList[0])
                },
                style = MdTheme.typography.regular14TextStyle
            )
        } else if (optionList[1].isChecked && optionList[1].isOther) {
            MdOutlinedTextField(
                modifier = Modifier
                    .padding(
                        bottom = MdTheme.dimension.cardPadding,
                    )
                    .fillMaxWidth()
                    .padding(0.dp),
                value = optionList[1].otherValue ?: "",
                onValueChange = {
                    onOtherValueChange(it, optionList[1])
                },
                style = MdTheme.typography.regular14TextStyle
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MDYesOrNoComponentPreview(modifier: Modifier = Modifier) {
    val optionList = arrayListOf(
        OptionData("Yes", true, isOther = true),
        OptionData("No", false)
    )

    MDYesOrNoComponent(
        Modifier.padding(
            top = MdTheme.dimension.cardPadding,
            start = MdTheme.dimension.cardPadding,
            end = MdTheme.dimension.cardPadding,
            bottom = MdTheme.dimension.cardPadding
        ), true, optionList, { isChecked, optionData -> }, { it, optionData -> }
    )
}