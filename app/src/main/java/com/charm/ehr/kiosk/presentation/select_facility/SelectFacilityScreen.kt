package com.charm.ehr.kiosk.presentation.select_facility

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.domain.Facility
import com.charm.ehr.kiosk.domain.PostalAddress
import com.charm.ehr.kiosk.presentation.composable.CharmKioskLogoHeader
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdCircularImageView
import com.charm.ehr.kiosk.presentation.composable.MdCircularProgressBar
import com.charm.ehr.kiosk.presentation.composable.SelectionCardItem
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun SelectFacilityScreen(
    memberName: String,
    onClickFacility: (Facility) -> Unit,
    state: FacilityListState,
    onClickRetry: () -> Unit,
    isFromSettings: Boolean,
    onBackPressed: () -> Unit,
    selectedFacility: Facility?,

    ) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MdColors.MdSurfaceColor),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CharmKioskLogoHeader(onBackPressed = {
                onBackPressed()
            }, showBackPress = isFromSettings)
            FacilityCard(
                state = state,
                memberName = memberName,
                onClickFacility = onClickFacility,
                onClickRetry = onClickRetry,
                isFromSettings = isFromSettings,
                selectedFacility = selectedFacility
            )
        }
    }
}

@Composable
fun FacilityCard(
    memberName: String,
    onClickFacility: (Facility) -> Unit,
    onClickRetry: () -> Unit,
    state: FacilityListState,
    isFromSettings: Boolean,
    selectedFacility: Facility?,
) {

    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    MdCardWithoutTonalElevation(
        modifier = Modifier
            .fillMaxWidth(if (isPortrait) 0.7f else 0.5f)
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(MdTheme.dimension.cardCornerRadius),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            if (!isFromSettings) {
                MemberInfo(memberName)
                HorizontalDivider(
                    modifier = Modifier.padding(top = 24.dp),
                    thickness = 1.dp,
                    color = MdColors.Black5
                )
            }


            if (state.isLoading) {
                LoadingIndicator()
            } else if (state.errorMessage != null) {
                ErrorMessageWithRetry(
                    errorMessage = state.errorMessage.asString(),
                    onClickRetry = {
                        onClickRetry()
                    })
            } else if (state.facilityList.isNotEmpty()) {
                FacilityList(state.facilityList, onClickFacility, isFromSettings, selectedFacility)
            }

        }
    }
}

@Composable
fun ErrorMessageWithRetry(errorMessage: String, onClickRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(MdTheme.dimension.minHeightForCard),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = errorMessage,
            style = MdTheme.typography.medium18TextStyle,
            color = MdColors.Black90
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onClickRetry) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                text = stringResource(R.string.retry),
                style = MdTheme.typography.medium18TextStyle,
                color = Color.White
            )
        }
    }
}

@Composable
fun MemberInfo(memberName: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        MdCircularImageView(
            imageResource = R.drawable.kiosk_icon_dummy,
            size = 48.dp,
            shape = RoundedCornerShape(12.dp)
        )
        Text(
            text = memberName,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MdTheme.typography.semiBold18TextStyle
        )
    }
}

@Composable
fun LoadingIndicator() {
    Row(
        modifier = Modifier.height(MdTheme.dimension.minHeightForCard),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MdCircularProgressBar(size = 50.dp)
    }
}

@Composable
fun FacilityList(
    listOfFacility: List<Facility>,
    onClickFacility: (Facility) -> Unit,
    isFromSettings: Boolean,
    selectedFacility: Facility?,
) {
    if (!isFromSettings) {
        Spacer(modifier = Modifier.height(24.dp))
    }
    Text(
        text = stringResource(R.string.select_facility),
        style = MdTheme.typography.semiBold20TextStyle,
    )
    Spacer(modifier = Modifier.height(12.dp))
    LazyColumn {
        items(listOfFacility) { facility ->
            Spacer(modifier = Modifier.height(12.dp))
            SelectionCardItem(
                selected = facility.facilityId == selectedFacility?.facilityId,
                heading = facility.facilityName ?: "",
                message = facility.postalAddress?.toString() ?: "",
                onClick = { onClickFacility(facility) })
        }
    }

}

@Composable
fun FacilityItem(facility: Facility, onClickFacility: (Facility) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onClickFacility(facility) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            text = facility.facilityName ?: "",
            style = MdTheme.typography.medium18TextStyle,
            textAlign = TextAlign.Start
        )
        Icon(
            painter = painterResource(R.drawable.ic_arrow_down),
            contentDescription = "Arrow pointing right",
            modifier = Modifier.rotate(-90f)
        )
        Spacer(modifier = Modifier.width(18.dp))
    }
}

@Composable
@Preview(device = "spec:width=700dp,height=1200dp,dpi=420,isRound=false,chinSize=0dp,orientation=portrait")
fun PreviewChooseFacilityScreen() {

    val listOfFacilityName = listOf(
        Facility(
            facilityName = "Emergency healthcare", postalAddress = PostalAddress(
                line1 = "123 Main St",
                city = "Seattle",
                state = "WA",
                postalCode = "98101",
                country = "USA"
            )
        ),
        Facility(facilityName = "Harborview Medical Center"),
        Facility(facilityName = "Ambulatory surgical Center"),
        Facility(facilityName = "Atlanta Medical Group")
    )

    MdTheme {
        SelectFacilityScreen(
            memberName = "Mary Taylor",
            onClickFacility = {},
            state = FacilityListState(
                isLoading = false,
                errorMessage = null,
                facilityList = listOfFacilityName
            ),
            onClickRetry = {},
            isFromSettings = false,
            {

            }, selectedFacility = null
        )
    }

}