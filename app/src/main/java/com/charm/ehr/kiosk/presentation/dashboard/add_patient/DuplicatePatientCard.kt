package com.charm.ehr.kiosk.presentation.dashboard.add_patient

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.util.MDAccountsRelatedUtil
import com.charm.ehr.kiosk.common.util.ModelUtil
import com.charm.ehr.kiosk.domain.Patient
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdHighlightedText
import com.charm.ehr.kiosk.presentation.composable.MdHorizontalSpace
import com.charm.ehr.kiosk.presentation.composable.MdVerticalSpace
import com.charm.ehr.kiosk.presentation.composable.PatientImageItem
import com.charm.ehr.kiosk.presentation.dashboard.search_patient.ResultDetailIcon
import com.charm.ehr.kiosk.presentation.patient_profile.dummyTestPatient
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun DuplicatePatientDialogue(
    currentPatient: Patient,
    existingPatient: Patient,
    imageLoader: ImageLoader,
    onSelectPatient: (Patient) -> Unit,
    addAsDuplicate: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true,
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        ),
        onDismissRequest = {
            onDismissRequest()
        },
    ) {
        DuplicatePatientCard(
            modifier = Modifier.padding(16.dp),
            currentPatient = currentPatient,
            existingPatient = existingPatient,
            imageLoader = imageLoader,
            onSelectPatient = onSelectPatient,
            addAsDuplicate = addAsDuplicate,
        )
    }
}

@Composable
fun DuplicatePatientCard(
    modifier: Modifier,
    currentPatient: Patient,
    existingPatient: Patient,
    imageLoader: ImageLoader,
    onSelectPatient: (Patient) -> Unit = {},
    addAsDuplicate: () -> Unit = {},
) {

    var isAllowDuplicate by remember { mutableStateOf(false) }

    val isPortrait = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

    MdCardWithoutTonalElevation(
        modifier = modifier.fillMaxWidth(if (isPortrait) 0.9f else 0.7f),
        elevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(32.dp)) {
            Text(stringResource(R.string.patient_record_already_exist), style = MdTheme.typography.semiBold20TextStyle)
            MdVerticalSpace(2f)
            MdInfoCard(
                text = stringResource(R.string.patient_with_same_detail_content),
                iconId = R.drawable.ic_info,
                textColor = MdColors.BlackText,
                backgroundColor = MdColors.PaleOrange,
            )
            MdVerticalSpace(2f)
            PatientDetailCardForDuplicate(
                imageSize = 60.dp,
                searchQuery = "",
                existingPatient = existingPatient,
                onClickPatient = onSelectPatient,
                imageLoader = imageLoader,
                currentPatient = currentPatient
            )
            MdVerticalSpace(3f)

            Row(
                modifier = Modifier
                    .padding(0.dp)
                    .clickable {
                        isAllowDuplicate = !isAllowDuplicate
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    modifier = Modifier.padding(0.dp),
                    checked = isAllowDuplicate,
                    onCheckedChange = {
                        isAllowDuplicate = it
                    }
                )

                Text(
                    stringResource(R.string.i_understand_and_want_to_create_a_duplicate_patient_record),
                    style = MdTheme.typography.regular16TextStyle
                )

                Spacer(modifier = Modifier.weight(1f))

                if (isAllowDuplicate) {
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MdColors.BrandPink, RoundedCornerShape(100.dp))
                            .clickable { addAsDuplicate() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.add_duplicate_patient),
                            modifier = Modifier
                                .wrapContentWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MdTheme.typography.regular16TextStyle,
                            color = MdColors.BrandPink
                        )
                    }
                }

            }

            MdVerticalSpace(2f)
        }
    }
}

