package com.charm.ehr.kiosk.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MdDimensions(
    val small: TextUnit,
    val smMedium: TextUnit,
    val medium: TextUnit,
    val medLarge: TextUnit,
    val large: TextUnit,
    val xLarge: TextUnit,
    val xxLarge: TextUnit,
    val doublePadding: Dp,
    val cardPadding: Dp,
    val contentCardPadding: Dp,
    val halfCardPadding: Dp,
    val patientProfileImageSize: Dp,
    val cardCornerRadius: Dp,
    val contentPadding: Dp,
    val horizontalPadding: Dp,
    val labelVerticalPadding: Dp,
    val minHeightForCard: Dp,
    val minHeightForSelectable: Dp,
    val minHeightSimpleQuestion: Dp,
    val bottomBarHeight:Dp,
    val finishButtonHeight:Dp,
)

val LocalDimensions = staticCompositionLocalOf {
    MdDimensions(
        small = 12.sp,
        smMedium = 13.sp,
        medium = 14.sp,
        medLarge = 15.sp,
        large = 16.sp,
        xLarge = 18.sp,
        xxLarge = 20.sp,
        cardPadding = 8.dp,
        halfCardPadding = 4.dp,
        patientProfileImageSize = 50.dp,
        doublePadding = 16.dp,
        cardCornerRadius = 24.dp,
        contentPadding = 6.dp,
        horizontalPadding = 8.dp,
        labelVerticalPadding = 6.dp,
        minHeightForCard = 200.dp,
        minHeightForSelectable = 41.dp,
        minHeightSimpleQuestion = 40.dp,
        bottomBarHeight=75.dp,
        finishButtonHeight=44.dp,
         contentCardPadding=24.dp
    )
}