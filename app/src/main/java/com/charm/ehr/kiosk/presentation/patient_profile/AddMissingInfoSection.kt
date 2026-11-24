package com.charm.ehr.kiosk.presentation.patient_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.composable.MdVerticalSpace
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun AddMissingPatientInfoSection(
    state: PatientProfileState,
    onClickAdd: (Section) -> Unit
) {

    if (state.missingSections.isEmpty()) return

    Column() {

        Text(
            stringResource(R.string.complete_your_profile),
            style = MdTheme.typography.semiBold20TextStyle,
            modifier = Modifier.padding(MdTheme.dimension.contentCardPadding)
        )
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = MdTheme.dimension.contentCardPadding),
            horizontalArrangement = Arrangement.spacedBy(MdTheme.dimension.cardPadding)
        ) {

            if (state.missingSections.contains(Section.PERSONAL_INFO)) {
                AddMissingPatientInfoCard(
                    iconResId = R.drawable.ic_group,
                    label = stringResource(R.string.demographics)
                ) {
                    onClickAdd(Section.PERSONAL_INFO)
                }
            }


            if (state.missingSections.contains(Section.CONTACT_INFO)) {
                AddMissingPatientInfoCard(
                    iconResId = R.drawable.ic_contact,
                    label = stringResource(R.string.contact_details)
                ) {
                    onClickAdd(Section.CONTACT_INFO)
                }
            }

            if (state.missingSections.contains(Section.EMERGENCY_CONTACT)) {
                AddMissingPatientInfoCard(
                    iconResId = R.drawable.ic_phone,
                    label = stringResource(R.string.emergency_contact)
                ) {
                    onClickAdd(Section.CONTACT_INFO)
                }
            }


            if (state.missingSections.contains(Section.CAREGIVER_DETAILS)) {
                AddMissingPatientInfoCard(
                    iconResId = R.drawable.ic_care,
                    label = stringResource(R.string.caregiver)
                ) {
                    onClickAdd(Section.CAREGIVER_DETAILS)
                }
            }

            if (state.isInsuranceRequired) {
                if (state.missingSections.contains(Section.PRIMARY_INSURANCE)) {
                    AddMissingPatientInfoCard(
                        iconResId = R.drawable.ic_insurance,
                        label = stringResource(R.string.primary_insurance)
                    ) {
                        onClickAdd(Section.PRIMARY_INSURANCE)
                    }
                }

                if (state.missingSections.contains(Section.SECONDARY_INSURANCE)) {
                    AddMissingPatientInfoCard(
                        iconResId = R.drawable.ic_insurance,
                        label = stringResource(R.string.secondary_insurance)
                    ) {
                        onClickAdd(Section.SECONDARY_INSURANCE)
                    }
                }
            }


        }
        MdVerticalSpace(2f)
    }

}

@Preview(showBackground = true, widthDp = 600, heightDp = 1000)
@Composable
fun AddMissingPatientInfoSectionPreview() {
    MdTheme {
        Box(
            modifier = Modifier
                .background(MdColors.MdSurfaceColor)

        ) {
            AddMissingPatientInfoSection(
                // Assuming PatientProfileState has a default constructor.
                // Fill in parameters if your data class requires them.
                state = PatientProfileState(
                    missingSections = setOf(
                        Section.PERSONAL_INFO,
                        Section.CONTACT_INFO,
                        Section.CAREGIVER_DETAILS,
                        Section.PRIMARY_INSURANCE,
                        Section.SECONDARY_INSURANCE
                    )
                ),
                onClickAdd = {},

                )
        }
    }
}