@Composable
fun PatientDetailCardForDuplicate(
    searchQuery: String,
    currentPatient: Patient,
    existingPatient: Patient,
    imageSize: Dp,
    textStyle: TextStyle = MdTheme.typography.regular16TextStyle,
    detailTextStyle: TextStyle = MdTheme.typography.regular14TextStyle,
    detailTextColor: Color = MdColors.BlackText,
    onClickPatient: (Patient) -> Unit,
    imageLoader: ImageLoader,
) {

    val patientName = remember { existingPatient.fullName.orEmpty() }
    val patientRecordId = remember { existingPatient.patientRecordId.orEmpty() }
    val patientDOB = remember { existingPatient.dateOfBirth?.toString(ModelUtil.charmApiDateFormat()).orEmpty() }
    val patientPhoneNumber = remember { ModelUtil.getPhoneNumberFromPatient(existingPatient) }
    val emailId = remember { existingPatient.contactDetails?.emailId.orEmpty() }
    val pronoun = remember { existingPatient.pronouns ?: "" }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(),
            onClick = { onClickPatient(existingPatient) })
    ) {

        val photoPath = MDAccountsRelatedUtil.getPatientPhotoUrl(existingPatient.practicePatientId)
        PatientImageItem(
            hasImage = existingPatient.isPhotoAvailableOnline,
            imageUrl = photoPath,
            imageLoader = imageLoader,
            imgSize = imageSize,
            selectedPatient = existingPatient,
            textStyle = MdTheme.typography.medium20TextStyle.copy(letterSpacing = 1.sp)
        )

        MdHorizontalSpace(2f)


        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                MdHighlightedText(
                    text = patientName,
                    style = textStyle,
                    modifier = Modifier.padding(vertical = 4.dp),
                    searchQuery = searchQuery,
                    highlightColor = MdColors.PaleYellow
                )

                MdHorizontalSpace(1f)

                MdHighlightedText(
                    pronoun, style = MdTheme.typography.regular14TextStyle, searchQuery = searchQuery, highlightColor = MdColors.PaleYellow
                )

                MdHorizontalSpace(2f)

                //Color Dependents on active / inactive
                MdCardWithoutTonalElevation(
                    modifier = Modifier.wrapContentWidth(),
                    elevation = 0.dp, colors = MdColors.GreenMatt
                ) {
                    Text(
                        stringResource(R.string.active),
                        style = MdTheme.typography.regular14TextStyle,
                        color = MdColors.BlackText,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 1.dp)
                    )
                }

            }
            MdVerticalSpace(.5f)
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .wrapContentWidth()
                    .padding(vertical = 4.dp)
            ) {

                if (patientRecordId.isNotBlank()) {
                    ResultDetailIcon(R.drawable.ic_patient_id)
                    MdHighlightedText(
                        text = patientRecordId,
                        style = detailTextStyle.copy(color = detailTextColor),
                        searchQuery = searchQuery,
                        highlightColor = MdColors.PaleYellow
                    )
                    MdHorizontalSpace(2.5f)
                }

                if (patientDOB.isNotBlank()) {
                    ResultDetailIcon(R.drawable.ic_outline_cake_24)
                    MdHighlightedText(
                        text = patientDOB,
                        style = detailTextStyle.copy(color = detailTextColor),
                        searchQuery = searchQuery,
                        highlightColor = MdColors.PaleYellow
                    )
                    MdHorizontalSpace(2.5f)
                }

                if (patientPhoneNumber.isNotBlank()) {
                    ResultDetailIcon(R.drawable.ic_outline_phone_24)
                    MdHighlightedText(
                        text = patientPhoneNumber,
                        style = detailTextStyle.copy(color = detailTextColor),
                        searchQuery = searchQuery,
                        highlightColor = MdColors.PaleYellow,
                    )
                    MdHorizontalSpace(2.5f)
                }

                if (emailId.isNotBlank()) {
                    ResultDetailIcon(R.drawable.ic_outline_email_24)
                    MdHighlightedText(
                        text = emailId,
                        style = detailTextStyle.copy(color = detailTextColor),
                        searchQuery = searchQuery,
                        highlightColor = MdColors.PaleYellow,
                        middleEllipsis = true,
                    )
                    MdHorizontalSpace(2.5f)
                }

                MdHorizontalSpace(1f)

            }
        }


        Box(modifier = Modifier.padding(horizontal = 8.dp)) {

            Text(
                text = stringResource(R.string.view),
                modifier = Modifier.wrapContentWidth(),
                style = MdTheme.typography.regular16TextStyle,
                color = MdColors.BrandPink
            )

        }

    }
}

@Composable
fun MdInfoCard(
    text: String, @DrawableRes iconId: Int, backgroundColor: Color, textColor: Color
) {
    MdCardWithoutTonalElevation(
        modifier = Modifier.padding(),
        colors = backgroundColor,
        elevation = 0.dp
    ) {
        Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {

            MdHorizontalSpace(2f)
            Text(text = text, color = textColor)
            MdHorizontalSpace(2f)
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1200)
@Composable
fun DuplicatePatientCardPreview() {
    MdTheme {
        Box(modifier = Modifier.background(MdColors.MdSurfaceColor)) {

            DuplicatePatientCard(
                modifier = Modifier.padding(16.dp),
                currentPatient = dummyTestPatient,
                existingPatient = dummyTestPatient,
                imageLoader = ImageLoader.Builder(LocalContext.current).build(),
            )
        }
    }
}