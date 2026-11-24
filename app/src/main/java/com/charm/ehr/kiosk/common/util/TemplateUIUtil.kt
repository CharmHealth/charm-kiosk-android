package com.charm.ehr.kiosk.common.util


import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charm.ehr.kiosk.domain.OptionData
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.domain.SOAPTemplateTableCell
import com.charm.ehr.kiosk.presentation.composable.MDCustomIconCheckBox
import com.charm.ehr.kiosk.presentation.composable.MDNestedCheckBox
import com.charm.ehr.kiosk.presentation.composable.MDNestedRadioButton
import com.charm.ehr.kiosk.presentation.composable.MDSlider
import com.charm.ehr.kiosk.presentation.composable.MDTable
import com.charm.ehr.kiosk.presentation.composable.MDTextView
import com.charm.ehr.kiosk.presentation.composable.MDYesOrNoComponent
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdDateInputTextField
import com.charm.ehr.kiosk.presentation.composable.MdOutlinedTextField
import com.charm.ehr.kiosk.presentation.composable.PatientSignatureWithDialog
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdDimensions
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import com.charm.ehr.kiosk.presentation.theme.OpenSansFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object TemplateUIUtil {

    @Composable
    fun RenderLabelTextView(labelValue: AnnotatedString) {
        MDTextView(
            modifier = Modifier.padding(horizontal = MdTheme.dimension.horizontalPadding),
            value = labelValue,
            style = MdTheme.typography.medium16TextStyle
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun RenderDateQuestion(
        modifier: Modifier,
        answer: String?,
        labelText: AnnotatedString,
        onValueChange: (String) -> Unit
    ) {
        RenderLabelTextView(labelText)
        MdDateInputTextField(
            value = answer ?: "",
            textStyle = MdTheme.typography.regular14TextStyle,
            onValueChange = {
                onValueChange(it)
            },
            modifier = modifier
                .padding(
                    top = MdTheme.dimension.contentPadding,
                    bottom = MdTheme.dimension.doublePadding,
                    start = MdTheme.dimension.horizontalPadding,
                    end = MdTheme.dimension.horizontalPadding
                ),
            isError = false,
            labelText = "",
            placeholder = {
                Text(
                    text = ModelUtil.displayDateFormateMonthFirst,
                    style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Black40)
                )
            }
        )
    }


    @Composable
    fun RenderSimpleQuestion(
        modifier: Modifier,
        answer: String?,
        labelText: AnnotatedString,
        onValueChange: (String) -> Unit,
        isMultipleLine: Boolean,
        isNotes: Boolean
    ) {

        if (!isNotes) {
            RenderLabelTextView(labelText)
        }
        if (!isMultipleLine) {
            MdOutlinedTextField(
                modifier = modifier
                    .padding(
                        top = MdTheme.dimension.contentPadding,
                        bottom = MdTheme.dimension.doublePadding,
                        start = MdTheme.dimension.horizontalPadding,
                        end = MdTheme.dimension.horizontalPadding
                    )
                    .fillMaxWidth()
                    .padding(0.dp, 0.dp, 0.dp, 0.dp),
                value = answer ?: "",
                onValueChange = {
                    onValueChange(it)
                },
                style = MdTheme.typography.regular14TextStyle
            )
        } else {
            val textModifier =
                if (isNotes) modifier.padding(
                    bottom = MdTheme.dimension.doublePadding,
                    start = MdTheme.dimension.horizontalPadding,
                    end = MdTheme.dimension.horizontalPadding
                ) else modifier
                    .padding(
                        top = MdTheme.dimension.contentPadding,
                        bottom = MdTheme.dimension.doublePadding,
                        start = MdTheme.dimension.horizontalPadding,
                        end = MdTheme.dimension.horizontalPadding
                    )
            MdOutlinedTextField(
                modifier = textModifier
                    .wrapContentHeight()
                    .heightIn(min = 90.dp)
                    .fillMaxWidth()
                    .padding(0.dp, 0.dp, 0.dp, 0.dp),
                value = answer ?: "",
                onValueChange = {
                    onValueChange(it)
                },
                singleLine = false,
                style = MdTheme.typography.regular14TextStyle,
//                decorationBox = { innerTextField ->
//                    Box(
//                        modifier = Modifier
//                            .padding(8.dp)
//                            .fillMaxWidth()
//                    ) {
//                        innerTextField()
//                    }
//                }
            )
        }
    }

    @Composable
    fun RenderMultiSelectComponent(
        modifier: Modifier,
        labelText: AnnotatedString,
        optionList: List<OptionData>,
        isCheckList: Boolean = false,
        isAdditionalComments: Boolean = false,
        additionalComments: String = "",
        onCommentsChange: (String) -> Unit = {},
        onOptionChange: (List<OptionData>) -> Unit
    ) {
        if (!isCheckList) {
            RenderLabelTextView(labelText)
        }
        Column(
            modifier = modifier
                .padding(
                    start = MdTheme.dimension.horizontalPadding,
                    end = MdTheme.dimension.horizontalPadding,
                    bottom = MdTheme.dimension.doublePadding
                )
                .wrapContentSize()

        ) {
            MDNestedCheckBox(
                optionList,
                arrayListOf(),
                enable = true,
                onOptionChange = onOptionChange,
                isAdditionalComments = isAdditionalComments,
                additionalComments = additionalComments,
                onCommentChange = onCommentsChange
            )
        }
    }

    @Composable
    fun RenderSingleSelectComponent(
        modifier: Modifier,
        labelText: AnnotatedString,
        optionList: List<OptionData>,
        isAdditionalComments: Boolean = false,
        additionalComments: String = "",
        onCommentsChange: (String) -> Unit = {},
        onValueChange: (Boolean, OptionData, List<OptionData>?) -> Unit,
        onOtherValueChange: (String, OptionData) -> Unit
    ) {
        RenderLabelTextView(labelText)
        MDNestedRadioButton(
            modifier
                .wrapContentSize()
                .padding(
                    start = MdTheme.dimension.horizontalPadding,
                    end = MdTheme.dimension.horizontalPadding,
                    bottom = MdTheme.dimension.doublePadding
                ),
            optionList,
            true,
            onValueChange,
            onOtherValueChange,
            isAdditionalComments,
            additionalComments,
            onCommentChange = onCommentsChange
        )
    }

    @Composable
    fun RenderYesOrNoQuestion(
        modifier: Modifier,
        labelText: AnnotatedString,
        optionList: List<OptionData>,
        onValueChange: (Boolean, OptionData) -> Unit,
        onOtherValueChange: (String, OptionData) -> Unit
    ) {
        RenderLabelTextView(labelText)
        MDYesOrNoComponent(
            modifier.padding(
                start = MdTheme.dimension.horizontalPadding,
                end = MdTheme.dimension.horizontalPadding,
                bottom = MdTheme.dimension.doublePadding
            ),
            true, optionList, onValueChange, onOtherValueChange
        )
    }

    @Composable
    fun RenderHeaderView(
        labelText: String,
        entryAnswer: String?,
        fontSize: Int,
        textAlignment: String,
        fontStyle: String,
        fontWeight: String,
        fontDecoration: String,
        onValueChange: (Boolean) -> Unit
    ) {
        val isCheckedBoolean = !entryAnswer.isNullOrBlank()
        var isChecked by remember { mutableStateOf(isCheckedBoolean) }
        var alignment = Arrangement.Start
        var textAlign: TextAlign? = null
        when (textAlignment) {

            ModelUtil.questionnaireLabelAlignmentRight -> {
                alignment = Arrangement.End
            }

            ModelUtil.questionnaireLabelAlignmentCenter -> {
                alignment = Arrangement.Center
            }

            ModelUtil.questionnaireLabelAlignmentJustify -> {
                textAlign = TextAlign.Justify
            }
        }
        var textStyle = MdTheme.typography.regular14TextStyle.copy(fontSize = fontSize.sp)
        if (fontDecoration.equals(
                ModelUtil.questionnaireLabelAlignmentUnderline,
                true
            ) || fontDecoration.contains("underline")
        ) {
            textStyle = textStyle.copy(textDecoration = TextDecoration.Underline)
        }
        if (fontWeight.equals(
                ModelUtil.questionnaireLabelAlignmentBold,
                true
            ) || fontWeight == "700"
        ) {
            textStyle = textStyle.copy(fontWeight = FontWeight.Bold)
        }

        if (fontStyle.equals(ModelUtil.questionnaireLabelAlignmentItalics, true)) {
            textStyle = textStyle.copy(fontStyle = FontStyle.Italic)
        }
        Column(
            Modifier.padding(
                start = MdTheme.dimension.horizontalPadding,
                end = MdTheme.dimension.horizontalPadding,
                bottom = MdTheme.dimension.contentPadding
            )
        ) {
            MDCustomIconCheckBox(
                labelText, isChecked, {
                    isChecked = it
                    onValueChange(it)
                },
                hasChildren = false,
                enable = true,
                textStyle = textStyle, horizontalArrangement = alignment, textAlign = textAlign
            )
        }
    }

    @Composable
    fun RenderRatingScale(
        modifier: Modifier,
        labelText: AnnotatedString,
        isMandatory: Boolean,
        minValue: Int,
        maxValue: Int,
        answer: Int,
        onValueChange: (Int) -> Unit
    ) {
        RenderLabelTextView(labelText)

        MDSlider(
            modifier, answer, {
                onValueChange(it)
            }, minValue, maxValue
        )
    }

    @Composable
    fun RenderTableComponent(
        tableData: ArrayList<ArrayList<SOAPTemplateTableCell>>, isEnable: Boolean = true,
        modifier: Modifier,
        onCellValueChange: (row: Int, col: Int, value: String) -> Unit,
        totalOuterPadding: Dp
    ) {
        val totalColSpan = calculateTotalColSpan(tableData)
        MDTable(
            modifier,
            tableData,
            totalColSpan,
            isEnable = isEnable,
            onCellValueChange = onCellValueChange,
            totalOuterPadding = totalOuterPadding
        )
    }

    private fun calculateTotalColSpan(tableData: ArrayList<ArrayList<SOAPTemplateTableCell>>): Int {
        var totalCalSpan = 0
        tableData.firstOrNull()?.forEach {
            if (it.colspan.isNotBlank())
                totalCalSpan += it.colspan.toInt()
        }
        return totalCalSpan
    }


    @Composable
    fun RenderHeaderViewWithoutCheckBox(
        answer: String,
        fontSize: Int,
        textAlignment: String,
        fontStyle: String,
        fontWeight: String,
        fontDecoration: String
    ) {
        var alignment = Arrangement.Start
        var textAlign: TextAlign? = null
        when (textAlignment) {

            ModelUtil.questionnaireLabelAlignmentRight -> {
                alignment = Arrangement.End
            }

            ModelUtil.questionnaireLabelAlignmentCenter -> {
                alignment = Arrangement.Center
            }

            ModelUtil.questionnaireLabelAlignmentJustify -> {
                textAlign = TextAlign.Justify
            }
        }
        var textStyle = MdTheme.typography.regular14TextStyle.copy(fontSize = fontSize.sp)
        if (fontDecoration.equals(
                ModelUtil.questionnaireLabelAlignmentUnderline,
                true
            ) || fontDecoration.contains("underline")
        ) {
            textStyle = textStyle.copy(textDecoration = TextDecoration.Underline)
        }
        if (fontWeight.equals(
                ModelUtil.questionnaireLabelAlignmentBold,
                true
            ) || fontWeight == "700"
        ) {
            textStyle = textStyle.copy(fontWeight = FontWeight.Bold)
        }

        if (fontStyle.equals(ModelUtil.questionnaireLabelAlignmentItalics, true)) {
            textStyle = textStyle.copy(fontStyle = FontStyle.Italic)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = alignment) {
            MDTextView(
                value = answer, modifier = Modifier
                    .padding(
                        horizontal = MdTheme.dimension.horizontalPadding,
                        vertical = MdTheme.dimension.halfCardPadding
                    ), style = textStyle, textAlign = textAlign
            )
        }
    }

    suspend fun optimizedScrollToItem(listState: LazyListState, targetIndex: Int) {
        val layoutInfo = listState.layoutInfo
        val currentIndex = listState.firstVisibleItemIndex

        val itemsPerScreen = layoutInfo.visibleItemsInfo.size
        if (itemsPerScreen <= 0) return

        // Calculate the intermediate index for the jump scroll
        val intermediateIndex = when {
            targetIndex > currentIndex -> (currentIndex + itemsPerScreen).coerceAtMost(targetIndex)
            targetIndex < currentIndex -> (currentIndex - itemsPerScreen).coerceAtLeast(targetIndex)
            else -> currentIndex
        }

        listState.scrollToItem(intermediateIndex)

        listState.animateScrollToItem(targetIndex)

    }

    @Composable
    fun GetSOAPHeaderView(
        headingText: String,
        fontSize: Int,
        textAlignment: String,
        fontStyle: String,
        fontWeight: String,
        fontDecoration: String, isCheckBoxShown: Boolean,
        onValueChange: (Boolean) -> Unit
    ) {
        val entryName = UIUtil.addMandatoryStar(
            headingText,
            MdColors.bloodRed,
            false,
            MdTheme.dimension.medium,
            fontWeight = FontWeight.Medium,
            fontFamily = OpenSansFontFamily,
            MdColors.Black86
        )

        if (isCheckBoxShown) {
            RenderHeaderView(
                entryName.toString(),
                headingText,
                fontSize,
                textAlignment,
                fontStyle,
                fontWeight,
                fontDecoration,
                onValueChange = {
                    onValueChange(it)
                })
        } else {
            RenderHeaderViewWithoutCheckBox(
                headingText,
                fontSize,
                textAlignment,
                fontStyle,
                fontWeight,
                fontDecoration
            )
        }
    }

    @SuppressLint("ConfigurationScreenWidthHeight")
    @Composable
    fun isTablet(): Boolean {
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp
        return screenWidthDp >= 600 // Tablets are typically 600dp or wider
    }

    @Composable
    fun UnsupportedImageTypeBox() {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .border(1.dp, MdColors.LightGray, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                MDTextView(
                    value = stringResource(R.string.image_type_not_supported),
                    style = MdTheme.typography.regular14TextStyle
                )
            }
        }
    }

    @Composable
    fun SectionCard(
        title: String,
        modifier: Modifier,
        itemCount: Int,
        scrollToIndex: Int? = null,
        isCollapsible: Boolean = true,
        expanded: Boolean,
        onExpandedChange: ((Boolean) -> Unit),
        itemContent: @Composable (index: Int, modifier: Modifier) -> Unit,
        focus: Boolean,
        lazyListState: LazyListState? = null,
        lazyViewportRectProvider: () -> Rect? = { null },
    ) {
        val isExpanded = expanded
        val bringIntoViewRequesters = remember(itemCount) {
            List(itemCount) { BringIntoViewRequester() }
        }
        val childOffsets = remember { mutableMapOf<Int, Float>() }
        val coroutineScope = rememberCoroutineScope()
        val childRects = remember { mutableMapOf<Int, Rect>() }




        MdCardWithoutTonalElevation(
            modifier = modifier,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MdColors.blackishGrey),
            elevation = 0.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MdColors.BrandLightGreyishPink)
                        .let { if (isCollapsible) it.clickable { onExpandedChange.invoke(!isExpanded) } else it }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MdTheme.typography.semiBold16TextStyle,
                        modifier = Modifier.weight(1f)
                    )
                    if (isCollapsible) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_up),
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
                        )
                    }
                }
                AnimatedVisibility(
                    visible = !isCollapsible || isExpanded,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MdColors.White)
                ) {
                    if (!isCollapsible || isExpanded) {
                        Column {
                            for (i in 0 until itemCount) {
                                val mod = Modifier
                                    .onGloballyPositioned { coordinates ->
                                        childOffsets[i] = coordinates.positionInWindow().y
                                        childRects[i] = coordinates.boundsInWindow()
                                    }
                                    .bringIntoViewRequester(bringIntoViewRequesters[i])
                                    .onFocusEvent { focusState ->
                                        if (focus && scrollToIndex != null && scrollToIndex in bringIntoViewRequesters.indices && scrollToIndex == i) {
                                            coroutineScope.launch {
                                                delay(50)
                                                val viewport = lazyViewportRectProvider() ?: run {
                                                    // If we can't read viewport, fallback to bringIntoView
                                                    bringIntoViewRequesters[scrollToIndex].bringIntoView()
                                                    return@launch
                                                }
                                                val targetRect =
                                                    childRects[scrollToIndex] ?: return@launch
                                                val isVisible = targetRect.overlaps(viewport)
                                                if (!isVisible) {
                                                    // Only if completely offscreen, ask parents to bring it into view
                                                    bringIntoViewRequesters[scrollToIndex].bringIntoView()
                                                }

                                                val viewportHeightPx =
                                                    lazyListState?.layoutInfo?.viewportEndOffset
                                                        ?.minus(lazyListState.layoutInfo.viewportStartOffset)
                                                        ?.toFloat() ?: return@launch
                                                val centerY = viewportHeightPx / 2
                                                childOffsets[scrollToIndex]?.let {
                                                    val targetOffset = it - centerY
                                                    if (targetOffset != 0f) {
                                                        lazyListState.animateScrollBy(targetOffset)
                                                    }
                                                }

                                            }
                                        }
                                    }
                                itemContent(i, mod)
                            }
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun RenderCommentField(
        additionalComments: String = "",
        onCommentChange: (String) -> Unit
    ) {
        MDTextView(
            modifier = Modifier,
            value = stringResource(R.string.comment_str),
            style = MdTheme.typography.medium14TextStyle
        )
        MdOutlinedTextField(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(),
            value = additionalComments,
            onValueChange = onCommentChange,
            style = MdTheme.typography.regular14TextStyle,
        )
    }


    @Composable
    fun RenderSignature(
        modifier: Modifier,
        labelText: AnnotatedString, signatureBitmap: Bitmap?,
        onSignatureChanged: (Bitmap?) -> Unit, isLoading: Boolean = false,
        isRetry: Boolean = false,
        onRetry: () -> Unit = {},
    ) {
        RenderLabelTextView(labelText)
        PatientSignatureWithDialog(
            modifier,
            signatureBitmap = signatureBitmap,
            onSignatureChanged = onSignatureChanged,
            isRetry = isRetry,
            onRetry = onRetry,
            isLoading = isLoading
        )
    }


}

@Preview(showSystemUi = true)
@Composable
fun SectionCardPreview() {
    val itemCount = 8
    val scrollToIndex = 4 // Example: scroll to item 4
//    TemplateUIUtil.SectionCard(
//        title = "Sample Section",
//        modifier = Modifier.padding(8.dp),
//        itemCount = itemCount,
//        scrollToIndex = scrollToIndex,
//        expanded = false,
//        onExpandedChange = {},
//        itemContent = { index, mod ->
//            Text(
//                "Item $index: This is the content inside the section.",
//                modifier = mod.padding(8.dp)
//            )
//        },
//        focus = false
    //  )
}

@Composable
fun StyledNotesText(notes: String, dimensions: MdDimensions) {
    Text(
        buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontFamily = OpenSansFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = dimensions.medium,
                    color = MdColors.Black86,
                )
            ) {
                append(notes)
            }
        }
    )
}
