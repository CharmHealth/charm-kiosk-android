package com.charm.ehr.kiosk.presentation.composable


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.domain.OptionData
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import com.charm.ehr.kiosk.common.util.TemplateUIUtil

@Composable
fun MDNestedRadioButton(
    modifier: Modifier,
    list: List<OptionData>,
    enable: Boolean = true,
    onValueChange: (Boolean, OptionData, List<OptionData>?) -> Unit,
    onOtherValueChange: (String, OptionData) -> Unit,
    isAdditionalComments: Boolean = false,
    additionalComments: String = "",
    onCommentChange: (String) -> Unit = {}
) {
    val lastOption = list.last()

    Column(
        modifier
            .selectableGroup()
            .padding(top = 4.dp, bottom = 4.dp)
    ) {
        list.forEach { optionData ->
            RenderRadioLayout(
                optionData,
                lastOption == optionData,
                onValueChange,
                enable,
                onOtherValueChange
            )
        }
        if (isAdditionalComments) {
            TemplateUIUtil.RenderCommentField(additionalComments, onCommentChange)
        }
    }
}


@Composable
fun RenderRadioLayout(
    optionData: OptionData,
    isLast: Boolean,
    onValueChange: (Boolean, OptionData, List<OptionData>?) -> Unit,
    enable: Boolean,
    onOtherValueChange: (String, OptionData) -> Unit
) {
    val isParent = optionData.childrenOptionList?.isNotEmpty() == true
    MDCustomIconRadio(
        Modifier.fillMaxWidth(),
        optionData.optionString ?: "",
        optionData.isChecked,
        onValueChange = { isChecked ->
            onValueChange(isChecked, optionData, null)
        },
        isParent,
        enable
    )
    if (optionData.isOther && optionData.isChecked) {
        MdOutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, end = 4.dp),
            value = optionData.otherValue ?: "",
            onValueChange = { changedText ->
                onOtherValueChange(changedText, optionData)
            },
            style = MdTheme.typography.regular14TextStyle,
            enabled = enable
        )
    }
    if (isParent && optionData.isChecked) {
        val childrenVerticalLine = arrayListOf<Boolean>()
        if (optionData.viewLevel > 0) {
            childrenVerticalLine.add(!isLast)
        }
        MDNestedCheckBox(
            optionData.childrenOptionList, childrenVerticalLine, true, enable
        ) { updatedOptionList ->
            onValueChange(true, optionData, updatedOptionList)
        }
    }
}

@Composable
fun MDNestedCheckBox(
    list: List<OptionData>?,
    verticalLineArr: ArrayList<Boolean>,
    isRadio: Boolean = false,
    enable: Boolean,
    isAdditionalComments: Boolean = false,
    additionalComments: String = "",
    onCommentChange: (String) -> Unit = {},
    onOptionChange: (List<OptionData>) -> Unit,
) {
    val lastOption = if (list?.isNotEmpty() == true) {
        list.last()
    } else {
        null
    }

    list?.forEach { option ->
        Column {
            RenderCheckBoxLayout(
                option,
                lastOption == option,
                verticalLineArr,
                isRadio,
                enable,
                onCheckedChange = { isChecked, childrenList ->
                    val updatedList = if (childrenList != null) {
                        list.map {
                            if (it == option) {
                                it.copy(childrenOptionList = childrenList)
                            } else {
                                it
                            }
                        }
                    } else {
                        list.map {
                            if (it == option) {
                                it.copy(isChecked = isChecked)
                            } else {
                                it
                            }
                        }
                    }
                    onOptionChange(updatedList)
                },
                onOtherValueChange = { newOtherValue ->
                    val updated = option.copy(otherValue = newOtherValue)
                    val updatedList = list.map {
                        if (it == option) {
                            updated
                        } else {
                            it
                        }
                    }
                    onOptionChange(updatedList)
                })
        }
    }

    if (isAdditionalComments) {
        TemplateUIUtil.RenderCommentField(additionalComments, onCommentChange)
    }
}

