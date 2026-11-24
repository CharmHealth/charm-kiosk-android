package com.charm.ehr.kiosk.presentation.questionnaire

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charm.ehr.kiosk.domain.OptionData
import com.charm.ehr.kiosk.domain.Question
import com.charm.ehr.kiosk.domain.QuestionnaireType
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.domain.SOAPEntry
import com.charm.ehr.kiosk.domain.SOAPEntryType
import com.charm.ehr.kiosk.common.util.UIUtil
import com.charm.ehr.kiosk.presentation.composable.BottomBarWithPrimarySecondaryButton
import com.charm.ehr.kiosk.presentation.composable.MdAlertDialog
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdCircularProgressBar
import com.charm.ehr.kiosk.presentation.composable.MdProgressDialog
import com.charm.ehr.kiosk.presentation.composable.MdTopToolBar
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import com.charm.ehr.kiosk.presentation.theme.OpenSansFontFamily
import com.charm.ehr.kiosk.common.util.FileUtil
import com.charm.ehr.kiosk.common.util.FileUtil.getFileFromFilePath
import com.charm.ehr.kiosk.common.util.TemplateUIUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun QuestionnaireRootScreen(
    questionnaireViewModel: QuestionnaireDetailViewModel,
    onFinishClick: () -> Unit,
    onBackPress: () -> Unit
) {
    val state by questionnaireViewModel.questionnaireState.collectAsStateWithLifecycle()
    val questionnaireList by questionnaireViewModel.patientSharedQuestionnaires.collectAsStateWithLifecycle()
    var showOnBackPressDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        questionnaireViewModel.navigationEvents.collectLatest { event ->
            when (event) {
                QuestionnaireDetailViewModel.NavigationEvent.Back -> onBackPress()
                QuestionnaireDetailViewModel.NavigationEvent.Finish -> onFinishClick()
            }
        }
    }

    if (state.isLoading) {
        MdProgressDialog()
    }

    BackHandler(enabled = true) {
        if (state.isQuestionnaireValueUpdated) {
            showOnBackPressDialog = true
        } else {
            onBackPress()
        }
    }


    if (showOnBackPressDialog) {
        MdAlertDialog(
            heading = stringResource(R.string.unsaved_changes),
            message = stringResource(R.string.discard_changes_msg),
            primaryButtonResId = R.string.discard,
            secondaryButtonResId = R.string.cancel,
            onPrimaryAction = {
                onBackPress()
                showOnBackPressDialog = false
            },
            onSecondaryAction = {
                showOnBackPressDialog = false
            })
    }

    if (state.errorMessage != null) {
        MdAlertDialog(
            onSecondaryAction = {
                questionnaireViewModel.clearError()
                if (questionnaireViewModel.isInitialLoadFailed) {
                    onBackPress()
                }
            },
            heading = stringResource(R.string.api_not_success_error),
            message = state.errorMessage?.asString() ?: "",
            primaryButtonResId = R.string.retry,
            onPrimaryAction = {
                questionnaireViewModel.retryLastFailedAction()
            },
            secondaryButtonResId = R.string.cancel,
        )
    }

    QuestionScreen(state, totalQuestion = questionnaireList?.size ?: 0) {
        if (it == QuestionnaireDetailAction.OnBackPress) {
            if (state.isQuestionnaireValueUpdated) {
                showOnBackPressDialog = true
            } else {
                onBackPress()
            }
        }
        questionnaireViewModel.onChangeAction(action = it)
    }
}

