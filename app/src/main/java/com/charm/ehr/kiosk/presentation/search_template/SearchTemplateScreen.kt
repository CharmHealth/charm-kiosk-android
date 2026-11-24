package com.charm.ehr.kiosk.presentation.search_template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.util.ModelUtil
import com.charm.ehr.kiosk.domain.ConsentForm
import com.charm.ehr.kiosk.domain.QuestionnairePhysicianTemplate
import com.charm.ehr.kiosk.presentation.composable.BottomBarWithPrimarySecondaryButton
import com.charm.ehr.kiosk.presentation.composable.ChipList
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdTopToolBar
import com.charm.ehr.kiosk.presentation.composable.SearchBarWithDropDown
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

enum class SearchTemplateType(val type: String) {
    Questionnaire("Questionnaire"), ConsentForm("ConsentForm")
}

@Composable
fun SearchTemplateScreen(
    state: SearchTemplateState,
    action: (SearchTemplateAction) -> Unit,
    selectedTemplates: List<Any>,
    displayName: (Any) -> String,
    onItemRemoved: (Any) -> Unit,
    onPopBackstack: () -> Unit
) {

    val templateTypeList = mutableListOf<SearchTemplateType>()
    templateTypeList.add(SearchTemplateType.Questionnaire)
    if (!ModelUtil.hideConsentForm) {
        templateTypeList.add(SearchTemplateType.ConsentForm)
    }
    Box {
        Column(
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.systemBars.union(WindowInsets.displayCutout)
            )
        ) {
            //Change heading after enabling consent form
            MdTopToolBar(
                headingText = stringResource(R.string.select_questionnaire),
                onBackClick = { action(SearchTemplateAction.OnBackPress) }
            )

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                MdCardWithoutTonalElevation(
                    modifier = Modifier
                        .padding(
                            start = MdTheme.dimension.cardPadding,
                            end = MdTheme.dimension.cardPadding,
                            bottom = MdTheme.dimension.cardPadding
                        )
                        .fillMaxHeight()
                ) {
                    Column(modifier = Modifier.padding(vertical = MdTheme.dimension.cardPadding)) {

                        val chipScrollState = rememberScrollState()
                        LaunchedEffect(selectedTemplates.size) {
                            if (selectedTemplates.isNotEmpty()) {
                                chipScrollState.animateScrollTo(chipScrollState.maxValue)
                            }
                        }
                        if (selectedTemplates.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .padding(bottom = MdTheme.dimension.cardPadding)
                                    .fillMaxWidth()
                                    .heightIn(max = 120.dp)
                                    .verticalScroll(chipScrollState)
                            ) {
                                ChipList(
                                    modifier = Modifier
                                        .padding(horizontal = 14.dp),
                                    items = selectedTemplates,
                                    displayString = {
                                        displayName(it)
                                    },
                                    onRemoveItem = {
                                        onItemRemoved(it)
                                    },
                                    textStyle = MdTheme.typography.medium14TextStyle,
                                    horizontal = Arrangement.spacedBy(16.dp),
                                    vertical = Arrangement.spacedBy(8.dp),
                                    hideCloseIcon = false,
                                    chipColor = MdColors.BrandLightPinkVariant
                                )
                            }

                        }
                        SearchBarWithDropDown(
                            options = templateTypeList,
                            getLabelFromOptions = { it.type },
                            onOptionSelected = {
                                action(
                                    SearchTemplateAction.OnChangeTemplateType(
                                        formType = it,
                                        query = state.searchQuery
                                    )
                                )
                            },
                            onSearch = { query ->
                                action(SearchTemplateAction.OnSearchQueryChange(query))
                            },
                            isLoading = state.isLoading,
                            searchResult = state.searchResults,
                            pojoToString = {
                                if (state.templateType == SearchTemplateType.Questionnaire) {
                                    val questionnaireTemplate = it as QuestionnairePhysicianTemplate
                                    questionnaireTemplate.name ?: ""
                                } else if (state.templateType == SearchTemplateType.ConsentForm) {
                                    val consentForm = it as ConsentForm
                                    consentForm.fileName ?: "" //Todo: check
                                } else {
                                    ""
                                }
                            },
                            onItemSelected = { item ->
                                if (state.templateType == SearchTemplateType.Questionnaire) {
                                    action(SearchTemplateAction.OnSelectQuestionnaire(item as QuestionnairePhysicianTemplate))
                                } else if (state.templateType == SearchTemplateType.ConsentForm) {
                                    action(SearchTemplateAction.OnSelectConsentTemplate(item as ConsentForm))
                                }

                            },
                            onItemRemoved = { item ->
                                if (state.templateType == SearchTemplateType.Questionnaire) {
                                    action(SearchTemplateAction.OnRemoveQuestionnaire(item as QuestionnairePhysicianTemplate))
                                } else if (state.templateType == SearchTemplateType.ConsentForm) {
                                    action(SearchTemplateAction.OnRemoveConsentTemplate(item as ConsentForm))
                                }
                            },
                            isSelected = {
                                if (state.templateType == SearchTemplateType.Questionnaire) {
                                    state.selectedQuestionnaireSet.any { selected -> (it as QuestionnairePhysicianTemplate).templateId == selected.templateId }
                                } else if (state.templateType == SearchTemplateType.ConsentForm) {
                                    state.selectedConsentFormSet.any { selected -> (it as ConsentForm).fileId == selected.fileId }
                                } else {
                                    false
                                }
                            },
                            selectedItems = if (state.templateType == SearchTemplateType.Questionnaire) {
                                state.selectedQuestionnaireSet
                            } else {
                                state.selectedConsentFormSet
                            },
                            uniqueValueForKey = {
                                if (state.templateType == SearchTemplateType.Questionnaire) {
                                    (it as QuestionnairePhysicianTemplate).templateId ?: ""
                                } else if (state.templateType == SearchTemplateType.ConsentForm) {
                                    (it as ConsentForm).fileId ?: ""
                                } else {
                                    ""
                                }
                            }, isEnabled = {
                                if (state.templateType == SearchTemplateType.Questionnaire) {
                                    val result = state.selectedQuestionnaireSet.any { selected ->
                                        (it as QuestionnairePhysicianTemplate).templateId == selected.templateId && selected.patientQuesMap?.isFilled == true
                                    }
                                    !result // isEnabled is false only if isFilled is true
                                } else {
                                    true
                                }
                            }
                        )
                    }
                }
            }
        }
        BottomBarWithPrimarySecondaryButton(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            onPrimaryClick = {
                action(SearchTemplateAction.OnSubmit)
            },
            primaryButtonText = stringResource(R.string.doneStr),
        )
    }
}