@Composable
fun RenderCheckBoxLayout(
    optionData: OptionData,
    isLast: Boolean,
    verticalLineArr: ArrayList<Boolean>,
    isRadio: Boolean,
    enable: Boolean,
    onCheckedChange: (Boolean, List<OptionData>?) -> Unit,
    onOtherValueChange: (String) -> Unit
) {
    val isParent = optionData.childrenOptionList?.isNotEmpty() == true
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (optionData.viewLevel > 0) {
            RenderDividerLayout(Modifier.align(Alignment.Top), isLast, verticalLineArr, isRadio)
        }

        MDCustomIconCheckBox(
            optionData.optionString ?: "",
            optionData.isChecked,
            onValueChange = { onCheckedChange(it, null) },
            isParent,
            enable,
            textStyle = MdTheme.typography.regular14TextStyle
        )
    }
    if (optionData.isOther && optionData.isChecked) {
        MdOutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, end = 4.dp),
            value = optionData.otherValue ?: "",
            onValueChange = onOtherValueChange,
            style = MdTheme.typography.regular14TextStyle
        )
    }
    if (isParent && optionData.isChecked) {
        val childrenVerticalLine = arrayListOf<Boolean>()
        childrenVerticalLine.addAll(verticalLineArr)
        if (optionData.viewLevel > 0) {
            childrenVerticalLine.add(!isLast)
        }
        MDNestedCheckBox(optionData.childrenOptionList, childrenVerticalLine, isRadio, enable) {
            // Create a copy with updated isChecked and propagate to parent
            onCheckedChange(true, it)
        }
    }
}

@Composable
fun RenderDividerLayout(
    modifier: Modifier = Modifier,
    isLast: Boolean,
    verticalLineArr: ArrayList<Boolean>,
    isRadio: Boolean
) {
    //Render Vertical Line
    var paddingStart: Dp = if (isRadio) 10.dp else 9.dp
    verticalLineArr.forEach {
        if (it) {
            MDVerticalLine(
                modifier
                    .padding(start = paddingStart), 0.5.dp, MdColors.Black60
            )
            paddingStart = 19.6.dp
        } else {
            paddingStart += 19.8.dp
        }
    }
    if (isLast) {
        MDVerticalLine(
            modifier
                .padding(start = paddingStart)
                .height(19.5.dp), 0.5.dp, MdColors.Black60
        )
    } else {
        MDVerticalLine(modifier.padding(start = paddingStart), 0.5.dp, MdColors.Black60)
    }
    MDHorizontalLine(
        modifier
            .width(10.dp)
            .padding(top = 19.dp), 0.5.dp, MdColors.Black60
    )
}


