package com.charm.ehr.kiosk.presentation.support

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdCommonPrimaryButton
import com.charm.ehr.kiosk.presentation.composable.MdCustomOutlineTextField
import com.charm.ehr.kiosk.presentation.composable.MdDropDownMenu
import com.charm.ehr.kiosk.presentation.composable.MdHorizontalSpace
import com.charm.ehr.kiosk.presentation.composable.MdTopToolBar
import com.charm.ehr.kiosk.presentation.composable.MdVerticalSpace
import com.charm.ehr.kiosk.presentation.composable.autoBringIntoView
import com.charm.ehr.kiosk.presentation.edit_profile.FieldState
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import java.io.File

@Composable
fun SupportScreen(state: SupportState, onAction: (SupportScreenAction) -> Unit, imageLoader: ImageLoader) {

    val subjectFocusRequester = remember { FocusRequester() }
    val messageFocusRequester = remember { FocusRequester() }
    var isCategoryDropDownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.focusRequest) {
        when (state.focusRequest) {
            SupportState.FocusField.CATEGORY -> isCategoryDropDownExpanded = true
            SupportState.FocusField.SUBJECT -> subjectFocusRequester.requestFocus()
            SupportState.FocusField.MESSAGE -> messageFocusRequester.requestFocus()
            null -> Unit
        }
        if (state.focusRequest != null) {
            onAction(SupportScreenAction.ClearFocusRequest)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .background(MdColors.MdSurfaceColor)
    ) {

        MdTopToolBar(
            headingText = stringResource(R.string.contact_support),
            onBackClick = {
                onAction(SupportScreenAction.OnBackPressed)
            })

        Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        MdCardWithoutTonalElevation(modifier = Modifier
                .wrapContentHeight()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {

                    MdCustomOutlineTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = stringResource(R.string.support_email_address),
                        enabled = false,
                        onValueChange = {

                        },
                        labelText = "To",
                    )

                    MdVerticalSpace(2f)

                    MdDropDownMenu(
                        modifier = Modifier
                            .fillMaxWidth(),
                        textStyle = MdTheme.typography.medium14TextStyle,
                        options = state.categoryOptions.value.orEmpty(),
                        selectedOption = state.category.value.orEmpty(),
                        onOptionSelected = {
                            onAction(SupportScreenAction.UpdateSupportField(category = it))
                        },
                        labelText = stringResource(id = R.string.category),
                        optionToString = {
                            it
                        },
                        isError = (state.category.error != null),
                        itemTextStyle = MdTheme.typography.medium14TextStyle,
                        expanded = isCategoryDropDownExpanded,
                        onExpandedChange = { isCategoryDropDownExpanded = it },
                        isMandatory = true
                    )

                    MdVerticalSpace(2f)

                    MdCustomOutlineTextField(
                        modifier = Modifier
                            .autoBringIntoView()
                            .fillMaxWidth()
                            .focusRequester(subjectFocusRequester),
                        value = state.subject.value.orEmpty(),
                        onValueChange = {
                            onAction(SupportScreenAction.UpdateSupportField(subject = it))
                        },
                        labelText = stringResource(R.string.subject),
                        isError = state.subject.error != null,
                        isMandatory = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                    ) {

                        state.subject.error?.let {
                            Text(
                                modifier = Modifier.padding(horizontal = 4.dp),
                                text = it.asString(),
                                style = MdTheme.typography.medium14TextStyle.copy(color = MdColors.Red),
                            )
                        }

                        Text(
                            text = state.subjectCharacterDisplay.value.orEmpty(),
                            style = MdTheme.typography.medium14TextStyle.copy(color = MdColors.Black40),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 4.dp),
                            textAlign = TextAlign.End
                        )
                    }


                    MdVerticalSpace(0.5f)

                    MdCustomOutlineTextField(
                        modifier = Modifier
                            .autoBringIntoView()
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 500.dp)
                            .focusRequester(messageFocusRequester),
                        isError = state.message.error != null,
                        value = state.message.value.orEmpty(),
                        onValueChange = {
                            onAction(SupportScreenAction.UpdateSupportField(message = it))
                        },
                        labelText = stringResource(R.string.message),
                        isMandatory = true,
                        maxLines = Int.MAX_VALUE
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                    ) {
                        state.message.error?.let {
                            Text(
                                text = it.asString(),
                                style = MdTheme.typography.medium14TextStyle.copy(color = MdColors.Red),
                                modifier = Modifier.padding(4.dp),
                            )
                        }
                        Text(
                            text = state.messageCharacterDisplay.value.orEmpty(),
                            style = MdTheme.typography.medium14TextStyle.copy(MdColors.Black40),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 4.dp),
                            textAlign = TextAlign.End
                        )
                    }

                    val fileEntries = state.filePathAndStatus.entries.toList()
                    val attachmentCount = fileEntries.size
                    val attachmentString = when (attachmentCount) {
                        1 -> stringResource(R.string.attachment)
                        in 2..Int.MAX_VALUE -> stringResource(R.string.attachments)
                        else -> ""
                    }


                    if (attachmentCount > 0) {

                        MdVerticalSpace()

                        Text(attachmentString)

                        MdVerticalSpace()

                        // Replaced LazyVerticalGrid (scrollable) with a measured two-column layout
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val pairs = fileEntries.chunked(2)
                            pairs.forEachIndexed { rowIndex, pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    pair.forEach { entry ->
                                        val (filePath, isLoading) = entry
                                        Box(modifier = Modifier.weight(1f)) {
                                            AttachmentItem(
                                                filePath = filePath,
                                                onViewAttachment = {
                                                    //TODO:Implement view logic
                                                },
                                                onRemove = {
                                                    onAction(SupportScreenAction.RemoveFile(File(filePath)))
                                                },
                                                imageLoader = imageLoader,
                                                isLoading = isLoading
                                            )
                                        }
                                    }
                                    if (pair.size == 1) {
                                        // fill remaining space when odd count
                                        Box(modifier = Modifier.weight(1f)) {}
                                    }
                                }
                                if (rowIndex < pairs.size - 1) {
                                    MdVerticalSpace()
                                }
                            }
                        }

                    }

                    Row(Modifier.padding(top = 12.dp)) {
                        OutlinedButton(
                            modifier = Modifier.height(44.dp),
                            onClick = {
                                onAction(SupportScreenAction.OnClickAddAttachment)
                            },
                            border = BorderStroke(1.dp, MdColors.Black10)
                        ) {
                            Row {
                                Icon(
                                    painter = painterResource(R.drawable.ic_attachment),
                                    tint = MdColors.Black40,
                                    contentDescription = "Add Attachment"
                                )
                                MdHorizontalSpace()
                                Text(text = stringResource(R.string.attach), style = MdTheme.typography.medium16TextStyle)
                                MdHorizontalSpace(.5f)
                            }
                        }

                        MdHorizontalSpace(modifier = Modifier.weight(1f))

                        MdCommonPrimaryButton(
                            modifier = Modifier.width(180.dp).height(44.dp),
                            text = stringResource(R.string.send),
                            onClick = {
                                onAction(SupportScreenAction.SendSupport)
                            }
                        )
                    }

                }
            }
        }

    }
}

@Composable
@Preview(showBackground = true, widthDp = 600, heightDp = 1200)
fun SupportScreenPreview() {
    MdTheme {
        SupportScreen(
            imageLoader = ImageLoader.Builder(LocalContext.current).build(),
            state = SupportState(
                category = FieldState("Technical Support"),
                fromAddress = FieldState("patient@gmail.com"),
                toAddress = "support@charmhealth.com",
                subject = FieldState("Issue with patient registration"),
                subjectCharacterDisplay = FieldState("32/100"),
                message = FieldState("I am experiencing difficulties with the patient registration form. The form does not save my changes after clicking the submit button."),
                messageCharacterDisplay = FieldState("145/500"),
            ), onAction = {

            })
    }

}