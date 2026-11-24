package com.charm.ehr.kiosk.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charm.ehr.kiosk.R

@Composable
fun MdColorScheme() = lightColorScheme(
    primary = MdColors.BrandPink,
//    secondary = PurpleGrey40,
//    tertiary = Pink40
)

val OpenSansFontFamily = FontFamily(
    fonts = listOf(
        Font(R.font.open_sans_font, FontWeight.Normal),
        Font(R.font.open_sans_light_font, FontWeight.Light),
        Font(R.font.open_sans_medium_font, FontWeight.Medium),
        Font(R.font.open_sans_semi_bold_font, FontWeight.SemiBold)
    )
)

@Composable
fun MdTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {

    val dimensions = getResponsiveDimensions()
    val customTypography = getCustomTypography(dimensions)

    CompositionLocalProvider(
        LocalCustomTypography provides customTypography, LocalDimensions provides dimensions
    ) {
        MaterialTheme(
            colorScheme = MdColorScheme(), content = content
        )
    }

}

object MdTheme {
    val typography: CustomTypography
        @Composable get() = LocalCustomTypography.current
    val dimension: MdDimensions
        @Composable get() = LocalDimensions.current
}

@Composable
fun getResponsiveDimensions(): MdDimensions {
    val screenWidth = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
    return when {
//        screenWidth < 480 -> MdDimensions(
//            small = 12.sp,
//            smMedium = 13.sp,
//            medium = 14.sp,
//            medLarge = 15.sp,
//            large = 16.sp,
//            xLarge = 18.sp,
//            xxLarge = 20.sp,
//            cardPadding = 8.dp,
//            halfCardPadding = 4.dp,
//            patientProfileImageSize = 40.dp,
//            doublePadding = 16.dp,
//            cardCornerRadius = 24.dp,
//            contentPadding = 6.dp,
//            horizontalPadding = 8.dp,
//            minHeightForCard = 200.dp,
//            labelVerticalPadding = 8.dp,
//        )
//
//        screenWidth < 720 -> MdDimensions(
//            small = 12.sp,
//            smMedium = 13.sp,
//            medium = 14.sp,
//            medLarge = 15.sp,
//            large = 16.sp,
//            xLarge = 18.sp,
//            xxLarge = 20.sp,
//            cardPadding = 16.dp,
//            halfCardPadding = 8.dp,
//            patientProfileImageSize = 50.dp,
//            doublePadding = 16.dp,
//            cardCornerRadius = 24.dp,
//            contentPadding = 8.dp,
//            horizontalPadding = 12.dp,
//            minHeightForCard = 200.dp,
//            labelVerticalPadding = 8.dp,
//        )

        else -> MdDimensions(
            small = 12.sp,
            smMedium = 13.sp,
            medium = 14.sp,
            medLarge = 15.sp,
            large = 16.sp,
            xLarge = 18.sp,
            xxLarge = 20.sp,
            cardPadding = 16.dp,
            contentPadding = 12.dp,
            halfCardPadding = 6.dp,
            patientProfileImageSize = 50.dp,
            doublePadding = 32.dp,
            cardCornerRadius = 24.dp,
            horizontalPadding = 12.dp,
            minHeightForCard = 200.dp,
            labelVerticalPadding = 8.dp,
            minHeightForSelectable = 41.dp,
            minHeightSimpleQuestion = 40.dp,
            bottomBarHeight=75.dp,
            finishButtonHeight=44.dp,
            contentCardPadding=24.dp
        )
    }
}

fun getCustomTypography(dimensions: MdDimensions): CustomTypography {
    return CustomTypography(
        medium12TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = dimensions.small,
            color = MdColors.Black86,
        ), medium14TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = dimensions.medium,
            color = MdColors.Black86,
        ), medium16TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = dimensions.large,
            color = MdColors.Black86,
        ), medium18TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = dimensions.xLarge,
            color = MdColors.Black86,
        ), medium20TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = dimensions.xxLarge,
            color = MdColors.Black86,
        ), regular12TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = dimensions.small,
            color = MdColors.Black86,
        ), regular14TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = dimensions.medium,
            color = MdColors.Black86,
        ), regular16TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = dimensions.medLarge,
            color = MdColors.Black86,
        ), regular18TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = dimensions.xLarge,
            color = MdColors.Black86,
        ), regular20TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = dimensions.xxLarge,
            color = MdColors.Black86,
        ),

        semiBold12TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = dimensions.small,
            color = MdColors.Black86
        ), semiBold14TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = dimensions.medium,
            color = MdColors.Black86,
        ), semiBold16TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = dimensions.large,
            color = MdColors.Black86
        ), semiBold18TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = dimensions.xLarge,
            color = MdColors.Black86,
        ), semiBold20TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = dimensions.xxLarge,
            color = MdColors.Black86
        ), semiBold22TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            color = MdColors.Black86,
        ), semiBold24TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            color = MdColors.Black86,
        ),
        bold22TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MdColors.Black86,
        ), bold24TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MdColors.Black86,
        ),
        bold18TextStyle = TextStyle(
            fontFamily = OpenSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MdColors.Black86,
        )
    )
}

@Composable
fun MdFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MdColors.BrandPink,
    unfocusedBorderColor = MdColors.LightBlueGray,
    focusedLabelColor = MdColors.BrandPink,
    unfocusedLabelColor = MdColors.Black86,
    errorBorderColor = MdColors.RedMatt,
    errorLabelColor = MdColors.RedMatt,
    disabledContainerColor = MdColors.disableContainerColor,
    disabledBorderColor = MdColors.blackishGreyLight,
    disabledTextColor = MdColors.disabledTextColor
)