package com.charm.ehr.kiosk.presentation.select_gender


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdTopToolBar
import com.charm.ehr.kiosk.presentation.settings.SettingsState
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun SelectGenderScreen(
    genderIdentity: List<String>,
    settingsState: SettingsState,
    onGenderSelected: (String) -> Unit,
    onBackPressed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MdColors.MdSurfaceColor)
            .windowInsetsPadding(
                WindowInsets.systemBars.union(WindowInsets.displayCutout)
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            MdTopToolBar(
                headingText = stringResource(id = R.string.default_patient_gender), onBackClick = {
                    onBackPressed()
                })
            MdCardWithoutTonalElevation(
                modifier = Modifier
                    .padding(horizontal = 120.dp, vertical = 32.dp),
                shape = RoundedCornerShape(MdTheme.dimension.cardCornerRadius),
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(stringResource(id = R.string.default_gender_selection_msg))
                    genderIdentity.forEach { gender ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGenderSelected(gender) }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = gender.equals(settingsState.defaultGender, true),
                                onClick = { onGenderSelected(gender) }
                            )
                            Text(text = gender, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PreviewSelectGenderScreen() {
//    val genderList = listOf(
//        "Male",
//        "Female",
//        "Other"
//    )
//    var selectedGender by remember { mutableStateOf(genderList[0]) }
//    SelectGenderScreen(
//        genderIdentity = genderList,
//        selectedGender = selectedGender,
//        onGenderSelected = { selectedGender = it }, {}
//    )
//}