@Composable
fun QuestionScreen(
    questionScreenState: QuestionnaireDetailScreenState,
    totalQuestion: Int,
    onAction: (QuestionnaireDetailAction) -> Unit,
) {
    val focusEntryId = questionScreenState.focusEntryId
    val childFocusIndex = questionScreenState.childFocusIndex
    val context = LocalContext.current
    if (questionScreenState.toastMessage != null) {
        LaunchedEffect(questionScreenState.toastMessage) {
            Toast.makeText(
                context,
                questionScreenState.toastMessage,
                Toast.LENGTH_SHORT
            ).show()
            onAction(QuestionnaireDetailAction.ClearToastMessage)
        }
    }
    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MdColors.MdSurfaceColor)
                .windowInsetsPadding(
                    WindowInsets.systemBars.union(WindowInsets.displayCutout)
                )
        ) {
            if (questionScreenState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    MdCircularProgressBar()
                }
            } else if (questionScreenState.questionnaireMetaData != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        MdTopToolBar(
                            headingText = "(${
                                questionScreenState.selectedQuestionnaireIndex?.plus(
                                    1
                                )
                            }/$totalQuestion) ${questionScreenState.questionnaireMetaData.templateName}",
                            onBackClick = { onAction(QuestionnaireDetailAction.OnBackPress) }
                        )
                        MdCardWithoutTonalElevation(
                            modifier = Modifier
                                .padding(horizontal = MdTheme.dimension.cardPadding)
                                .weight(1f)
                        ) {
                            val listState = rememberLazyListState()
                            val lazyViewportRect = remember { mutableStateOf<Rect?>(null) }
                            val keyboardController = LocalSoftwareKeyboardController.current
                            LaunchedEffect(listState.isScrollInProgress) {
                                if (listState.isScrollInProgress) {
                                    keyboardController?.hide()
                                }
                            }
                            LaunchedEffect(questionScreenState.focusEntryId) {
                                if (questionScreenState.focusIndex >= 0) {
                                    // Scroll to the focused item
                                    listState.scrollToItem(questionScreenState.focusIndex)
                                }
                            }
                            if (questionScreenState.questionnaireMetaData.questions.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(MdTheme.dimension.horizontalPadding),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (questionScreenState.questionnaireMetaData.unSupportedQuestionnaireType.isNotEmpty()) {
                                        val unsupportedTypes =
                                            questionScreenState.questionnaireMetaData.unSupportedQuestionnaireType
                                        if (unsupportedTypes.contains(QuestionnaireType.PrimaryInsuranceDetails) || unsupportedTypes.contains(
                                                QuestionnaireType.PrimaryContactDetails
                                            ) || unsupportedTypes.contains(QuestionnaireType.PersonalDetails)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.demographics_contact_details_filled_msg),
                                                style = MdTheme.typography.semiBold18TextStyle
                                            )
                                        } else if (unsupportedTypes.contains(QuestionnaireType.Medications) || unsupportedTypes.contains(
                                                QuestionnaireType.Supplements
                                            ) || unsupportedTypes.contains(QuestionnaireType.Allergies)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.non_supported_questionnaire_msg),
                                                style = MdTheme.typography.semiBold18TextStyle
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = stringResource(R.string.no_questions_available),
                                            style = MdTheme.typography.semiBold18TextStyle
                                        )
                                    }
                                }
                                return@MdCardWithoutTonalElevation
                            }
                            // Flatten questions: for SOAPWidgetQuestion, add each entry as a separate item
                            val flatQuestions =
                                remember(questionScreenState.questionnaireMetaData.questions) {
                                    questionScreenState.questionnaireMetaData.questions.flatMap { q ->
                                        if (q is Question.SOAPWidgetQuestion) {
                                            q.soapWidget.entries.map { entry -> Pair(q, entry) }
                                        } else {
                                            listOf(Pair(q, null))
                                        }
                                    }
                                }
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .onGloballyPositioned { coords ->
                                        lazyViewportRect.value = coords.boundsInWindow()
                                    }
                                    .fillMaxSize()
                                    .padding(
                                        top = MdTheme.dimension.cardPadding,
                                        bottom = 72.dp,
                                    )
                            ) {
                                itemsIndexed(flatQuestions, key = { index, (question, soapEntry) ->
                                    question.entryId + (soapEntry?.soapEntryId ?: "")
                                }) { index, (question, soapEntry) ->
                                    if (soapEntry == null) {
                                        QuestionItem(
                                            question = question,
                                            onAnswerChanged = { question, answer, soapEntry, childQuestion ->
                                                onAction(
                                                    QuestionnaireDetailAction.AnswerChanged(
                                                        changedQuestion = question,
                                                        answer,
                                                        soapEntry,
                                                        childQuestion
                                                    )
                                                )
                                            },
                                            focus = questionScreenState.focusEntryId == question.entryId,
                                            modifier = Modifier,
                                            onFetchSignature = { signatureUrl, entryId ->
                                                onAction(
                                                    QuestionnaireDetailAction.FetchSignature(
                                                        signatureUrl,
                                                        entryId
                                                    )
                                                )
                                            },
                                            childFocusIndex = childFocusIndex,
                                            updateFocusEntryId = {
                                                onAction(
                                                    QuestionnaireDetailAction.UpdateFocusEntryId(
                                                        null,
                                                        -1,
                                                        -1,
                                                        false
                                                    )
                                                )
                                            },
                                            lazyListState = listState,
                                            lazyViewportRect = lazyViewportRect,
                                        )
                                    } else {
                                        // Safe cast to prevent ClassCastException
                                        val soapQuestion = question as? Question.SOAPWidgetQuestion
                                        if (soapQuestion != null) {
                                            SOAPEntryItem(
                                                question = soapQuestion,
                                                entry = soapEntry,
                                                onAnswerChanged = { q, answer, entry, _ ->
                                                    onAction(
                                                        QuestionnaireDetailAction.AnswerChanged(
                                                            q,
                                                            answer,
                                                            entry,
                                                            null
                                                        )
                                                    )
                                                },
                                                focus = focusEntryId == soapEntry.soapEntryId,
                                                isFromSection = false,
                                                updateFocusEntryId = {
                                                    onAction(
                                                        QuestionnaireDetailAction.UpdateFocusEntryId(
                                                            null,
                                                            -1,
                                                            -1,
                                                            false
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // Button bar logic
        BottomBarWithPrimarySecondaryButton(
            modifier = Modifier.align(Alignment.BottomCenter),
            primaryButtonText = when {
                questionScreenState.isSingleQuestionnaire || questionScreenState.isLastQuestionnaire -> stringResource(
                    R.string.finishStr
                )

                else -> stringResource(R.string.nextStr)
            },
            onPrimaryClick = {
                onAction(QuestionnaireDetailAction.PostQuestionnaire(true))
            },
            secondaryButtonText = stringResource(R.string.previous),
            onSecondaryClick = {
                onAction(QuestionnaireDetailAction.PostQuestionnaire(false))
            },
            showSecondary = !questionScreenState.isSingleQuestionnaire && !questionScreenState.isFirstQuestionnaire
        )
    }
}

@Composable
fun QuestionItem(
    question: Question,
    onAnswerChanged: (Question, String, SOAPEntry?, Question?) -> Unit = { _, _, _, _ -> },
    focus: Boolean = false,
    modifier: Modifier,
    isFromSection: Boolean = false,
    onFetchSignature: (signatureUrl: String, entryId: String?) -> Unit,
    childFocusIndex: Int,
    updateFocusEntryId: () -> Unit,
    lazyListState: LazyListState,
    lazyViewportRect: MutableState<Rect?>
) {
    val label = UIUtil.addMandatoryStar(
        question.notes ?: "",
        MdColors.bloodRed,
        question.isMandatory,
        MdTheme.dimension.large,
        fontWeight = FontWeight.Medium,
        fontFamily = OpenSansFontFamily,
        MdColors.Black86
    )

    val focusRequester = remember { FocusRequester() }
    val focusModifier = if (focus) modifier.focusRequester(focusRequester) else modifier
    LaunchedEffect(focus) {
        if (focus && question !is Question.Section) {
            focusRequester.requestFocus()
            updateFocusEntryId()
        }
    }

    when (question) {
        is Question.Label -> {
            TemplateUIUtil.RenderHeaderViewWithoutCheckBox(
                question.notes ?: "",
                question.fontSize ?: 14,
                question.textAlign ?: "",
                question.fontStyle ?: "",
                question.fontWeight ?: "",
                question.textDecoration ?: ""
            )
        }

        is Question.SimpleQuestion -> {
            TemplateUIUtil.RenderSimpleQuestion(
                modifier = focusModifier,
                question.answer ?: "",
                label,
                { newValue ->
                    onAnswerChanged(question, newValue, null, null)
                },
                isMultipleLine = question.isMultiLine,
                isNotes = false
            )
        }

        is Question.RatingScaleQuestion -> {
            TemplateUIUtil.RenderRatingScale(
                focusModifier
                    .padding(
                        top = MdTheme.dimension.halfCardPadding,
                        start = MdTheme.dimension.horizontalPadding,
                        end = MdTheme.dimension.horizontalPadding,
                        bottom = MdTheme.dimension.cardPadding
                    ),
                label,
                question.isMandatory,
                question.fromScale,
                question.toScale,
                question.answer?.toIntOrNull() ?: question.fromScale,
                onValueChange = {
                    onAnswerChanged(question, it.toString(), null, null)
                })
        }

        is Question.QuestionWithOptions -> {
            if (question.isMultiChoice) {
                val onValueChange: (List<OptionData>) -> Unit = { optionList ->
                    onAnswerChanged(
                        question.copy(
                            options = optionList
                        ), "", null, null
                    )
                }
                TemplateUIUtil.RenderMultiSelectComponent(
                    modifier = focusModifier,
                    labelText = label,
                    question.options,
                    false,
                    isAdditionalComments = false,
                    additionalComments = "",
                    onOptionChange = {
                        onValueChange(it)
                    })
            } else {
                val onValueChange: (Boolean, OptionData, List<OptionData>?) -> Unit =
                    { isChecked, optionData, updatedChildrenList ->
                        onAnswerChanged(
                            question.copy(
                                updatedValue = isChecked,
                                updatedOption = optionData,
                            ), "", null, null
                        )
                    }
                val onOtherValueChange: (String, OptionData) -> Unit =
                    { otherValue, optionData ->
                        onAnswerChanged(
                            question.copy(
                                updatedOtherValue = otherValue,
                                updatedOption = optionData,
                            ), "", null, null
                        )
                    }
                TemplateUIUtil.RenderSingleSelectComponent(
                    modifier = focusModifier,
                    labelText = label,
                    optionList = question.options,
                    onValueChange = onValueChange,
                    onOtherValueChange = onOtherValueChange
                )
            }
        }

        is Question.YesOrNoQuestion -> {
            val onValueChange: (Boolean, OptionData) -> Unit =
                { isChecked, optionData ->
                    onAnswerChanged(
                        question.copy(
                            updatedValue = isChecked,
                            updatedOption = optionData,
                        ), "", null, null
                    )
                }
            val onOtherValueChange: (String, OptionData) -> Unit =
                { otherValue, optionData ->
                    onAnswerChanged(
                        question.copy(
                            updatedOtherValue = otherValue,
                            updatedOption = optionData,
                        ), "", null, null
                    )
                }
            TemplateUIUtil.RenderYesOrNoQuestion(
                modifier = focusModifier,
                label,
                question.options,
                onValueChange,
                onOtherValueChange = onOtherValueChange
            )
        }

        is Question.DateQuestion -> {
            TemplateUIUtil.RenderDateQuestion(
                modifier = focusModifier,
                question.answer,
                label
            ) { newValue ->
                onAnswerChanged(question, newValue, null, null)
            }
        }

        is Question.SignatureQuestion -> {
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            val signatureFilePath = question.signatureFilePath
            var hasTried by remember(question.entryId) { mutableStateOf(false) }
            val signatureUrl = question.signatureUrl
            if (!signatureUrl.isNullOrBlank() && signatureFilePath == null) {
                LaunchedEffect(signatureUrl, hasTried) {
                    if (!hasTried) {
                        onFetchSignature(signatureUrl, question.entryId)
                        hasTried = true
                    }
                }
            }
            var signatureBitmap by remember { mutableStateOf<Bitmap?>(null) }
            LaunchedEffect(question.signatureFilePath) {
                if (!signatureFilePath.isNullOrBlank()) {
                    val file = signatureFilePath.getFileFromFilePath()
                    file?.let {
                        signatureBitmap = FileUtil.fileToBitmap(it)
                    }
                }
            }
            TemplateUIUtil.RenderSignature(
                modifier = focusModifier,
                label,
                signatureBitmap,
                onSignatureChanged = {
                    if (it != null) {
                        coroutineScope.launch {
                            val newFile = FileUtil.bitmapToFile(
                                context = context,
                                bitmap = it,
                                format = Bitmap.CompressFormat.PNG
                            )
                            val newFilePath = newFile?.absolutePath
                            if (newFilePath != null) {
                                onAnswerChanged(
                                    question,
                                    newFilePath,
                                    null,
                                    null
                                )
                            }
                        }
                    }
                },
                isLoading = question.isLoading,
                onRetry = {
                    hasTried = false
                    if (!signatureUrl.isNullOrBlank()) {
                        onFetchSignature(signatureUrl, question.entryId)
                    }
                },
                isRetry = question.isError
            )
        }

        is Question.Section -> {
            var expanded by remember { mutableStateOf(question.expandByDefault) }
            LaunchedEffect(focus) {
                if (focus && !expanded) {
                    expanded = true
                }
            }
            TemplateUIUtil.SectionCard(
                title = question.notes ?: "",
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .padding(MdTheme.dimension.horizontalPadding),
                itemCount = question.questions.size,
                scrollToIndex = childFocusIndex,
                isCollapsible = question.isCollapsible,
                expanded = expanded,
                onExpandedChange = {
                    expanded = it
                },
                itemContent = { index, modifier ->
                    val childQuestion = question.questions[index]
                    QuestionItem(
                        question = childQuestion,
                        onAnswerChanged = { childQuestion, value, entry, child ->
                            onAnswerChanged(question, value, entry, childQuestion)
                        },
                        focus = focus && childFocusIndex == index,
                        modifier = modifier,
                        isFromSection = true,
                        onFetchSignature = onFetchSignature,
                        childFocusIndex = childFocusIndex,
                        updateFocusEntryId = {
                            updateFocusEntryId()
                        },
                        lazyListState = lazyListState,
                        lazyViewportRect
                    )
                },
                focus = focus,
                lazyListState = lazyListState,
                lazyViewportRectProvider = { lazyViewportRect.value }
            )
        }

        is Question.SOAPWidgetQuestion -> {
            SOAPWidgetQuestionItem(
                question,
                onAnswerChanged,
                focus,
                isFromSection,
                updateFocusEntryId = updateFocusEntryId
            )
        }
    }
}

@Composable
fun SOAPWidgetQuestionItem(
    question: Question.SOAPWidgetQuestion,
    onAnswerChanged: (Question, String, SOAPEntry?, Question?) -> Unit = { _, _, _, _ -> },
    focus: Boolean,
    isFromSection: Boolean,
    updateFocusEntryId: () -> Unit
) {
    val widget = question.soapWidget
    widget.entries.forEach { entry ->
        SOAPEntryItem(
            question, entry,
            onAnswerChanged = onAnswerChanged,
            focus = focus,
            isFromSection = isFromSection,
            updateFocusEntryId = updateFocusEntryId
        )

    }
}

@Composable
fun SOAPEntryItem(
    question: Question.SOAPWidgetQuestion,
    entry: SOAPEntry,
    onAnswerChanged: (Question, String, SOAPEntry?, Question?) -> Unit = { _, _, _, _ -> },
    focus: Boolean = false,
    isFromSection: Boolean,
    updateFocusEntryId: () -> Unit
) {
    val label = UIUtil.addMandatoryStar(
        entry.soapEntryName ?: "",
        MdColors.bloodRed,
        entry.isMandatory,
        MdTheme.dimension.large,
        fontWeight = FontWeight.Medium,
        fontFamily = OpenSansFontFamily,
        MdColors.Black86
    )

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focus) {
        if (focus) {
            focusRequester.requestFocus()
            updateFocusEntryId()
        }
    }


    when (entry) {
        is SOAPEntry.HeadingEntry -> {
            val showCheckBox = false
            TemplateUIUtil.GetSOAPHeaderView(
                entry.soapEntryName ?: "",
                entry.fontSize ?: 14,
                entry.textAlign ?: "",
                entry.fontStyle ?: "",
                entry.fontWeight ?: "",
                entry.textDecoration ?: "",
                false
            ) {
                if (showCheckBox) {
                    if (it) {
                        onAnswerChanged(
                            question, entry.soapEntryName ?: "", entry, null
                        )
                    } else {
                        onAnswerChanged(
                            question, "", entry, null
                        )
                    }

                }
            }
        }

        is SOAPEntry.SimpleQuestionEntry -> {
            val isMultipleLine = entry.isMultiLine
            TemplateUIUtil.RenderSimpleQuestion(
                modifier = Modifier.focusRequester(focusRequester),
                entry.answer,
                label,
                { newValue ->
                    if ((isMultipleLine && newValue.length <= 5000) || (!isMultipleLine && newValue.length <= 100)) {
                        onAnswerChanged(question, newValue, entry, null)
                    }
                },
                isMultipleLine = isMultipleLine,
                false
            )
        }

        is SOAPEntry.CheckboxEntry -> {
            val onValueChange: (List<OptionData>) -> Unit = { optionList ->
                onAnswerChanged(
                    question, "", entry.copy(
                        optionList = optionList
                    ), null
                )
            }
            val onCommentChange: (String) -> Unit = { comment ->
                onAnswerChanged(
                    question, "", entry.copy(
                        updatedCommentValue = comment
                    ), null
                )
            }

            TemplateUIUtil.RenderMultiSelectComponent(
                modifier = Modifier.focusRequester(focusRequester),
                label,
                entry.optionList,
                entry.soapEntryType == SOAPEntryType.CheckList,
                entry.isAdditionalComments,
                entry.additionalComment ?: "",
                onOptionChange = {
                    onValueChange(it)
                }, onCommentsChange = onCommentChange
            )
        }

        is SOAPEntry.RadioEntry -> {
            val onValueChange: (Boolean, OptionData, List<OptionData>?) -> Unit =
                { isChecked, optionData, updatedChildrenList ->

                    onAnswerChanged(
                        question, "", entry.copy(
                            updatedValue = isChecked,
                            updatedOption = optionData,
                            updatedChildrenList = updatedChildrenList
                        ), null
                    )
                }

            val onOtherValueChange: (String, OptionData) -> Unit =
                { otherValue, optionData ->
                    onAnswerChanged(
                        question, "", entry.copy(
                            updatedOption = optionData,
                            updatedOtherValue = otherValue
                        ), null
                    )
                }
            val onCommentChange: (String) -> Unit = { comment ->
                onAnswerChanged(
                    question, "", entry.copy(
                        updatedCommentValue = comment
                    ), null
                )
            }

            TemplateUIUtil.RenderSingleSelectComponent(
                modifier = Modifier.focusRequester(focusRequester),
                labelText = label,
                entry.optionList,
                entry.isAdditionalComments,
                entry.additionalComment ?: "",
                onValueChange = onValueChange,
                onOtherValueChange = onOtherValueChange,
                onCommentsChange = onCommentChange
            )
        }

        is SOAPEntry.TableEntry -> {
            val tableData = entry.soapWidgetTableData
            val onCellValueChange = { row: Int, col: Int, value: String ->
                onAnswerChanged(
                    question, value, entry.copy(
                        rowIndex = row, colIndex = col
                    ), null
                )
            }
            var totalPadding =
                (MdTheme.dimension.cardPadding * 2) + (MdTheme.dimension.horizontalPadding * 2)
            if (isFromSection) {
                totalPadding += (MdTheme.dimension.horizontalPadding * 2)
            }
            TemplateUIUtil.RenderTableComponent(
                tableData, modifier = Modifier
                    .focusRequester(focusRequester)
                    .padding(
                        bottom = MdTheme.dimension.doublePadding,
                        start = MdTheme.dimension.horizontalPadding,
                        end = MdTheme.dimension.horizontalPadding
                    ), onCellValueChange = onCellValueChange, totalOuterPadding = totalPadding
            )
        }

        is SOAPEntry.TextBoxEntry -> {
            var answer = entry.answer
            if (answer.isNullOrBlank()) {
                answer = entry.soapEntryName
            }
            TemplateUIUtil.RenderSimpleQuestion(
                modifier = Modifier.focusRequester(focusRequester), answer, label, { newValue ->
                    if (newValue.length <= 5000) {
                        onAnswerChanged(question, newValue, entry, null)
                    }
                }, isMultipleLine = true, isNotes = true
            )
        }

        is SOAPEntry.RatingScaleEntry -> {

            TemplateUIUtil.RenderRatingScale(
                Modifier
                    .focusRequester(focusRequester)
                    .padding(
                        top = MdTheme.dimension.halfCardPadding,
                        start = MdTheme.dimension.horizontalPadding,
                        end = MdTheme.dimension.horizontalPadding,
                        bottom = MdTheme.dimension.cardPadding
                    ),
                label,
                entry.isMandatory,
                entry.fromScale,
                entry.toScale,
                entry.answer?.toIntOrNull() ?: entry.fromScale,
                onValueChange = {
                    onAnswerChanged(question, it.toInt().toString(), entry, null)
                })
        }

        is SOAPEntry.YesOrNoEntry -> {
            val onValueChange: (Boolean, OptionData) -> Unit =
                { isChecked, optionData ->

                    onAnswerChanged(
                        question, "", entry.copy(
                            updatedValue = isChecked,
                            updatedOption = optionData,
                        ), null
                    )
                }
            val onOtherValueChange: (String, OptionData) -> Unit =
                { otherValue, optionData ->

                    onAnswerChanged(
                        question, "", entry.copy(
                            updatedOption = optionData,
                            updatedOtherValue = otherValue
                        ), null
                    )
                }
            TemplateUIUtil.RenderYesOrNoQuestion(
                modifier = Modifier.focusRequester(focusRequester),
                label,
                entry.optionList,
                onValueChange,
                onOtherValueChange = onOtherValueChange
            )
        }

        is SOAPEntry.ImageEntry -> {
            TemplateUIUtil.UnsupportedImageTypeBox()
        }

    }
}
