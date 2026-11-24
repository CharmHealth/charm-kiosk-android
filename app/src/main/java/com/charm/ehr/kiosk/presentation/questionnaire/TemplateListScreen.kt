package com.charm.ehr.kiosk.presentation.questionnaire

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.domain.ConsentForm
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.domain.QuestionnairePhysicianTemplate
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.composable.BottomBarWithPrimarySecondaryButton
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdTopToolBar
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun TemplateListScreen(
    state: TemplateListState, onAction: (TemplateListAction) -> Unit, onBackPress: () -> Unit,
    patient: Patient?,
    questionnaireList: List<QuestionnairePhysicianTemplate>?
) {

    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MdColors.MdSurfaceColor)
                .windowInsetsPadding(
                    WindowInsets.systemBars.union(WindowInsets.displayCutout)
                )
        ) {

            MdTopToolBar(
                headingText = patient?.fullName ?: "", onBackClick = onBackPress
            )

            MdCardWithoutTonalElevation(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxHeight(), shape = RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp
                )
            ) {

                LazyColumn(modifier = Modifier.fillMaxWidth()) {

                    if (questionnaireList?.isNotEmpty() == true) {
                        item {
                            Text(
                                text = "Questionnaires",
                                style = MdTheme.typography.semiBold18TextStyle,
                                modifier = Modifier.padding(
                                    start=MdTheme.dimension.contentCardPadding,
                                    top=MdTheme.dimension.contentCardPadding,
                                    end=MdTheme.dimension.contentCardPadding,
                                    bottom = MdTheme.dimension.cardPadding
                                )
                            )
                        }
                    }
                    questionnaireList?.forEach { questionnaireItem ->
                        item(key = questionnaireItem.templateId) {
                            TemplatesListItem(
                                displayString = questionnaireItem.name ?: "",
                                onSelectChange = {
                                    onAction(
                                        TemplateListAction.OnClickTemplate(
                                            questionnaireId = questionnaireItem.patientQuesMap?.patientQuesMapId
                                                ?: ""
                                        )
                                    )
                                },
                                isSelected = questionnaireItem.patientQuesMap?.isSubmitted == true,
                            )
                        }
                    }

                    if (state.consentFormList.isNotEmpty()) {
                        item {
                            Text(
                                text = "Consent forms",
                                style = MdTheme.typography.semiBold18TextStyle,
                                modifier = Modifier.padding(
                                    top = 20.dp,
                                    bottom = 4.dp,
                                    start = 14.dp,
                                    end = 16.dp
                                )
                            )
                        }
                    }
                    state.consentFormList.forEach { consentForm ->
                        item(key = consentForm.fileId) {
                            TemplatesListItem(
                                displayString = consentForm.fileName ?: "",
                                onSelectChange = {
                                    onAction(
                                        TemplateListAction.OnClickConsentForm(
                                            consentFormId = consentForm.fileId ?: ""
                                        )
                                    )
                                },
                                isSelected = false,
                            )
                        }
                    }

                }

            }

        }


        BottomBarWithPrimarySecondaryButton(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            onPrimaryClick = {
                if (questionnaireList?.all { it.patientQuesMap?.isSubmitted == true } == true) {
                    onAction(TemplateListAction.onFinishClick)
                } else {
                    val firstNotSubmitted =
                        questionnaireList?.firstOrNull { it.patientQuesMap?.isSubmitted != true }
                    if (firstNotSubmitted != null) {
                        onAction(
                            TemplateListAction.OnClickTemplate(
                                questionnaireId = firstNotSubmitted.patientQuesMap?.patientQuesMapId
                                    ?: ""
                            )
                        )
                    }
                }
            },
            primaryButtonText = if (questionnaireList?.all { it.patientQuesMap?.isSubmitted == true } == true) stringResource(
                R.string.finishStr
            ) else stringResource(R.string.nextStr),
        )
    }

}

@Composable
fun TemplatesListItem(
    displayString: String, isSelected: Boolean, onSelectChange: () -> Unit
) {
    Row(
        modifier = Modifier.padding(start=MdTheme.dimension.contentCardPadding, end=MdTheme.dimension.contentCardPadding)
            .heightIn(min = 65.dp)
            .fillMaxWidth()
            .clickable { onSelectChange() }, // Add clickable to Row
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconResource =
            if (isSelected) R.drawable.ic_checked_circle else R.drawable.ic_unchecked_circle
        Icon(
            painter = painterResource(iconResource),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.padding(end=16.dp)
                .clickable { onSelectChange() }
        )

        Text(
            modifier = Modifier.weight(1f),
            text = displayString,
            style = MdTheme.typography.medium16TextStyle,
        )
        Image(
            modifier = Modifier.padding(start=16.dp)
                .rotate(-90f)
            ,
            painter = painterResource(R.drawable.ic_arrow_down),
            contentDescription = null,
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewTemplateListScreen() {
    val sampleState = TemplateListState(
        consentFormList = listOf(
            ConsentForm(fileName = "Sample Consent Form 1", fileId = "11"),
            ConsentForm(fileName = "Sample Consent Form 2", fileId = "12"),
            ConsentForm(fileName = "Sample Consent Form 3", fileId = "13"),
        )
    )
    MdTheme {
        TemplateListScreen(
            state = sampleState, onAction = {}, onBackPress = {}, patient = Patient(
                fullName = "Lewis Hamilton",
            ), questionnaireList = listOf(
                QuestionnairePhysicianTemplate(name = "Sample Questionnaire 1", templateId = "1"),
                QuestionnairePhysicianTemplate(name = "Sample Questionnaire 2", templateId = "2"),
                QuestionnairePhysicianTemplate(name = "Sample Questionnaire 3", templateId = "3"),
                QuestionnairePhysicianTemplate(name = "Sample Questionnaire 4", templateId = "4"),
                QuestionnairePhysicianTemplate(name = "Sample Questionnaire 5", templateId = "5"),
                QuestionnairePhysicianTemplate(name = "Sample Questionnaire 6", templateId = "6"),
            )
        )
    }
}