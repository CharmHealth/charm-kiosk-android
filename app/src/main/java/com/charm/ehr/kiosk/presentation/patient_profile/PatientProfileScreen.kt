package com.charm.ehr.kiosk.presentation.patient_profile


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.PatientProfileFlow
import com.charm.ehr.kiosk.common.util.UIUtil
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.domain.PatientQuesMap
import com.charm.ehr.kiosk.domain.QuestionnairePhysicianTemplate
import com.charm.ehr.kiosk.presentation.composable.ChipFlowLayoutWithButton
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdCircularProgressBar
import com.charm.ehr.kiosk.presentation.composable.MdCommonPrimaryButton
import com.charm.ehr.kiosk.presentation.composable.MdHorizontalSpace
import com.charm.ehr.kiosk.presentation.composable.MdTopToolBar
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun PatientProfileScreen(
    state: PatientProfileState,
    imageLoader: ImageLoader,
    popBackStack: () -> Unit,
    selectedPatient: Patient?,
    patientSharedQuestionnaires: List<QuestionnairePhysicianTemplate>?,
    onAction: (PatientProfileAction) -> Unit,
    workFlowType: PatientProfileFlow
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.displayCutout
            )
    ) {

        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MdColors.MdSurfaceColor)
                    .align(Alignment.TopStart)
            ) {

                MdTopToolBar(
                    headingText = if (workFlowType == PatientProfileFlow.AFTER_HANDOVER) stringResource(
                        R.string.profile
                    ) else stringResource(R.string.patient_profile), onBackClick = {
                        popBackStack()
                    }, workFlowType != PatientProfileFlow.AFTER_HANDOVER
                )

                if (state.isLoading) {

                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        MdCircularProgressBar()
                    }
                } else if (selectedPatient != null) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()) //TODO: Change later
                    ) {

                        PatientInfoCard(
                            patient = selectedPatient,
                            imageLoader = imageLoader,
                            state = state,
                            showEdit = (workFlowType != PatientProfileFlow.BEFORE_HANDOVER && state.allowEditPatientDetails),
                            onClickEdit = {
                                onAction(
                                    PatientProfileAction.onEditPatient(
                                        selectedPatient.practicePatientId ?: "", null
                                    )
                                )
                            })
                        if (state.isQuestionnaireRequired || state.isConsentFormRequired) {
                            if (workFlowType == PatientProfileFlow.BEFORE_HANDOVER || patientSharedQuestionnaires?.isNotEmpty() == true) {
                                QuestionnaireAndConsentCard(
                                    state = state,
                                    onClickAddQuestionnaire = {
                                        onAction(PatientProfileAction.onAddQuestionnaire)
                                    },
                                    onClickAddConsentForm = {
                                        onAction(PatientProfileAction.onAddQuestionnaire)
                                    },
                                    onClickFill = {
                                        onAction(PatientProfileAction.onClickFill)
                                    },
                                    workFlowType = workFlowType,
                                    patientSharedQuestionnaires = patientSharedQuestionnaires
                                        ?: emptyList()
                                )
                            }
                        }

                        if (workFlowType == PatientProfileFlow.AFTER_HANDOVER || workFlowType == PatientProfileFlow.SELF_CHECKIN) {
                            AddMissingPatientInfoSection(
                                state = state,
                                onClickAdd = {
                                    onAction(
                                        PatientProfileAction.onEditPatient(
                                            selectedPatient.practicePatientId ?: "", it
                                        )
                                    )
                                }
                            )
                        }

                        PatientDetailSection(
                            patient = selectedPatient,
                            showEdit = (workFlowType != PatientProfileFlow.BEFORE_HANDOVER),
                            state = state,
                            onClickEdit = {
                                onAction(
                                    PatientProfileAction.onEditPatient(
                                        selectedPatient.practicePatientId ?: "", it
                                    )
                                )
                            },
                            onAddMissingSection = {
                                onAction(PatientProfileAction.onAddMissingSection(it))
                            },
                            onRemoveMissingSection = {
                                onAction(PatientProfileAction.onRemoveMissingSection(it))
                            }
                        )
                    }
                }

            }
        }

        if (workFlowType == PatientProfileFlow.BEFORE_HANDOVER) {
            BottomBar(modifier = Modifier, onClick = {
                onAction(PatientProfileAction.onHandOver)
            }, buttonText = stringResource(R.string.hand_over))
        } else {
            BottomBar(modifier = Modifier, onClick = {
                onAction(PatientProfileAction.onFinishClick)
            }, buttonText = stringResource(R.string.finishStr))
        }
    }

}


