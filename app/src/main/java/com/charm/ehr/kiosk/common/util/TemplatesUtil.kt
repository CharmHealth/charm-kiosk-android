package com.charm.ehr.kiosk.common.util


import com.charm.ehr.kiosk.domain.OptionData
import com.charm.ehr.kiosk.domain.SOAPTemplateTableCell
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlin.collections.iterator

object TemplatesUtil {

    /*
NOTE: This method returns NULL if the string is not hierarchical (i.e, doesn't begin with ">>").
It returns the option string (by removing the leading ">>") otherwise.
For uniformity use this method alone to get hierarchicalOptionString
*/

    fun getHierarchicalOptionString(option: String): String? {
        if (option.startsWith(ModelUtil.swHierarchySymbol()))
            return option.substring(2).trim()
        return null
    }

    /*
     NOTE: IMP: Use this method everywhere to get the options from resp string.
     Don't do it yourself. Because this method trims and gives the option string.
     Even if procedure to get options changes, it'll need to be changed in only one place. There'll be uniformity.
     */

    fun getOptionsArrayFromString(optionsString: String): ArrayList<String> {
        var optStr = optionsString
        val optionsArrList = ArrayList<String>()

//To remove the trailing separator symbol(^^)
        val len = optionsString.length

        if (len >= 2 && optStr.substring(len - 2) == ModelUtil.swOptionsSeparatorSymbol()) {
            optStr = optStr.substring(0, len - 2)
        }

        val optionsTempArrList = optStr.split(ModelUtil.swOptionsSeparatorSymbol())
        for (str: String in optionsTempArrList) {
//TODO:
            optionsArrList.add(str.trim())
        }
        return optionsArrList

    }

    fun getNextHierarchyElementsForOption(
        tag: Int,
        hierarchyKeys: ArrayList<String>?,
        optionAL: ArrayList<String>
    ): ArrayList<String> {
        var nextLevelOptions = optionAL
        if (!hierarchyKeys.isNullOrEmpty()) {
            for (key: String in hierarchyKeys) {
                nextLevelOptions = getNextHierarchicalElementsWithSelectedOption(
                    key.split("\n").first().toInt(),
                    nextLevelOptions
                )
            }
        }
        nextLevelOptions = getNextHierarchicalElementsWithSelectedOption(tag, nextLevelOptions)
        return nextLevelOptions
    }

    private fun getNextHierarchicalElementsWithSelectedOption(
        tag: Int,
        optionsAL: ArrayList<String>
    ): ArrayList<String> {
        val optionNumber = tag % ModelUtil.swTagConst()
        var i = -1
        val nextLevelOptions = ArrayList<String>()
        for (str: String in optionsAL) {
            val opt = getHierarchicalOptionString(str)
            if (opt == null) {
                i++
            } else if (i == optionNumber) {
                nextLevelOptions.add(opt)
            } else if (i > optionNumber) {
                break
            }
        }
        return nextLevelOptions
    }


    fun insertObjectWithKeyAndOptionTagInDict(
        key: String,
        tag: Int,
        ansObjLM: LinkedHashMap<String, Any>?
    ) {
        val emptyHashMap = LinkedHashMap<String, LinkedHashMap<String, Any>>()
        if (ansObjLM?.isNotEmpty() == true) {
            var check = false
            var i = 0
            for (entry in ansObjLM) {
                val str = entry.key.split("\n").first()
                if (str.toInt() > tag) {
                    check = true
                }
                i++
            }
            if (check)
                insertInLinkedHashMap(ansObjLM, i, key, emptyHashMap)
            if (!check) {
                ansObjLM[key] = emptyHashMap
            }
        } else {
            ansObjLM?.put(key, emptyHashMap)
        }
    }

    private fun insertInLinkedHashMap(
        input: LinkedHashMap<String, Any>,
        index: Int,
        key: String,
        value: Any
    ) {
        if (index >= 0 && index <= input.count()) {
            val output = LinkedHashMap<String, Any>()
            var i = 0
            if (index == 0) {
                output[key] = value
                output.putAll(input)
            } else {
                for (entry in input) {
                    if (i == index) {
                        output[key] = value
                    }
                    output[entry.key] = entry.value
                    i++
                }
            }
            if (index == input.count()) {
                output[key] = value
            }
            input.clear()
            input.putAll(output)
            output.clear()
        }
    }


