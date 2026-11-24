package com.charm.ehr.kiosk.presentation.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

private val bottomBarButtonModifier = Modifier
    .wrapContentWidth()
    .height(48.dp)

@Composable
fun MdBottomBarForForm(
    modifier: Modifier = Modifier,
    isSaveFlow: Boolean = false,
    onClickNext: () -> Unit,
    onClickSave: () -> Unit,
    showSkip: Boolean,
    onClickSkip: () -> Unit,
    showPrevious: Boolean,
    onClickPrevious: () -> Unit
) {

    val buttonTextStyle = MdTheme.typography.semiBold16TextStyle

    Box(modifier = modifier.fillMaxWidth()) {
        MdCardWithoutTonalElevation(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            elevation = 16.dp,
            shape = RectangleShape
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp) // Align button to the end (right)
            ) {

                if (showPrevious) {
                    MdCommonPrimaryButton(
                        text = stringResource(R.string.previous),
                        onClick = {
                            onClickPrevious()
                        },
                        modifier = bottomBarButtonModifier,
                        buttonColor = MdColors.White,
                        textColor = MdColors.BrandPink,
                        textStyle = buttonTextStyle,
                        startIcon = {
                            Icon(
                                tint = MdColors.BrandPink,
                                painter = painterResource(id = R.drawable.ic_arrow_down),
                                contentDescription = null,
                                modifier = Modifier.rotate(90f),
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (showSkip) {
                    MdCommonPrimaryButton(
                        text = stringResource(R.string.skip),
                        onClick = {
                            onClickSkip()
                        },
                        textStyle = buttonTextStyle,
                        modifier = bottomBarButtonModifier,
                        buttonColor = MdColors.White,
                        textColor = MdColors.BrandPink
                    )
                }

                if (isSaveFlow) {
                    MdHorizontalSpace(2f)
                    MdCommonPrimaryButton(
                        text = stringResource(R.string.save),
                        textStyle = buttonTextStyle,
                        onClick = {
                            onClickSave()
                        },
                        modifier = bottomBarButtonModifier
                    )
                } else {
                    MdHorizontalSpace(2f)
                    MdCommonPrimaryButton(
                        text = stringResource(R.string.next),
                        textStyle = buttonTextStyle,
                        onClick = {
                            onClickNext()
                        },
                        modifier = bottomBarButtonModifier,
                        endIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_down),
                                contentDescription = null,
                                modifier = Modifier.rotate(270f),
                            )
                        }
                    )
                }

                MdHorizontalSpace(2f)

            }

        }
    }

}

@Composable
@Preview(showBackground = true, widthDp = 800, heightDp = 1000)
fun MdBottomBarForFormPreview() {
    MdTheme {
        MdBottomBarForForm(
            onClickSave = {},
            onClickSkip = {},
            onClickNext = {},
            showSkip = true,
            showPrevious = true,
            onClickPrevious = {}
        )
    }
}