@Composable
fun BottomBar(modifier: Modifier, onClick: () -> Unit, buttonText: String) {
    MdCardWithoutTonalElevation(
        modifier = modifier
            .fillMaxWidth(),
        elevation = 16.dp, shape = RectangleShape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
            horizontalArrangement = Arrangement.End // Align button to the end (right)
        ) {
            MdCommonPrimaryButton(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .widthIn(min = 140.dp),
                text = buttonText,
                onClick = {
                    onClick()
                }
            )
        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionnaireAndConsentCard(
    state: PatientProfileState,
    onClickAddQuestionnaire: () -> Unit,
    onClickAddConsentForm: () -> Unit,
    onClickFill: () -> Unit,
    workFlowType: PatientProfileFlow,
    patientSharedQuestionnaires: List<QuestionnairePhysicianTemplate>,
) {

    val context = LocalContext.current

    val questionnaireCount =
        remember(patientSharedQuestionnaires) {
            patientSharedQuestionnaires.filter {
                it.patientQuesMap?.isSubmitted == false
            }.size
        }
    val consentFormCount =
        remember(state.patientSharedConsentForms) { state.patientSharedConsentForms.size }
    val questionnaireDisplayString = remember(questionnaireCount, consentFormCount) {
        UIUtil.getQuestionDisplayString(questionnaireCount, consentFormCount, context)
    }

    val hasFormsToFill = (questionnaireCount > 0 || consentFormCount > 0)

    val showBadgeQuestionnaire =
        (workFlowType == PatientProfileFlow.AFTER_HANDOVER || workFlowType == PatientProfileFlow.SELF_CHECKIN) && patientSharedQuestionnaires.isNotEmpty()

    val topPadding = if (showBadgeQuestionnaire) {
        18.dp
    } else {
        0.dp
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding, bottom = 0.dp, start = 0.dp, end = 0.dp)
    ) {

        MdCardWithoutTonalElevation(
            modifier = Modifier
                .padding(
                    vertical = MdTheme.dimension.halfCardPadding,
                    horizontal = MdTheme.dimension.contentCardPadding
                )
                .fillMaxWidth(), elevation = 0.dp
        ) {
            if (workFlowType == PatientProfileFlow.AFTER_HANDOVER || workFlowType == PatientProfileFlow.SELF_CHECKIN) {
                Column(
                    modifier = Modifier.padding(
                        start = MdTheme.dimension.contentCardPadding,
                        end = MdTheme.dimension.contentCardPadding
                    )
                ) {
                    if (patientSharedQuestionnaires.isNotEmpty()) {
                        if (hasFormsToFill) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 8.dp, vertical = 16.dp
                                ), verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = questionnaireDisplayString,
                                    style = MdTheme.typography.medium16TextStyle
                                )

                                MdHorizontalSpace(1f)

                                MdCardWithoutTonalElevation(
                                    modifier = Modifier,
                                    colors = MdColors.MattYellow,
                                    shape = RoundedCornerShape(50.dp),
                                    elevation = 0.dp
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .background(MdColors.MattYellow)
                                    ) {
                                        Text(
                                            modifier = Modifier.padding(horizontal = 4.dp),
                                            text = (questionnaireCount + consentFormCount).toString(),
                                            style = MdTheme.typography.medium14TextStyle
                                        )
                                    }
                                }

                                MdHorizontalSpace(
                                    numberOfSpaces = 1f, modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )

                                MdCommonPrimaryButton(
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .height(44.dp),
                                    text = stringResource(R.string.fill),
                                    onClick = {
                                        onClickFill()
                                    },
                                    endIcon = {
                                        Icon(
                                            modifier = Modifier.rotate(270f),
                                            painter = painterResource(R.drawable.ic_arrow_down),
                                            contentDescription = null
                                        )
                                    },
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.padding(
                                     vertical = 16.dp
                                ), verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = stringResource(R.string.questionnaire_completed_msg),
                                    style = MdTheme.typography.medium16TextStyle
                                )

                                MdHorizontalSpace(
                                    numberOfSpaces = 1f, modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )

                                TextButton(
                                    modifier = Modifier,
                                    onClick = {
                                        onClickFill()
                                    }) {
                                    Text(
                                        text = stringResource(R.string.view),
                                        style = MdTheme.typography.semiBold16TextStyle.copy(
                                            MdColors.BrandPink
                                        )
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        modifier = Modifier.rotate(270f),
                                        painter = painterResource(R.drawable.ic_arrow_down),
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.padding(
                        start = MdTheme.dimension.contentCardPadding,
                    )
                ) {
                    ChipFlowLayoutWithButton(
                        selectedItems = patientSharedQuestionnaires,
                        labelString = stringResource(R.string.questionnaire),
                        labelDetailString = stringResource(R.string.add_questionnaires),
                        displayString = { it.name ?: "" },
                        onRemoveItem = { item ->
                        },
                        onClickAdd = {
                            onClickAddQuestionnaire()
                        },
                    )

                    if (state.isConsentFormRequired) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 2.dp),
                            color = MdColors.Black10
                        )

                        ChipFlowLayoutWithButton(
                            selectedItems = state.patientSharedConsentForms,
                            labelString = stringResource(R.string.consent_form),
                            labelDetailString = stringResource(R.string.search_consent_form),
                            displayString = { it.fileName ?: "" },
                            onRemoveItem = { item -> },//TODO:Remove
                            onClickAdd = {
                                onClickAddConsentForm()
                            }
                        )
                    }
                }

            }
        }

        if (showBadgeQuestionnaire) {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .wrapContentWidth()
                    .align(Alignment.TopStart)
                    .offset(y = (-8).dp, x = 40.dp) // half above, half inside
            ) {

                MdCardWithoutTonalElevation(
                    modifier = Modifier,
                    colors = if (hasFormsToFill) MdColors.MattYellow else MdColors.GreenMatt,
                    shape = RoundedCornerShape(50.dp),
                    elevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .background(if (hasFormsToFill) MdColors.MattYellow else MdColors.GreenMatt)
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = if (hasFormsToFill) stringResource(R.string.pending) else stringResource(
                                R.string.completed
                            ),
                            style = MdTheme.typography.medium14TextStyle
                        )
                    }
                }

            }
        }
    }

}