@Preview(showBackground = true, device = "spec:parent=medium_tablet,orientation=portrait")
@Composable
fun SearchTemplateScreenPreview() {
    val sampleQuestionnaireList = listOf(
        QuestionnairePhysicianTemplate(templateId = "q1", name = "General Health Questionnaire"),
        QuestionnairePhysicianTemplate(templateId = "q2", name = "Pain Assessment Form"),
        QuestionnairePhysicianTemplate(templateId = "q3", name = "Mental Health Survey"),
        QuestionnairePhysicianTemplate(templateId = "q4", name = "Mental Health Survey"),
        QuestionnairePhysicianTemplate(templateId = "q5", name = "Mental Health Survey"),
        QuestionnairePhysicianTemplate(templateId = "q6", name = "Mental Health Survey"),
        QuestionnairePhysicianTemplate(templateId = "q7", name = "Mental Health Survey"),
        QuestionnairePhysicianTemplate(templateId = "q8", name = "Mental Health Survey"),
        QuestionnairePhysicianTemplate(templateId = "q9", name = "Mental Health Survey"),
        QuestionnairePhysicianTemplate(templateId = "q10", name = "Mental Health Survey"),
        QuestionnairePhysicianTemplate(templateId = "q11", name = "Mental Health Survey"),
        QuestionnairePhysicianTemplate(templateId = "q12", name = "Mental Health Survey"),
        QuestionnairePhysicianTemplate(templateId = "q13", name = "Mental Health Survey"),
    )
    val sampleConsentFormList = listOf(
        ConsentForm(fileId = "c1", fileName = "Consent for Treatment"),
        ConsentForm(fileId = "c2", fileName = "HIPAA Authorization"),
        ConsentForm(fileId = "c3", fileName = "Research Participation Agreement")
    )

    val sampleState = SearchTemplateState(
        isLoading = false,
        searchQuery = "Health",
        templateType = SearchTemplateType.Questionnaire,
        searchResults = sampleQuestionnaireList,
        selectedQuestionnaireSet = mutableSetOf(sampleQuestionnaireList[0]),
        selectedConsentFormSet = mutableSetOf(),
        errorMessage = null
    )

    MdTheme {
        SearchTemplateScreen(
            state = sampleState,
            action = {},
            selectedTemplates = listOf(
                sampleQuestionnaireList[0].name ?: "",
                sampleQuestionnaireList[1].name ?: "",
                "The new fever quesitonnaire",
                "The new fever quesitonnaire",
                "The new fever quesitonnaire",
                "The new fever quesitonnaire",
                "The new fever quesitonnaire",
                "The new fever quesitonnaire",
                "The new fever quesitonnaire",
                "The new fever quesitonnaire",
                "The new fever quesitonnaire",

            ),
            displayName = { it.toString() },
            onItemRemoved = {},
            onPopBackstack = {}
        )
    }
}