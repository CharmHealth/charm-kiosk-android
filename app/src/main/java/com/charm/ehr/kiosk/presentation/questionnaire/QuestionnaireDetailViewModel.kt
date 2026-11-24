package com.charm.ehr.kiosk.presentation.questionnaire

import UiText
import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charm.ehr.kiosk.domain.ConsentForm
import com.charm.ehr.kiosk.domain.OptionData
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.domain.Question
import com.charm.ehr.kiosk.domain.QuestionnaireMetaData
import com.charm.ehr.kiosk.domain.QuestionnairePhysicianTemplate
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.domain.SOAPEntry
import com.charm.ehr.kiosk.domain.SOAPTemplateTableCell
import com.charm.ehr.kiosk.common.util.DateUtil
import com.charm.ehr.kiosk.domain.repository.FileRepository
import com.charm.ehr.kiosk.domain.repository.PatientDetailRepository
import com.charm.ehr.kiosk.domain.repository.QuestionnaireRepository
import com.charm.ehr.kiosk.presentation.composable.APIState
import com.charm.ehr.kiosk.common.util.ModelUtil
import com.charm.ehr.kiosk.common.util.TemplatesUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionnaireDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val questionnaireRepository: QuestionnaireRepository,
    private val patientDetailRepository: PatientDetailRepository,
    private val fileRepository: FileRepository,
) : ViewModel() {

    private val _questionnaireState =
        MutableStateFlow(QuestionnaireDetailScreenState())
    val questionnaireState: StateFlow<QuestionnaireDetailScreenState> = _questionnaireState

    private val _templateListState: MutableStateFlow<TemplateListState> =
        MutableStateFlow(TemplateListState())
    val templateListState: StateFlow<TemplateListState> = _templateListState

    // Navigation event flow for Compose navigation
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents = _navigationEvents
    var isInitialLoadFailed: Boolean = false

    val patientSharedQuestionnaires: StateFlow<List<QuestionnairePhysicianTemplate>?> =
        patientDetailRepository.patientSharedQuestionnaires

    val selectedPatient: StateFlow<Patient?> = patientDetailRepository.patient

    sealed class NavigationEvent {
        object Back : NavigationEvent()
        object Finish : NavigationEvent()
    }

    //region Template list

    fun updateConsentFormList(patientSharedConsentForms: List<ConsentForm>) {
        _templateListState.update {
            it.copy(consentFormList = patientSharedConsentForms)
        }
    }

    //endregion

    fun onChangeAction(action: QuestionnaireDetailAction) {
        when (action) {
            is QuestionnaireDetailAction.ClearToastMessage -> {
                clearToastMessage()
            }

            is QuestionnaireDetailAction.AnswerChanged -> {
                answerChanged(
                    action.changedQuestion,
                    action.answer,
                    action.soapEntry,
                    action.childQuestion
                )
            }

            is QuestionnaireDetailAction.PostQuestionnaire -> {
                postQuestionnaire(action.isFromNext)
            }

            is QuestionnaireDetailAction.RetryLastFailedAction -> {
                retryLastFailedAction()
            }

            is QuestionnaireDetailAction.UpdateFocusEntryId -> {
                updateFocusEntryId(
                    action.entryId,
                    action.parentIndex,
                    action.childFocusIndex,
                    action.isFromSection
                )
            }

            is QuestionnaireDetailAction.FetchSignature -> {
                fetchSignature(action.signatureUrl, action.entryId)
            }

            is QuestionnaireDetailAction.ClearError -> {
                clearError()
            }

            is QuestionnaireDetailAction.OnBackPress -> {

            }
        }
    }

    // Use TemplateListState.questionnaireList for navigation
    private var currentQuestionnaireIndex: Int = 0
        set(value) {
            field = value
            val list = patientSharedQuestionnaires.value
            setSelectedQuestionnaireId(
                list?.getOrNull(field)?.patientQuesMap?.patientQuesMapId,
                field
            )
        }

    fun setQuestionnaireNavigation(selectedId: String) {
        val list = patientSharedQuestionnaires.value
        currentQuestionnaireIndex =
            list?.indexOfFirst { it.patientQuesMap?.patientQuesMapId == selectedId } ?: 0
        _questionnaireState.update {
            it.copy(
                isSingleQuestionnaire = isSingleQuestionnaire(),
                isLastQuestionnaire = isLastQuestionnaire(),
                isFirstQuestionnaire = isFirstQuestionnaire(),
                isQuestionnaireValueUpdated = false
            )
        }
    }

    fun nextQuestionnaire() {
        val list = patientSharedQuestionnaires.value
        if (currentQuestionnaireIndex < (list?.size?.minus(1) ?: 0)) {
            currentQuestionnaireIndex++
            _questionnaireState.update {
                it.copy(
                    isSingleQuestionnaire = isSingleQuestionnaire(),
                    isLastQuestionnaire = isLastQuestionnaire(),
                    isFirstQuestionnaire = isFirstQuestionnaire(),
                    isQuestionnaireValueUpdated = false
                )
            }
        }
    }

    fun previousQuestionnaire() {
        if (currentQuestionnaireIndex > 0) {
            currentQuestionnaireIndex--
            _questionnaireState.update {
                it.copy(
                    isSingleQuestionnaire = isSingleQuestionnaire(),
                    isLastQuestionnaire = isLastQuestionnaire(),
                    isFirstQuestionnaire = isFirstQuestionnaire(),
                    isQuestionnaireValueUpdated = false
                )
            }
        }
    }

    fun isFirstQuestionnaire(): Boolean = currentQuestionnaireIndex == 0
    fun isLastQuestionnaire(): Boolean =
        currentQuestionnaireIndex == patientSharedQuestionnaires.value?.size?.minus(1)

    fun isSingleQuestionnaire(): Boolean = patientSharedQuestionnaires.value?.size == 1

    private fun setSelectedQuestionnaireId(id: String?, selectedQuestionnaireIndex: Int) {
        _questionnaireState.update {
            it.copy(
                selectedQuestionnaireId = id,
                selectedQuestionnaireIndex = selectedQuestionnaireIndex
            )
        }
        loadQuestionnaire(id ?: "")
    }

    private var lastFailedAction: (() -> Unit)? = null

    fun clearError() {
        _questionnaireState.update { it.copy(errorMessage = null) }
        lastFailedAction = null
    }

    fun clearToastMessage() {
        _questionnaireState.update { it.copy(toastMessage = null) }
    }

    fun loadQuestionnaire(quesMapId: String) {
        isInitialLoadFailed = false
        viewModelScope.launch(Dispatchers.IO) {
            val result = questionnaireRepository.getQuestionnaireAnswer(quesMapId)
            when (result) {
                is APIState.Success -> {
                    _questionnaireState.update {
                        it.copy(
                            questionnaireMetaData = result.data,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }

                is APIState.Error -> {
                    isInitialLoadFailed = true
                    lastFailedAction = { loadQuestionnaire(quesMapId) }
                    _questionnaireState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.apiResponseError.errorMsg
                        )
                    }
                }

                is APIState.Loading -> {
                    _questionnaireState.update {
                        it.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }

    fun answerChanged(
        changedQuestion: Question,
        answer: String,
        soapEntry: SOAPEntry?,
        childQuestion: Question? = null
    ) {
        _questionnaireState.update {
            it.copy(
                questionnaireMetaData = it.questionnaireMetaData?.copy(
                    questions = it.questionnaireMetaData.questions.map { question ->
                        if (question.entryId == changedQuestion.entryId) {
                            updateQuestionAnswer(
                                question,
                                changedQuestion,
                                answer,
                                soapEntry,
                                childQuestion
                            )
                        } else {
                            question
                        }
                    }
                ),
                isQuestionnaireValueUpdated = true
            )
        }
    }

    fun updateQuestionAnswer(
        question: Question,
        changedQuestion: Question,
        answer: String,
        soapEntry: SOAPEntry?,
        childQuestion: Question?
    ): Question {
        return when (question) {
            is Question.SimpleQuestion -> {
                question.copy(answer = answer)
            }

            is Question.DateQuestion -> {
                question.copy(answer = answer)
            }

            is Question.QuestionWithOptions -> {
                if (question.isMultiChoice) {
                    val newOptionList =
                        (changedQuestion as? Question.QuestionWithOptions)?.options

                    question.copy(
                        options = newOptionList ?: emptyList()
                    )
                } else {
                    val updateValue =
                        (changedQuestion as? Question.QuestionWithOptions)?.updatedValue
                    val updatedOption =
                        (changedQuestion as? Question.QuestionWithOptions)?.updatedOption

                    val updatedOtherValue =
                        (changedQuestion as? Question.QuestionWithOptions)?.updatedOtherValue

                    val newOptionList =
                        updateSingleSelectComponent(
                            null,
                            updateValue,
                            updatedOption,
                            question.options,
                            updatedOtherValue
                        )

                    question.copy(
                        options = newOptionList
                    )
                }
            }

            is Question.YesOrNoQuestion -> {
                val updateValue =
                    (changedQuestion as? Question.YesOrNoQuestion)?.updatedValue
                val updatedOption =
                    (changedQuestion as? Question.YesOrNoQuestion)?.updatedOption

                val newOptionList =
                    updateSingleSelectComponent(
                        null,
                        updateValue,
                        updatedOption,
                        question.options,
                        null
                    )

                question.copy(
                    options = newOptionList
                )
            }

            is Question.RatingScaleQuestion -> {

                question.copy(answer = answer)
            }

            is Question.SignatureQuestion -> {
                question.copy(
                    signatureFilePath = answer, isSignatureChanged = true
                )
            }

            is Question.SOAPWidgetQuestion -> {
                updateSOAPQuestion(question, soapEntry, answer)
            }

            is Question.Section -> {
                // If the changed question is a child of a section, update the section's questions
                if (childQuestion != null) {
                    question.copy(
                        questions = question.questions.map { subQuestion ->
                            if (subQuestion.entryId == childQuestion.entryId) {
                                updateQuestionAnswer(
                                    subQuestion,
                                    childQuestion,
                                    answer,
                                    soapEntry,
                                    null
                                )
                            } else subQuestion
                        }
                    )
                } else question // No change for sections without child questions
            }

            else -> question // No change for other types
        }
    }

    fun updateSOAPQuestion(
        question: Question.SOAPWidgetQuestion,
        soapEntry: SOAPEntry?,
        answer: String
    ): Question.SOAPWidgetQuestion {
        return question.copy(
            soapWidget = question.soapWidget.copy(
                entries = question.soapWidget.entries.map { entry ->
                    if (entry.soapEntryId == soapEntry?.soapEntryId) {
                        when (entry) {
                            is SOAPEntry.SimpleQuestionEntry -> {
                                entry.copy(answer = answer)
                            }

                            is SOAPEntry.TextBoxEntry -> {
                                entry.copy(answer = answer)
                            }

                            is SOAPEntry.HeadingEntry -> {
                                entry.copy(answer = answer)
                            }

                            is SOAPEntry.YesOrNoEntry -> {
                                val updateValue =
                                    (soapEntry as? SOAPEntry.YesOrNoEntry)?.updatedValue
                                val updatedOption =
                                    (soapEntry as? SOAPEntry.YesOrNoEntry)?.updatedOption
                                val updatedOtherValue =
                                    (soapEntry as? SOAPEntry.YesOrNoEntry)?.updatedOtherValue


                                val newOptionList =
                                    updateSingleSelectComponent(
                                        null,
                                        updateValue,
                                        updatedOption,
                                        entry.optionList,
                                        updatedOtherValue
                                    )

                                entry.copy(
                                    optionList = newOptionList
                                )
                            }

                            is SOAPEntry.RatingScaleEntry -> {
                                entry.copy(answer = answer)
                            }

                            is SOAPEntry.TableEntry -> {
                                // Update the table cell using copy()
                                val rowIndex =
                                    (soapEntry as? SOAPEntry.TableEntry)?.rowIndex
                                        ?: -1
                                val colIndex =
                                    (soapEntry as? SOAPEntry.TableEntry)?.colIndex
                                        ?: -1
                                if (rowIndex >= 0 && colIndex >= 0) {
                                    val oldTable =
                                        entry.soapWidgetTableData
                                    val newTable =
                                        oldTable.mapIndexed { r, row ->
                                            if (r == rowIndex) {
                                                row.mapIndexed { c, cell ->
                                                    if (c == colIndex) cell.copy(
                                                        value = answer
                                                    ) else cell
                                                } as ArrayList<SOAPTemplateTableCell>
                                            } else row
                                        } as ArrayList<ArrayList<SOAPTemplateTableCell>>
                                    entry.copy(soapWidgetTableData = newTable)
                                } else entry
                            }

                            is SOAPEntry.RadioEntry -> {

                                val updatedCommentValue =
                                    (soapEntry as? SOAPEntry.RadioEntry)?.updatedCommentValue
                                if (updatedCommentValue != null) {
                                    return@map entry.copy(
                                        additionalComment = updatedCommentValue
                                    )
                                }
                                val updatedChildrenList =
                                    (soapEntry as? SOAPEntry.RadioEntry)?.updatedChildrenList
                                val updateValue =
                                    (soapEntry as? SOAPEntry.RadioEntry)?.updatedValue
                                val updatedOption =
                                    (soapEntry as? SOAPEntry.RadioEntry)?.updatedOption
                                val updatedOtherValue =
                                    (soapEntry as? SOAPEntry.RadioEntry)?.updatedOtherValue

                                val newOptionList =
                                    updateSingleSelectComponent(
                                        updatedChildrenList,
                                        updateValue,
                                        updatedOption,
                                        entry.optionList,
                                        updatedOtherValue
                                    )

                                entry.copy(
                                    optionList = newOptionList
                                )
                            }

                            is SOAPEntry.CheckboxEntry -> {
                                val updatedCommentValue =
                                    (soapEntry as? SOAPEntry.CheckboxEntry)?.updatedCommentValue
                                if (updatedCommentValue != null) {
                                    return@map entry.copy(
                                        additionalComment = updatedCommentValue
                                    )
                                }
                                val updatedOption =
                                    (soapEntry as? SOAPEntry.CheckboxEntry)?.optionList

                                entry.copy(
                                    optionList = updatedOption
                                        ?: emptyList()
                                )
                            }

                            else -> entry
                        }
                    } else entry
                }
            )
        )
    }

    fun updateSingleSelectComponent(
        updatedChildrenList: List<OptionData>?,
        updateValue: Boolean?,
        updatedOption: OptionData?,
        optionList: List<OptionData>,
        updatedOtherValue: String?
    ): List<OptionData> {
        return if (updatedOtherValue != null) {
            optionList.map { option ->
                if (option == updatedOption && option.isOther) {
                    option.copy(
                        otherValue = updatedOtherValue
                    )
                } else {
                    option
                }
            }
        } else if (updatedChildrenList != null) {
            optionList.map { option ->
                if (option == updatedOption) {
                    option.copy(
                        childrenOptionList = updatedChildrenList
                    )
                } else {
                    option
                }
            }
        } else {
            optionList.map { option ->
                if (updateValue == true) {
                    if (option == updatedOption) {
                        option.copy(
                            isChecked = true
                        )
                    } else if (option.isChecked) {
                        val childrenList =
                            TemplatesUtil.uncheckChildren(
                                option.childrenOptionList
                            )
                        option.copy(
                            isChecked = false,
                            childrenOptionList = childrenList
                        )
                    } else option
                } else option
            }
        }
    }

    fun checkSOAPTypeEmptyAnswer(
        entry: SOAPEntry,
        flatIndex: Int,
        isFromSection: Boolean
    ): Boolean {
        when (entry) {
            is SOAPEntry.SimpleQuestionEntry -> {
                if (entry.answer.isNullOrBlank() && entry.isMandatory) {
                    updateFocusEntryId(entry.soapEntryId, flatIndex, -1, isFromSection)
                    return false
                }
            }

            is SOAPEntry.TableEntry -> {
                entry.answer =
                    TemplatesUtil.getValueFromTable(entry.soapWidgetTableData)
            }

            is SOAPEntry.CheckboxEntry -> {
                if (entry.isMandatory) {
                    val isAnyOptionChecked = checkSelectionEmptyAnswer(entry.optionList)
                    if (!isAnyOptionChecked) {
                        updateFocusEntryId(entry.soapEntryId, flatIndex, -1, isFromSection)
                        return false
                    }
                }
                entry.answer =
                    TemplatesUtil.getValueFromNestedCheckList(entry.optionList)
                if (entry.isAdditionalComments && !entry.additionalComment.isNullOrBlank()) {
                    entry.answer += ModelUtil.appendStringCommentsValue + entry.additionalComment
                }
            }

            is SOAPEntry.RadioEntry -> {
                if (entry.isMandatory) {
                    val isAnyOptionChecked = checkSelectionEmptyAnswer(entry.optionList)
                    if (!isAnyOptionChecked) {
                        updateFocusEntryId(entry.soapEntryId, flatIndex, -1, isFromSection)
                        return false
                    }
                }
                entry.answer =
                    TemplatesUtil.getValueFromNestedCheckList(entry.optionList)
                if (entry.isAdditionalComments && !entry.additionalComment.isNullOrBlank()) {
                    entry.answer += ModelUtil.appendStringCommentsValue + entry.additionalComment
                }
            }

            is SOAPEntry.YesOrNoEntry -> {
                if (entry.isMandatory) {
                    val isAnyOptionChecked =
                        checkSelectionEmptyAnswer(entry.optionList, isYesOrNoQuestion = true)
                    if (!isAnyOptionChecked) {
                        updateFocusEntryId(entry.soapEntryId, flatIndex, -1, isFromSection)
                        return false
                    }
                }
                entry.optionList.forEach {
                    if (it.isChecked) {
                        val answer = it.optionString
                        if (it.isOther && !it.otherValue.isNullOrEmpty()) {
                            if (it.optionString.equals("Yes")) {
                                entry.answer =
                                    answer + ModelUtil.swOptionsSeparatorSymbol() + ModelUtil.appendStringYesComments + it.otherValue
                            } else {
                                entry.answer =
                                    answer + ModelUtil.swOptionsSeparatorSymbol() + ModelUtil.appendStringNoComments + it.otherValue
                            }
                        } else {
                            entry.answer = answer
                        }
                    }
                }

            }

            is SOAPEntry.RatingScaleEntry -> {
                if (entry.answer.isNullOrBlank() && entry.isMandatory) {
                    updateFocusEntryId(entry.soapEntryId, flatIndex, -1, isFromSection)
                    return false
                }
            }

            is SOAPEntry.HeadingEntry -> {}
            is SOAPEntry.ImageEntry -> {}
            is SOAPEntry.TextBoxEntry -> {}
        }
        return true
    }

    fun checkSelectionEmptyAnswer(
        optionList: List<OptionData>,
        isYesOrNoQuestion: Boolean = false
    ): Boolean {
        return optionList.any {
            if (it.isChecked) {
                if (!isYesOrNoQuestion && it.isOther) {
                    !it.otherValue.isNullOrEmpty()
                } else {
                    true
                }
            } else {
                false
            }
        }
    }

    fun checkEmptyAnswer(
        question: Question,
        parentIndex: Int,
        isFromSection: Boolean
    ): Boolean {
        if ((question is Question.SimpleQuestion || question is Question.RatingScaleQuestion) && question.isMandatory && (question.answer.isNullOrBlank())) {
            updateFocusEntryId(question.entryId, parentIndex, -1, isFromSection)
            return false
        } else if (question is Question.DateQuestion && (question.isMandatory || question.defaultValue != null) && (question.answer.isNullOrBlank())) {
            updateFocusEntryId(question.entryId, parentIndex, -1, isFromSection)
            return false
        } else if (question is Question.DateQuestion && !question.answer.isNullOrBlank()) {
            val formatter = ModelUtil.mothFirstDateFormatWithSlash()
            val date = DateUtil.getDate(question.answer, formatter)
            if (date == null) {
                updateFocusEntryId(question.entryId, parentIndex, -1, isFromSection)
                return false
            }
        } else if ((question is Question.QuestionWithOptions)) {
            if (question.isMandatory) {
                val isAnyOptionChecked = question.options.any {
                    if (it.isChecked) {
                        if (it.isOther) {
                            !it.otherValue.isNullOrEmpty()
                        } else {
                            true
                        }
                    } else {
                        false
                    }
                }
                if (!isAnyOptionChecked) {
                    updateFocusEntryId(question.entryId, parentIndex, -1, isFromSection)
                    return false
                }
            }
            if (question.isMultiChoice) {
                question.multipleAnswer =
                    TemplatesUtil.getValueFromMultiSelect(question.options)
            } else {
                question.answer = TemplatesUtil.getValueFromSingleSelect(question.options)
            }
        } else if (question is Question.SignatureQuestion) {
            if (question.signatureFilePath.isNullOrBlank() && question.isMandatory && !question.isSignatureChanged) {
                updateFocusEntryId(question.entryId, parentIndex, -1, isFromSection)
                return false
            }
        } else if (question is Question.YesOrNoQuestion) {
            question.answer = TemplatesUtil.getValueFromSingleSelect(question.options)
            if (question.isMandatory) {
                if (question.answer.isNullOrBlank()) {
                    updateFocusEntryId(question.entryId, parentIndex, -1, isFromSection)
                    return false
                }
            }
        } else if (question is Question.Section) {
            question.questions.forEachIndexed { index, subQuestion ->
                if (!checkEmptyAnswer(subQuestion, parentIndex, true)) {
                    updateFocusEntryId(question.entryId, parentIndex, index, false)
                    return false
                }
            }
        } else if (question is Question.SOAPWidgetQuestion) {
            question.soapWidget.entries.forEach { entry ->
                val result = checkSOAPTypeEmptyAnswer(entry, 0, isFromSection)
                if (!result) {
                    return false
                }
            }
        }
        return true
    }

    fun updateFocusEntryId(
        entryId: String?,
        parentIndex: Int,
        childFocusIndex: Int,
        isFromSection: Boolean
    ) {
        if (!isFromSection) {
            _questionnaireState.update {
                it.copy(
                    focusEntryId = entryId,
                    focusIndex = parentIndex,
                    childFocusIndex = childFocusIndex
                )
            }
        }
    }

    fun postQuestionnaire(isFromNext: Boolean) {
        val questions = questionnaireState.value.questionnaireMetaData?.questions.orEmpty()
        // Flatten questions for validation: for SOAPWidgetQuestion, add each entry as a separate item
        val flatQuestions = questions.flatMap { q ->
            if (q is Question.SOAPWidgetQuestion) {
                q.soapWidget.entries.map { entry -> Pair(q, entry) }
            } else {
                listOf(Pair(q, null))
            }
        }
        var flatIndex = 0
        for ((question, soapEntry) in flatQuestions) {
            val isValid = if (soapEntry == null) {
                checkEmptyAnswer(question, flatIndex, false)
            } else {
                checkSOAPTypeEmptyAnswer(soapEntry, flatIndex, false)
            }
            if (!isValid) {
                _questionnaireState.update {
                    it.copy(
                        focusIndex = flatIndex,
                        toastMessage = R.string.fill_mandatory_filed,
                    )
                }
                return
            }
            flatIndex++
        }
        if (questionnaireState.value.questionnaireMetaData?.isSubmitted == false) {
            questionnaireState.value.questionnaireMetaData?.isSubmitted = true
        }
        // If validation passes, post the questionnaire
        val metaData = questionnaireState.value.questionnaireMetaData
        if (questionnaireState.value.isQuestionnaireValueUpdated) {
            viewModelScope.launch(Dispatchers.IO) {
                _questionnaireState.update { it.copy(isLoading = true, errorMessage = null) }
                if (metaData != null) {
                    val signatureResult = postAllSignatures(metaData.questions)
                    if (!signatureResult) return@launch
                    postQuestionnaireAnswers(metaData, isFromNext)
                }
            }
        } else {
            onQuestionnairePostSuccess(metaData?.quesMapId ?: "", isFromNext)
        }
    }

    private suspend fun postAllSignatures(questions: List<Question>): Boolean {
        val signatureQuestions = getAllSignatureQuestions(questions)
        for (signatureQuestion in signatureQuestions) {
            val filePath = signatureQuestion.signatureFilePath
            if (!filePath.isNullOrBlank() && signatureQuestion.isSignatureChanged) {
                val result = questionnaireRepository.postSignForQuestionnaire(
                    filePath,
                    questionnaireState.value.selectedQuestionnaireId ?: "",
                    signatureQuestion.entryId ?: ""
                )
                if (result !is APIState.Success) {
                    lastFailedAction = { postQuestionnaire(true) }
                    _questionnaireState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UiText.StrResId(R.string.request_failed_try_again)
                        )
                    }
                    return false
                }
            }
        }
        return true
    }

    private suspend fun postQuestionnaireAnswers(
        metaData: QuestionnaireMetaData,
        isFromNext: Boolean
    ) {
        val result = questionnaireRepository.postQuestionnaireAnswers(
            metaData,
            selectedPatient.value?.practicePatientId ?: ""
        )
        when (result) {
            is APIState.Success -> {
                _questionnaireState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        isSuccess = true,
                        focusEntryId = null,
                        focusIndex = 0,
                        childFocusIndex = 0
                    )
                }
                onQuestionnairePostSuccess(metaData.quesMapId ?: "", isFromNext)

            }

            is APIState.Error -> {
                lastFailedAction = { postQuestionnaire(isFromNext) }
                _questionnaireState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.apiResponseError.errorMsg
                    )
                }
            }

            is APIState.Loading -> {
                _questionnaireState.update {
                    it.copy(isLoading = true, errorMessage = null)
                }
            }
        }
    }

    fun onQuestionnairePostSuccess(
        quesMapId: String,
        isFromNext: Boolean
    ) {
        val isSingle = isSingleQuestionnaire()
        val isLast = isLastQuestionnaire()
        if (isFromNext) {
            patientDetailRepository.submitQuestionnaire(quesMapId)
            if (!isSingle && !isLast) {
                nextQuestionnaire()
            } else {
                if (isSingle) {
                    viewModelScope.launch {
                        _navigationEvents.emit(NavigationEvent.Finish)
                    }
                } else {
                    if (patientSharedQuestionnaires.value?.all { it.patientQuesMap?.isSubmitted == true } == true) {
                        viewModelScope.launch {
                            _navigationEvents.emit(NavigationEvent.Finish)
                        }
                    } else {
                        viewModelScope.launch {
                            _navigationEvents.emit(NavigationEvent.Back)
                        }
                    }
                }
            }

        } else {
            previousQuestionnaire()
        }
    }

    // Helper to collect all signature questions, including inside sections
    private fun getAllSignatureQuestions(questions: List<Question>): List<Question.SignatureQuestion> {
        val result = mutableListOf<Question.SignatureQuestion>()
        for (q in questions) {
            if (q is Question.SignatureQuestion) {
                result.add(q)
            } else if (q is Question.Section) {
                result.addAll(getAllSignatureQuestions(q.questions))
            }
        }
        return result
    }

    //region Signature Bitmap

    fun fetchSignature(signatureUrl: String, entryId: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _questionnaireState.update { state ->
                state.copy(
                    questionnaireMetaData = state.questionnaireMetaData?.copy(
                        questions = state.questionnaireMetaData.questions.map { q ->
                            updateSignatureLoadingState(
                                q,
                                entryId,
                                isLoading = true,
                                isError = false
                            )
                        }
                    ))
            }
            val result = questionnaireRepository.getSignForQuestionnaire(signatureUrl)
            if (result is APIState.Success) {
                val file =
                    fileRepository.getResponseBodyToFile(
                        result.data,
                        Bitmap.CompressFormat.PNG
                    )
                val filePath = file?.absolutePath
                _questionnaireState.update { state ->
                    state.copy(
                        questionnaireMetaData = state.questionnaireMetaData?.copy(
                            questions = state.questionnaireMetaData.questions.map { q ->
                                updateSignatureLoadingState(
                                    q,
                                    entryId,
                                    isLoading = false,
                                    isError = false,
                                    filePath = filePath
                                )
                            }
                        ))
                }
            } else {
                lastFailedAction = { fetchSignature(signatureUrl, entryId) }
                _questionnaireState.update { state ->
                    state.copy(
                        questionnaireMetaData = state.questionnaireMetaData?.copy(
                            questions = state.questionnaireMetaData.questions.map { q ->
                                updateSignatureLoadingState(
                                    q,
                                    entryId,
                                    isLoading = false,
                                    isError = true,
                                    filePath = null
                                )
                            }
                        ),
                        errorMessage = UiText.StrResId(R.string.request_failed_try_again)
                    )
                }
            }
        }
    }

    // Helper to recursively update signature state inside sections
    private fun updateSignatureLoadingState(
        question: Question,
        entryId: String?,
        isLoading: Boolean,
        isError: Boolean,
        filePath: String? = null
    ): Question {
        return when (question) {
            is Question.SignatureQuestion -> {
                if (question.entryId == entryId) {
                    question.copy(
                        isLoading = isLoading,
                        isError = isError,
                        signatureFilePath = filePath ?: question.signatureFilePath
                    )
                } else question
            }

            is Question.Section -> {
                question.copy(
                    questions = question.questions.map { q ->
                        updateSignatureLoadingState(q, entryId, isLoading, isError, filePath)
                    }
                )
            }

            else -> question
        }
    }

    fun retryLastFailedAction() {
        val action = lastFailedAction
        clearError()
        action?.invoke()
    }

//endregion
}