//region Preview
@Preview(showBackground = true)
@Composable
fun NestedCheckListPreview() {
    val optionList: ArrayList<OptionData> = arrayListOf()
    val opt1 = OptionData()
    opt1.isChecked = false
    opt1.optionString =
        "opt1"
    optionList.add(opt1)
    val optionList1: ArrayList<OptionData> = arrayListOf()
    val opt11 = OptionData()
    opt11.optionString =
        "opt1.1"
    opt11.viewLevel = 1
    val optionList11: ArrayList<OptionData> = arrayListOf()
    val opt111 = OptionData()
    opt111.optionString =
        "opt1.1.1"
    opt111.viewLevel = 2
    optionList11.add(opt111)
    val opt112 = OptionData()
    opt112.optionString =
        "opt1.1.2"
    opt112.viewLevel = 2
    optionList11.add(opt112)
    opt11.childrenOptionList = optionList11
    val optionList111: ArrayList<OptionData> = arrayListOf()
    val opt1111 = OptionData()
    opt1111.optionString = "opt1.1.1.1"
    opt1111.viewLevel = 3
    val optionList1111: ArrayList<OptionData> = arrayListOf()
    val opt11111 = OptionData()
    opt11111.optionString = "opt1.1.1.1.1"
    opt11111.viewLevel = 4
    optionList1111.add(opt11111)
    opt1111.childrenOptionList = optionList1111
    optionList111.add(opt1111)
    opt111.childrenOptionList = optionList111
    optionList1.add(opt11)
    val opt12 = OptionData()
    opt12.optionString = "opt1.2"
    opt12.viewLevel = 1
    optionList1.add(opt12)
    opt1.childrenOptionList = optionList1
    val opt2 = OptionData()
    opt2.optionString = "opt2"
    val optionList2: ArrayList<OptionData> = arrayListOf()
    val opt21 = OptionData()
    opt21.optionString = "opt2.1"
    opt21.viewLevel = 1
    optionList2.add(opt21)
    val opt22 = OptionData()
    opt22.optionString = "opt2.2"
    opt22.viewLevel = 1
    val optionList22: ArrayList<OptionData> = arrayListOf()
    val opt221 = OptionData()
    opt221.optionString = "opt2.2.1"
    opt221.viewLevel = 2
    optionList22.add(opt221)
    val opt222 = OptionData()
    opt222.optionString = "opt2.2.2"
    opt222.viewLevel = 2
    val optionList222: ArrayList<OptionData> = arrayListOf()
    val opt2221 = OptionData()
    opt2221.optionString = "opt2.2.2.1"
    opt2221.viewLevel = 3
    optionList222.add(opt2221)
    val opt2222 = OptionData()
    opt2222.optionString = "opt2.2.2.2"
    opt2222.viewLevel = 3
    optionList222.add(opt2222)
    opt222.childrenOptionList = optionList222
    optionList22.add(opt222)
    opt22.childrenOptionList = optionList22
    optionList2.add(opt22)
    opt2.childrenOptionList = optionList2
    optionList.add(opt2)
    val other = OptionData()
    other.optionString = "Other"
    other.isOther = true
    other.isChecked = true
    optionList.add(other)
    var list = rememberSaveable { mutableStateOf(optionList.toList()) }
    Column(
        modifier = Modifier
            .wrapContentSize()
            .padding(start = MdTheme.dimension.cardPadding, top = 4.dp, bottom = 4.dp)
    ) {
        MDNestedCheckBox(list.value, arrayListOf(), isRadio = false, enable = true) {
            list.value = it
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NestedRadioPreview() {
    val optionList: ArrayList<OptionData> = arrayListOf()
    val opt1 = OptionData()
    opt1.optionString =
        "opt1"
    optionList.add(opt1)
    val optionList1: ArrayList<OptionData> = arrayListOf()
    val opt11 = OptionData()
    opt11.optionString =
        "opt1.1"
    opt11.viewLevel = 1
    val optionList11: ArrayList<OptionData> = arrayListOf()
    val opt111 = OptionData()
    opt111.optionString =
        "opt1.1.1"
    opt111.viewLevel = 2
    optionList11.add(opt111)
    val opt112 = OptionData()
    opt112.optionString =
        "opt1.1.2"
    opt112.viewLevel = 2
    optionList11.add(opt112)
    opt11.childrenOptionList = optionList11
    val optionList111: ArrayList<OptionData> = arrayListOf()
    val opt1111 = OptionData()
    opt1111.optionString = "opt1.1.1.1"
    opt1111.viewLevel = 3
    val optionList1111: ArrayList<OptionData> = arrayListOf()
    val opt11111 = OptionData()
    opt11111.optionString = "opt1.1.1.1.1"
    opt11111.viewLevel = 4
    optionList1111.add(opt11111)
    opt1111.childrenOptionList = optionList1111
    optionList111.add(opt1111)
    opt111.childrenOptionList = optionList111
    optionList1.add(opt11)
    val opt12 = OptionData()
    opt12.optionString = "opt1.2"
    opt12.viewLevel = 1
    optionList1.add(opt12)
    opt1.childrenOptionList = optionList1
    val opt2 = OptionData()
    opt2.optionString = "opt2"
    val optionList2: ArrayList<OptionData> = arrayListOf()
    val opt21 = OptionData()
    opt21.optionString = "opt2.1"
    opt21.viewLevel = 1
    optionList2.add(opt21)
    val opt22 = OptionData()
    opt22.optionString = "opt2.2"
    opt22.viewLevel = 1
    val optionList22: ArrayList<OptionData> = arrayListOf()
    val opt221 = OptionData()
    opt221.optionString = "opt2.2.1"
    opt221.viewLevel = 2
    optionList22.add(opt221)
    val opt222 = OptionData()
    opt222.optionString = "opt2.2.2"
    opt222.viewLevel = 2
    val optionList222: ArrayList<OptionData> = arrayListOf()
    val opt2221 = OptionData()
    opt2221.optionString = "opt2.2.2.1"
    opt2221.viewLevel = 3
    optionList222.add(opt2221)
    val opt2222 = OptionData()
    opt2222.optionString = "opt2.2.2.2"
    opt2222.viewLevel = 3
    optionList222.add(opt2222)
    opt222.childrenOptionList = optionList222
    optionList22.add(opt222)
    opt22.childrenOptionList = optionList22
    optionList2.add(opt22)
    opt2.childrenOptionList = optionList2
    optionList.add(opt2)
    val other = OptionData()
    other.optionString = "Other"
    other.isOther = true
    other.isChecked = true
    optionList.add(other)
    val list by rememberSaveable { mutableStateOf(optionList.toList()) }

    MDNestedRadioButton(
        Modifier
            .wrapContentSize()
            .padding(top = 4.dp, bottom = 4.dp),
        list,
        onValueChange = { a, b, c -> },
        onOtherValueChange = { a, b -> },
        isAdditionalComments = false,
        additionalComments = ""
    )
}
//endregion



