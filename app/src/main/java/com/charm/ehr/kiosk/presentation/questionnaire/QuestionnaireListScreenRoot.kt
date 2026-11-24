package com.charm.ehr.kiosk.presentation.questionnaire

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TemplateListScreenRoot(
    questionnaireViewModel: QuestionnaireDetailViewModel,
    onBackPress: () -> Unit,
    onTemplateClick: () -> Unit,
) {

    val state by questionnaireViewModel.templateListState.collectAsStateWithLifecycle()
    val patient by questionnaireViewModel.selectedPatient.collectAsStateWithLifecycle()
    val questionnaireList by   questionnaireViewModel.patientSharedQuestionnaires.collectAsStateWithLifecycle()


    TemplateListScreen(
        state = state,
        onBackPress = onBackPress,
        onAction = {
            when (it) {
                is TemplateListAction.OnClickTemplate -> {
                    questionnaireViewModel.setQuestionnaireNavigation(it.questionnaireId)
                    onTemplateClick()
                }

                is TemplateListAction.OnClickConsentForm -> {

                }

                is TemplateListAction.onBackPressed -> {
                    onBackPress()
                }

                is TemplateListAction.onFinishClick -> {
                    onBackPress()
                }
            }
        },
        patient = patient,
        questionnaireList =questionnaireList

    )

}
