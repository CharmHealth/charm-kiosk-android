package com.charm.ehr.kiosk.common.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.charm.ehr.kiosk.domain.Member
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.theme.MdColors
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Months
import org.joda.time.Years
import org.joda.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object UIUtil {

    val TAG = "UIUtil"
    fun Context.findActivity(): Activity {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        throw IllegalStateException("Permissions should be called in the context of an Activity")
    }

    fun showToast(context: Context?, source: String?, duration: Int) {

        try {
            if (context == null || source == null) return

            val toast = Toast.makeText(context, source, duration)
            toast.setGravity(Gravity.BOTTOM, 0, 50)
            toast.show()
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }

    fun showToast(context: Context?, @StringRes sourceId: Int, duration: Int) {

        try {
            if (context == null) return

            val toast = Toast.makeText(context, sourceId, duration)
            toast.setGravity(Gravity.BOTTOM, 0, 50)
            toast.show()
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }

    fun formMemberName(member: Member?): String {
        var memberName = ""
        if (member != null) {
            val prefix = member.prefix?.trim()
            val fullName = member.fullName?.trim() ?: ""

            memberName = if (prefix != null && prefix.isNotBlank() && fullName?.contains(
                    "Dr.",
                    false
                ) != true
            )
                "$prefix. $fullName"
            else fullName
        }


        return memberName
    }

    fun formInitials(name: String): String {
        // Remove everything after "."
        val cleanName = name.substringAfter(".").trim()

        // Split the remaining name into words
        val nameArr = cleanName.split(" ").filter { it.isNotBlank() }

        return buildString {
            if (nameArr.isNotEmpty()) {
                append(nameArr[0].first().uppercaseChar())  // First letter of first name
                if (nameArr.size > 1) {
                    append(
                        nameArr[1].first().uppercaseChar()
                    )  // First letter of second name (if available)
                }
            }
        }
    }

    fun dpToPx(context: Context, dp: Dp): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.value,
            context.resources.displayMetrics
        ).toInt()
    }

    fun addMandatoryStar(
        notes: String,
        color: Color,
        isMandatory: Boolean,
        fontSize: TextUnit,
        fontWeight: FontWeight,
        fontFamily: FontFamily,
        fontColor: Color
    ): AnnotatedString {
        return if (isMandatory) {
            buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = fontColor,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        fontFamily = fontFamily
                    )
                ) {
                    append(notes)
                }
                withStyle(style = SpanStyle(color = color, fontSize = 18.sp)) {
                    append(" *")
                }
            }
        } else {
            buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = fontColor,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        fontFamily = fontFamily
                    )
                ) {
                    append(notes)
                }

            }
        }
    }



    fun getQuestionDisplayString(
        questionnaireCount: Int,
        consentFormCount: Int,
        context: Context
    ): String {

        val questionnaireLabel = when {
            questionnaireCount > 1 -> context.getString(R.string.questionnaires) //with s
            questionnaireCount == 1 -> context.getString(R.string.questionnaire)
            else -> ""
        }

        val consentFormLabel = when {
            consentFormCount > 1 -> context.getString(R.string.consent_forms) //with s
            consentFormCount == 1 -> context.getString(R.string.consent_form)
            else -> ""
        }

        val parts = listOf(questionnaireLabel, consentFormLabel).filter { it.isNotEmpty() }

        return if (parts.isNotEmpty()) {
            "You have ${parts.joinToString(" and ")} to fill out"
        } else {
            ""
        }
    }

    fun getLabelColor(isError: Boolean, isFocused: Boolean, textStyle: TextStyle, enabled: Boolean): Color {
        val labelColor = when {
            !enabled-> MdColors.disabledLabelColor
            isError -> MdColors.Red
            isFocused -> MdColors.BrandPink
            else -> textStyle.color
        }
        return labelColor
    }

    fun getAgeFromDob(dob: DateTime?): String {
        if (dob == null) return ""
        val now = DateTime.now()

        val dobStart = dob.withTimeAtStartOfDay()
        val nowStart = now.withTimeAtStartOfDay()

        // Guard: a DOB in the future is a data-entry error — return safe default
        if (dobStart.isAfter(nowStart)) return "0 days"

        val years = Years.yearsBetween(dobStart, nowStart).years

        return when {
            years >= 1 -> "$years year${if (years > 1) "s" else ""}"
            else -> {
                val months = Months.monthsBetween(dobStart, nowStart).months
                if (months >= 1) {
                    "$months month${if (months > 1) "s" else ""}"
                } else {
                    val days = Days.daysBetween(dobStart, nowStart).days
                    "$days day${if (days != 1) "s" else ""}"
                }
            }
        }
    }


    fun formatMillisToDateTime(millisString: String,dateTimeFormatter: DateTimeFormatter): String {
        return try {
            val millis = millisString.toLong()
            val dateTime = DateTime(millis)
            dateTimeFormatter.print(dateTime)
        } catch (e: Exception) {
            ""
        }
    }

    fun getTimeAgo(millis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - millis

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$minutes minute${if (minutes > 1) "s" else ""} ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours hr${if (hours > 1) "s" else ""} ago"
            }
            else -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "$days day${if (days > 1) "s" else ""} ago"
            }
        }
    }


}