    //region Template String parsing methods


    fun getAnswerStringFromLinkedHashMap(
        ansObjLM: LinkedHashMap<String, *>?,
        prefix: String
    ): String {
        val separatorSymbol = ModelUtil.swOptionsSeparatorSymbol()
        var ansStr = ""

        if (ansObjLM?.isNotEmpty() == true) {
            for (entry in ansObjLM) {
                ansStr = ansStr + prefix + entry.key.split("\n").last()
                if (entry.value is LinkedHashMap<*, *>) {
                    val value = entry.value as LinkedHashMap<String, Any>
                    if (value.isNotEmpty()) {
                        ansStr = ansStr + separatorSymbol + getAnswerStringFromLinkedHashMap(
                            value,
                            prefix + ModelUtil.swHierarchySymbol()
                        )
                    }
                    ansStr += separatorSymbol
                }
            }
        }

        ansStr = ansStr.removeSuffix(separatorSymbol)
        return ansStr
    }


    fun getAnswerMapFromString(answerStr: String): LinkedHashMap<String, Any> {
        var optStr = answerStr

        //To remove the trailing separator symbol(^^)
        val len = answerStr.length

        if (len >= 2 && optStr.substring(len - 2) == ModelUtil.swOptionsSeparatorSymbol()) {
            optStr = optStr.substring(0, len - 2)
        }

        val optionsTempArrList = ArrayList(optStr.split(ModelUtil.swOptionsSeparatorSymbol()))
        return getAnswerMapFromArray(optionsTempArrList)
    }

    private fun getAnswerMapFromArray(answerArr: ArrayList<String>): LinkedHashMap<String, Any> {
        val answerHashMap = LinkedHashMap<String, Any>()
        var nextLevelArr = arrayListOf<String>()
        var currentStr = ""
        answerArr.forEach {
            var answer = it.trim()
            if (getHierarchicalOptionString(answer) == null) {
                if (nextLevelArr.isNotEmpty()) {
                    answerHashMap[currentStr] = getAnswerMapFromArray(nextLevelArr)
                }
                nextLevelArr = arrayListOf()
                currentStr = answer
                answerHashMap[currentStr] = ""
            } else {
                answer = getHierarchicalOptionString(answer) ?: ""
                nextLevelArr.add(answer)
            }
        }
        if (nextLevelArr.isNotEmpty()) {
            answerHashMap[currentStr] = getAnswerMapFromArray(nextLevelArr)
        }
        return answerHashMap
    }

    fun getOptionListFromJson(
        questionTypeObj: JsonObject,
        isMultiChoice: Boolean,
        answer: String,
        isOtherEnabled: Boolean
    ): ArrayList<OptionData> {
        val optionsAL =
            questionTypeObj.getAsJsonArray("options")?.map { it.asString }
                ?: listOf()
        val multipleAnswers =
            questionTypeObj.getAsJsonArray("multiple_answers")
                ?.map { it.asJsonObject.get("answer")?.asString ?: "" }
                ?: emptyList()
        val answerAL = LinkedHashMap<String, Any>()
        if (isMultiChoice) {
            multipleAnswers.forEach {
                if (it.isNotBlank()) {
                    answerAL[it] = ""
                }
            }

        } else {

            if (answer.isNotBlank()) {
                answerAL[answer] = ""
            }
        }
        var optionList =
            getOptionList(
                optionsAL as ArrayList<String>,
                answerAL,
                0
            )
        if (isOtherEnabled) {
            val optionOthers = OptionData("Others", isOther = true)
            val otherValue = if (isMultiChoice) {
                multipleAnswers.firstOrNull { answer ->
                    !optionsAL.map { it }
                        .contains(answer) && answer.isNotBlank()
                }
            } else {
                if (!optionsAL.contains(answer)) {
                    answer
                } else null
            }
            if (!otherValue.isNullOrEmpty()) {
                optionOthers.isChecked = true
                optionOthers.otherValue = otherValue
            }
            optionList.add(optionOthers)
        }
        return optionList
    }