@Preview(showBackground = true, widthDp = 600, heightDp = 1000)
@Composable
fun PatientProfileScreenBeforeHandoverPreview() {
    MdTheme {
        val context = LocalContext.current
        val imageLoader = remember { ImageLoader(context) }

        val sampleState = PatientProfileState(
            isLoading = false,
            errorMessage = null,
            isQuestionnaireRequired = true
        )

        PatientProfileScreen(
            state = sampleState,
            imageLoader = imageLoader,
            popBackStack = {},
            selectedPatient = dummyTestPatient,
            patientSharedQuestionnaires = listOf(
                QuestionnairePhysicianTemplate(
                    templateId = "q1",
                    name = "General Health and Wellness Assessment"
                ),
                QuestionnairePhysicianTemplate(
                    templateId = "q2",
                    name = "Comprehensive Pain Assessment and Management Form for Chronic Conditions"
                ),
                QuestionnairePhysicianTemplate(
                    templateId = "q3",
                    name = "Detailed Mental Health and Emotional Wellbeing Survey for New Patients"
                ),
                QuestionnairePhysicianTemplate(
                    templateId = "q4",
                    name = "Lifestyle and Behavioral Health Risk Evaluation Questionnaire for Adults"
                ),
                QuestionnairePhysicianTemplate(
                    templateId = "q5",
                    name = "Family Medical History and Genetic Risk Factors Assessment Form"
                ),
                QuestionnairePhysicianTemplate(
                    templateId = "q6",
                    name = "Pre-Operative Physical and Psychological Readiness Screening Questionnaire",
                    patientQuesMap = PatientQuesMap(patientQuesMapId = "pq_q6", isSubmitted = false)
                ),
            ),
            onAction = {},
            workFlowType = PatientProfileFlow.BEFORE_HANDOVER,
        )
    }
}

