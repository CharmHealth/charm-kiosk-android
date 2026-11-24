package com.charm.ehr.kiosk.presentation.search_template

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.charm.ehr.kiosk.domain.ConsentForm
import com.charm.ehr.kiosk.domain.QuestionnairePhysicianTemplate
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.composable.MdAlertDialog
import com.charm.ehr.kiosk.presentation.composable.MdProgressDialog
import com.charm.ehr.kiosk.presentation.patient_profile.PatientDetailViewmodel

@Composable
fun SearchTemplateScreenRoot(
    patientDetailViewmodel: PatientDetailViewmodel,
    navController: NavController,
    onPopBackstack: () -> Unit
) {

    val state by patientDetailViewmodel.searchTemplateState.collectAsStateWithLifecycle()
    val showDiscardDialog by patientDetailViewmodel.showDiscardDialog.collectAsStateWithLifecycle()

    if (state.isSuccess) {
        onPopBackstack()
        patientDetailViewmodel.onSearchFormAction(SearchTemplateAction.OnSuccess)
    }

    if (state.isLoading) {
        MdProgressDialog()
    }

    BackHandler(enabled = true) {
        if (state.isChangesMade) {
            patientDetailViewmodel.onSearchFormAction(SearchTemplateAction.showDiscardDialog)
        } else {
            onPopBackstack()
        }
    }

    if (showDiscardDialog) {
        MdAlertDialog(
            message = stringResource(R.string.discard_changes_msg),
            primaryButtonResId = R.string.discard,
            secondaryButtonResId = R.string.cancel,
            onPrimaryAction = {
                patientDetailViewmodel.onSearchFormAction(SearchTemplateAction.DismissDiscardDialog)
                onPopBackstack()
            },
            onSecondaryAction = {
                patientDetailViewmodel.onSearchFormAction(SearchTemplateAction.DismissDiscardDialog)
            }
        )
    }

    SearchTemplateScreen(
        state = state,
        action = {
            if (it is SearchTemplateAction.OnBackPress) {
                if (state.isChangesMade) {
                    patientDetailViewmodel.onSearchFormAction(SearchTemplateAction.showDiscardDialog)
                } else {
                    onPopBackstack()
                }
            }
            patientDetailViewmodel.onSearchFormAction(it)
        },
        selectedTemplates = (state.selectedQuestionnaireSet.toList() + state.selectedConsentFormSet.toList()),
        displayName = {
            if (it is QuestionnairePhysicianTemplate) {
                it.name ?: ""
            } else if (it is ConsentForm) {
                it.fileName ?: ""
            } else {
                ""
            }
        },
        onItemRemoved = {
            if (it is QuestionnairePhysicianTemplate) {
                patientDetailViewmodel.onSearchFormAction(
                    SearchTemplateAction.OnRemoveQuestionnaire(
                        it
                    )
                )
            } else if (it is ConsentForm) {
                patientDetailViewmodel.onSearchFormAction(
                    SearchTemplateAction.OnRemoveConsentTemplate(
                        it
                    )
                )
            }
        },
        onPopBackstack = onPopBackstack,
    )

}