    fun getOptionList(
        optionsAL: ArrayList<String>,
        answerAL: LinkedHashMap<String, Any>,
        viewLevel: Int
    ): ArrayList<OptionData> {
        val optionArr: ArrayList<OptionData> = arrayListOf()
        var i = 0
        while (i < optionsAL.size) {
            val option = optionsAL[i]
            if (getHierarchicalOptionString(option) == null) {
                val optionData = OptionData()
                optionData.optionString = option
                optionData.viewLevel = viewLevel
                i++
                val childOptionList = ArrayList<String>()
                while (i < optionsAL.size && getHierarchicalOptionString(optionsAL[i]) != null) {
                    childOptionList.add(getHierarchicalOptionString(optionsAL[i]) ?: "")
                    i++
                }
                if (option in answerAL) {
                    optionData.isChecked = true
                }
                if (childOptionList.isNotEmpty()) {
                    if (optionData.isChecked && answerAL[option] is LinkedHashMap<*, *>) {
                        val childrenAnswer = answerAL[option] as LinkedHashMap<String, Any>
                        optionData.childrenOptionList =
                            getOptionList(childOptionList, childrenAnswer, viewLevel + 1)
                    } else {
                        optionData.childrenOptionList =
                            getOptionList(childOptionList, LinkedHashMap(), viewLevel + 1)
                    }
                }
                if (option in answerAL) {
                    answerAL.remove(option)
                }
                optionArr.add(optionData)
            }
        }
        return optionArr
    }

    fun getValueFromMultiSelect(optionList: List<OptionData>?): List<String> {
        var answerList = mutableListOf<String>()
        optionList?.forEach {
            if (it.isChecked) {
                answerList.add(
                    if (it.isOther && !it.otherValue.isNullOrBlank()) {
                        it.otherValue
                    } else {
                        it.optionString
                    } ?: ""
                )
            }
        }
        return answerList
    }

    fun getValueFromSingleSelect(optionList: List<OptionData>?): String {
        var answer = ""
        optionList?.forEach {
            if (it.isChecked) {
                answer = if (it.isOther && !it.otherValue.isNullOrBlank()) {
                    it.otherValue
                } else {
                    it.optionString
                } ?: ""
            }
        }
        return answer
    }

    fun getValueFromNestedCheckList(optionList: List<OptionData>?): String {
        var answer = ""
        optionList?.forEach {
            if (it.isChecked) {
                if (it.isOther && !it.otherValue.isNullOrBlank()) {
                    answer += it.otherValue
                } else {
                    var childLevel = it.viewLevel
                    var ansSubStr = ""
                    while (childLevel > 0) {
                        ansSubStr += ModelUtil.swHierarchySymbol()
                        childLevel--
                    }
                    answer += ansSubStr + it.optionString + ModelUtil.swOptionsSeparatorSymbol()
                    if (!it.childrenOptionList.isNullOrEmpty()) {
                        val childList = arrayListOf<OptionData>()
                        childList.addAll(it.childrenOptionList ?: emptyList())
                        answer += getValueFromNestedCheckList(childList)
                    }
                }
            }
        }
        return answer
    }

    fun getValueFromTable(tableData: ArrayList<ArrayList<SOAPTemplateTableCell>>): String {
        val tableDataArr = JsonArray()
        tableData.forEach { rowData ->
            val rowArr = JsonArray()
            rowData.forEach { celldata ->
                val cellJson = JsonObject()
                cellJson.addProperty("value", celldata.value)
                cellJson.addProperty("colspan", celldata.colspan)
                cellJson.addProperty("type", celldata.type)
                rowArr.add(cellJson)
            }
            tableDataArr.add(rowArr)
        }
        return tableDataArr.toString()
    }

    fun unSelectChildren(childrenOptionList: List<OptionData>?) {
        childrenOptionList?.forEach {
            it.isChecked = false
            if (!it.childrenOptionList.isNullOrEmpty()) {
                unSelectChildren(it.childrenOptionList)
            }
        }
    }

    // Helper function to recursively uncheck children
    fun uncheckChildren(children: List<OptionData>?): List<OptionData>? {
        return children?.map { child ->
            child.copy(
                isChecked = false,
                childrenOptionList = uncheckChildren(child.childrenOptionList)
            )
        }
    }


    fun updateOptionInTree(
        options: List<OptionData>,
        updated: OptionData
    ): List<OptionData> {
        return options.map { option ->
            if (option == updated) {
                updated
            } else {
                val children = option.childrenOptionList
                if (!children.isNullOrEmpty()) {
                    option.copy(
                        childrenOptionList = ArrayList(updateOptionInTree(children, updated))
                    )
                } else {
                    option
                }
            }
        }
    }


//endregion


}