@Preview(showBackground = true, widthDp = 600, heightDp = 1000)
@Composable
fun PatientProfileScreenAfterHandoverPreview() {
    MdTheme {
        val context = LocalContext.current
        val imageLoader = remember { ImageLoader(context) }

        val sampleState = PatientProfileState(
            patientSharedConsentForms = arrayListOf(),
            isLoading = false, errorMessage = null,
            isQuestionnaireRequired = true,
            allowEditPatientDetails = true
        )

        PatientProfileScreen(
            state = sampleState,
            imageLoader = imageLoader,
            popBackStack = {},
            selectedPatient = dummyTestPatient,
            patientSharedQuestionnaires = listOf(
                QuestionnairePhysicianTemplate(
                    templateId = "q1",
                    name = "General Health and Wellness Assessment"
                ),
                QuestionnairePhysicianTemplate(
                    templateId = "q2",
                    name = "Comprehensive Pain Assessment and Management Form for Chronic Conditions"
                ),
                QuestionnairePhysicianTemplate(
                    templateId = "q3",
                    name = "Detailed Mental Health and Emotional Wellbeing Survey for New Patients"
                ),
                QuestionnairePhysicianTemplate(
                    templateId = "q4",
                    name = "Lifestyle and Behavioral Health Risk Evaluation Questionnaire for Adults"
                ),
                QuestionnairePhysicianTemplate(
                    templateId = "q5",
                    name = "Family Medical History and Genetic Risk Factors Assessment Form"
                ),
                QuestionnairePhysicianTemplate(
                    templateId = "q6",
                    name = "Pre-Operative Physical and Psychological Readiness Screening Questionnaire",
                    patientQuesMap = PatientQuesMap(patientQuesMapId = "pq_q6", isSubmitted = false)
                ),
            ),
            {},
            PatientProfileFlow.AFTER_HANDOVER,
        )
    }
}

@Preview(showBackground = true, widthDp = 600, heightDp = 1000)
@Composable
fun PatientProfileScreenSelfCheckInPreview() {
    MdTheme {
        val context = LocalContext.current
        val imageLoader = remember { ImageLoader(context) }

        val sampleState = PatientProfileState(
            isLoading = false,
            errorMessage = null,
            isQuestionnaireRequired = true
        )

        PatientProfileScreen(
            state = sampleState,
            imageLoader = imageLoader,
            popBackStack = {},
            selectedPatient = dummyTestPatient,
            patientSharedQuestionnaires = listOf(
                QuestionnairePhysicianTemplate(
                    templateId = "q1",
                    name = "General Health and Wellness Assessment"
                ),
                QuestionnairePhysicianTemplate(
                    templateId = "q2",
                    name = "Comprehensive Pain Assessment and Management Form for Chronic Conditions"
                ),
                QuestionnairePhysicianTemplate(
                    templateId = "q3",
                    name = "Detailed Mental Health and Emotional Wellbeing Survey for New Patients"
                ),
                QuestionnairePhysicianTemplate(
                    templateId = "q4",
                    name = "Lifestyle and Behavioral Health Risk Evaluation Questionnaire for Adults"
                ),
                QuestionnairePhysicianTemplate(
                    templateId = "q5",
                    name = "Family Medical History and Genetic Risk Factors Assessment Form"
                ),
                QuestionnairePhysicianTemplate(
                    templateId = "q6",
                    name = "Pre-Operative Physical and Psychological Readiness Screening Questionnaire"
                ),
            ),
            {},
            PatientProfileFlow.SELF_CHECKIN,
        )
    }
}








