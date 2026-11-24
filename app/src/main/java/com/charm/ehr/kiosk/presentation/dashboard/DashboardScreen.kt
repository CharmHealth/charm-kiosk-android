package com.charm.ehr.kiosk.presentation.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.common.util.MDAccountsRelatedUtil
import com.charm.ehr.kiosk.common.util.UIUtil
import com.charm.ehr.kiosk.domain.Facility
import com.charm.ehr.kiosk.presentation.composable.MdCardWithoutTonalElevation
import com.charm.ehr.kiosk.presentation.composable.MdVerticalSpace
import com.charm.ehr.kiosk.presentation.dashboard.add_patient.AddPatientCard
import com.charm.ehr.kiosk.presentation.dashboard.add_patient.AddPatientState
import com.charm.ehr.kiosk.presentation.dashboard.search_patient.AddPatientAction
import com.charm.ehr.kiosk.presentation.dashboard.search_patient.DashboardSection
import com.charm.ehr.kiosk.presentation.dashboard.search_patient.SearchPatientAction
import com.charm.ehr.kiosk.presentation.dashboard.search_patient.SearchPatientCard
import com.charm.ehr.kiosk.presentation.dashboard.search_patient.SearchPatientState
import com.charm.ehr.kiosk.presentation.settings.SettingsAction
import com.charm.ehr.kiosk.presentation.settings.SettingsScreen
import com.charm.ehr.kiosk.presentation.settings.SettingsState
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import java.util.UUID

@Composable
fun getDashboardMenuList(): List<NavigationItem> {
    val dashboardMenuList = listOf(
        NavigationItem(
            title = stringResource(R.string.add),
            selectedIcon = painterResource(R.drawable.ic_add_new_patient),
            unselectedIcon = painterResource(R.drawable.ic_add_new_patient), //TODO:Change filled icon
            hasNews = false,
            dashboardSection = DashboardSection.ADD,
        ),
        NavigationItem(
            title = stringResource(R.string.search),
            selectedIcon = painterResource(R.drawable.ic_search_new),
            unselectedIcon = painterResource(R.drawable.ic_search_new), //TODO:Change filled icon
            hasNews = false,
            dashboardSection = DashboardSection.SEARCH,
        ),
        /*   NavigationItem(
               title = "Telehealth",
               selectedIcon = Icons.Filled.Call,
               unselectedIcon = Icons.Outlined.Call,
               hasNews = false,
           ),*/
        NavigationItem(
            title = stringResource(R.string.settings),
            selectedIcon = painterResource(R.drawable.ic_setting),
            unselectedIcon = painterResource(R.drawable.ic_setting),//TODO:Change filled icon
            hasNews = false,
            dashboardSection = DashboardSection.SETTINGS,
        ),
    )
    return dashboardMenuList
}

@Composable
fun DashboardScreen(
    imageLoader: ImageLoader,
    onAddPatientAction: (AddPatientAction) -> Unit,
    onSearchPatientAction: (SearchPatientAction) -> Unit,
    onSettingsAction: (SettingsAction) -> Unit,
    addPatientState: AddPatientState,
    searchPatientState: SearchPatientState,
    settingState: SettingsState,
    navigateToPatientProfile: (patientId: String) -> Unit,
    selectedFacility: Facility?,
    loginName: String,
    genderOptionList: List<String>,
    isStandAloneUser: Boolean,
    refreshWorkId: UUID?
) {


    Row(
        modifier = Modifier
            .background(MdColors.MdSurfaceColor)
            .windowInsetsPadding(
                WindowInsets.systemBars.union(WindowInsets.displayCutout).only(
                    WindowInsetsSides.Top
                )
            )
    ) {
        NavigationSideBar(
            items = getDashboardMenuList(),
            dashBoardSection = searchPatientState.selectedDashboardSection,
            onNavigate = {
                onSearchPatientAction(SearchPatientAction.OnUpdateSelectedItemIndex(it))
            })

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            LoginInfo(
                userDisplayName = loginName,
                selectedFacility = selectedFacility,
                imageLoader = imageLoader
            )

            MdCardWithoutTonalElevation(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(MdColors.MdSurfaceColor)
                    .padding(
                        start = 0.dp, top = 18.dp, end = 18.dp, bottom = 0.dp
                    ),
                shape = RoundedCornerShape(
                    topStart = MdTheme.dimension.cardCornerRadius,
                    topEnd = MdTheme.dimension.cardCornerRadius,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ),
                elevation = 0.dp
            ) {
                when (searchPatientState.selectedDashboardSection) { //Need to move to Common state
                    DashboardSection.ADD -> {
                        AddPatientCard(
                            state = addPatientState,
                            onAction = {
                                onAddPatientAction(it)
                            },
                            imageLoader = imageLoader,
                            navigateToPatientProfile = {
                                navigateToPatientProfile(it)
                            },
                            genderOptionList = genderOptionList
                        )
                    }

                    DashboardSection.SEARCH -> {
                        SearchPatientCard(
                            state = searchPatientState,
                            imageLoader = imageLoader,
                            onClickPatient = {
                                onSearchPatientAction(SearchPatientAction.OnClickPatient(it))
                            },
                            onUpdateSearchType = {
                                onSearchPatientAction(SearchPatientAction.OnUpdateSearchType(it))
                            },
                            onChangeSearchQuery = {
                                onSearchPatientAction(SearchPatientAction.OnChangeSearchQuery(it))
                            },
                            onAddPatient = {
                                onSearchPatientAction(SearchPatientAction.OnAddPatient)
                            }
                        )
                    }

                    DashboardSection.SETTINGS -> {
                        SettingsScreen(settingState, onAction = {
                            onSettingsAction(it)
                        }, selectedFacility, isStandAloneUser,refreshWorkId)
                    }

                    else -> {}
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationIcon(
    item: NavigationItem,
    selected: Boolean,
) {
    BadgedBox(badge = {
        if (item.badgeCount != null) {
            Badge {
                Text(text = item.badgeCount.toString())
            }
        } else if (item.hasNews) {
            Badge()
        }
    }) {
        Icon(
            painter = if (selected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.title,
            tint = if (selected) MdColors.BrandPink else LocalContentColor.current
        )
    }
}

@Composable
fun NavigationSideBar(
    items: List<NavigationItem>,
    dashBoardSection: DashboardSection,
    onNavigate: (DashboardSection) -> Unit,
) {
    NavigationRail(
        modifier = Modifier.padding(horizontal = 10.dp),
        header = {
            Image(
                painterResource(id = R.drawable.ic_charm_logo),
                contentDescription = "",
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(20.dp)
            )
        }, containerColor = MdColors.MdSurfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 100.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top)
        ) {
            items.forEachIndexed { index, item ->
//                if (index == items.lastIndex) {
//                    Spacer(Modifier.weight(1f))
//                }
                NavigationRailItem(
                    selected = dashBoardSection == item.dashboardSection,
                    onClick = {
                        onNavigate(item.dashboardSection)
                    },
                    icon = {
                        NavigationIcon(
                            item = item, selected = dashBoardSection == item.dashboardSection
                        )
                    },
                    label = {
                        Text(text = item.title, style =if(dashBoardSection == item.dashboardSection) MdTheme.typography.semiBold14TextStyle else  MdTheme.typography.regular14TextStyle)
                    },
                    colors = NavigationRailItemDefaults.colors(
                        indicatorColor = if (dashBoardSection == item.dashboardSection) MdColors.BrandLightPink else MdColors.MdSurfaceColor
                    )
                )
//                if (index == items.lastIndex) {
//                    MdVerticalSpace()
//                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 800, heightDp = 1200)
fun DashboardScreenPreview() {
    MdTheme {
        DashboardScreen(
            imageLoader = ImageLoader.Builder(LocalContext.current).build(),
            onAddPatientAction = {},
            onSearchPatientAction = {},
            onSettingsAction = {},
            addPatientState = AddPatientState(),
            searchPatientState = SearchPatientState(selectedDashboardSection = DashboardSection.ADD),
            settingState = SettingsState(),
            navigateToPatientProfile = {},
            selectedFacility = Facility(facilityId = "sample_id", facilityName = "Sample Facility"),
            loginName = "John Doe", listOf("Male", "Female"), isStandAloneUser = false, refreshWorkId = null
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 800, heightDp = 1200)
fun DashboardScreenSearchPatientPreview() {
    MdTheme {
        DashboardScreen(
            imageLoader = ImageLoader.Builder(LocalContext.current).build(),
            onAddPatientAction = {},
            onSearchPatientAction = {},
            onSettingsAction = {},
            addPatientState = AddPatientState(),
            searchPatientState = SearchPatientState(selectedDashboardSection = DashboardSection.SEARCH),
            settingState = SettingsState(),
            navigateToPatientProfile = {},
            selectedFacility = Facility(facilityId = "sample_id", facilityName = "Sample Facility"),
            loginName = "John Doe", listOf("Male", "Female"),isStandAloneUser = false,refreshWorkId=null
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 800, heightDp = 1200)
fun DashboardScreenSettingPreview() {
    MdTheme {
        DashboardScreen(
            imageLoader = ImageLoader.Builder(LocalContext.current).build(),
            onAddPatientAction = {},
            onSearchPatientAction = {},
            onSettingsAction = {},
            addPatientState = AddPatientState(),
            searchPatientState = SearchPatientState(selectedDashboardSection = DashboardSection.SETTINGS),
            settingState = SettingsState(),
            navigateToPatientProfile = {},
            selectedFacility = Facility(facilityId = "sample_id", facilityName = "Sample Facility"),
            loginName = "John Doe", listOf("Male", "Female"),isStandAloneUser = false, refreshWorkId = null
        )
    }
}

data class NavigationItem(
    val title: String,
    val unselectedIcon: Painter,
    val selectedIcon: Painter,
    val hasNews: Boolean,
    val badgeCount: Int? = null,
    val dashboardSection: DashboardSection
)

@Composable
fun LoginInfo(userDisplayName: String, selectedFacility: Facility?, imageLoader: ImageLoader) {
    val facilityId = selectedFacility?.facilityId ?: ""
    val facilityName = selectedFacility?.facilityName ?: ""
    val facilityLogoUrl = MDAccountsRelatedUtil.getFacilityLogoUrl(facilityId)
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, end = 26.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 14.dp), // Attach texts to logo
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = facilityName,
                style = MdTheme.typography.semiBold16TextStyle,
                textAlign = TextAlign.End
            )
            MdVerticalSpace(.5f)
            Text(
                text = userDisplayName,
                style = MdTheme.typography.regular14TextStyle,
                textAlign = TextAlign.End
            )
        }
        if (facilityLogoUrl.isNotBlank()) {

            val imageHeight = 48.dp
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(facilityLogoUrl)
                    .size(UIUtil.dpToPx(context = context, dp = imageHeight))
                    .build(),
                imageLoader = imageLoader,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .heightIn(max = imageHeight)
            ) {
                val painterState = painter.state
                if (painterState is AsyncImagePainter.State.Success) {
                    SubcomposeAsyncImageContent()
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_facility_placeholder),
                        null,
                        modifier = Modifier.height(imageHeight)
                    )
                }
            }
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_facility_placeholder),
                null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 4.dp)
            )
        }
    }
}
