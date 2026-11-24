package com.charm.ehr.kiosk.presentation.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@Composable
fun MdAlertDialog(
    heading: String? = null,
    message: String,
    @StringRes primaryButtonResId: Int = R.string.retry,
    @StringRes secondaryButtonResId: Int = R.string.cancel,
    @StringRes tertiaryButtonResId: Int? = null,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit,
    onTertiaryAction: (() -> Unit)? = null,
) {
    Dialog(onDismissRequest = onSecondaryAction) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .padding(24.dp)
                    .defaultMinSize(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MdVerticalSpace()
                    if (heading != null) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = heading,
                            style = MdTheme.typography.semiBold20TextStyle.copy(color = MdColors.BrandPink)
                        )
                        MdVerticalSpace(2f)
                    }
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = message,
                        style = MdTheme.typography.medium18TextStyle,
                        color = Color.Black
                    )
                    MdVerticalSpace(2f)
                    Row {
                        if (tertiaryButtonResId != null && onTertiaryAction != null) {
                            Row(
                                modifier = Modifier.wrapContentWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                TextButton(
                                    modifier = Modifier.defaultMinSize(minWidth = 96.dp),
                                    onClick = onTertiaryAction
                                ) {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        text = stringResource(tertiaryButtonResId),
                                        style = MdTheme.typography.medium18TextStyle.copy(color = MdColors.BrandPink)
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            //region Secondary
                            TextButton(
                                modifier = Modifier.defaultMinSize(minWidth = 96.dp),
                                onClick = onSecondaryAction
                            ) {
                                Text(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    text = stringResource(secondaryButtonResId),
                                    style = MdTheme.typography.medium18TextStyle.copy(color = MdColors.BrandPink)
                                )
                            }
                            //endregion
                            Spacer(modifier = Modifier.width(16.dp))
                            //region Primary
                            TextButton(
                                modifier = Modifier.defaultMinSize(minWidth = 96.dp),
                                onClick = onPrimaryAction,
                                colors = ButtonDefaults.textButtonColors(containerColor = MdColors.BrandLightPink)
                            ) {
                                Text(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    text = stringResource(primaryButtonResId),
                                    style = MdTheme.typography.medium18TextStyle.copy(color = MdColors.BrandPink)
                                )
                            }
                            //endregion
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 800, heightDp = 1000)
@Composable
fun MdAlertDialogPreview() {
    MdTheme {
        MdAlertDialog(
            heading = "Preview Alert",
            message = "This is a preview alert message. asdfasdf asdfasdfasdf asdfa asdf asdf asdfasdf asadfasdf asdf asdfasdf asdfasdfsadf  asdf sadf asdf sdfsdf asdf ",
            onPrimaryAction = {},
            onSecondaryAction = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 1000)
@Composable
fun MdAlertDialogPreviewWithThirdButton() {
    MdTheme {
        MdAlertDialog(
            heading = "Preview Alert with Three Buttons",
            message = "This is a preview alert message with three buttons.",
            primaryButtonResId = R.string.save,
            secondaryButtonResId = R.string.discard,
            tertiaryButtonResId = R.string.cancel, // Example third button
            onPrimaryAction = {},
            onSecondaryAction = {},
            onTertiaryAction = {}
        )
    }
}

@Composable
fun ShowOpenSettingDialog(
    permission: String,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit
) {
    MdAlertDialog(
        heading = stringResource(R.string.turn_on_permission, permission),
        message = stringResource(
            R.string.guide_for_permission_str,
            permission
        ),
        primaryButtonResId = R.string.settings,
        secondaryButtonResId = R.string.cancel,
        onPrimaryAction = onPrimaryAction,
        onSecondaryAction = onSecondaryAction
    )
}

@Composable
fun ShowEducationalDialog(
    permission: String,
    message: String,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit
) {
    MdAlertDialog(
        heading = stringResource(R.string.allow_access, permission),
        message = message,
        primaryButtonResId = R.string.allow,
        secondaryButtonResId = R.string.cancel,
        onPrimaryAction = onPrimaryAction,
        onSecondaryAction = onSecondaryAction
    )
}

@Composable
fun MdMessageDialog(
    title: String,
    message: String,
    okButtonText: String = stringResource(R.string.alertDialogOkTextNoI18N),
    allowDismiss: Boolean = true,
    onOk: () -> Unit,
) {
    Dialog(
        onDismissRequest = if (allowDismiss) onOk else {
        {}
    }) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .padding(24.dp)
                    .defaultMinSize(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (title.isNotEmpty()) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = title,
                            style = MdTheme.typography.semiBold20TextStyle.copy(color = MdColors.BrandPink)
                        )
                    }
                    MdVerticalSpace()
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = message,
                        style = MdTheme.typography.medium18TextStyle,
                        color = Color.Black
                    )
                    MdVerticalSpace(2f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            modifier = Modifier.defaultMinSize(minWidth = 96.dp),
                            onClick = onOk,
                            colors = ButtonDefaults.textButtonColors(containerColor = MdColors.BrandLightPink)
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = okButtonText,
                                style = MdTheme.typography.medium18TextStyle.copy(color = MdColors.BrandPink)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MdMessageDialog(
    title: String,
    messageAnnotated: AnnotatedString,
    okButtonText: String = stringResource(R.string.alertDialogOkTextNoI18N),
    allowDismiss: Boolean = true,
    onOk: () -> Unit,
) {
    Dialog(
        onDismissRequest = if (allowDismiss) onOk else {
        {}
    }) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .padding(24.dp)
                    .defaultMinSize(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (title.isNotEmpty()) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = title,
                            style = MdTheme.typography.semiBold20TextStyle.copy(color = MdColors.BrandPink)
                        )
                    }
                    MdVerticalSpace()
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = messageAnnotated,
                        style = MdTheme.typography.medium18TextStyle,
                        color = Color.Black
                    )
                    MdVerticalSpace(2f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            modifier = Modifier.defaultMinSize(minWidth = 96.dp),
                            onClick = onOk,
                            colors = ButtonDefaults.textButtonColors(containerColor = MdColors.BrandLightPink)
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = okButtonText,
                                style = MdTheme.typography.medium18TextStyle.copy(color = MdColors.BrandPink)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 800, heightDp = 1000)
@Composable
fun MdSimpleAlertDialogPreview() {
    MdTheme {
        MdMessageDialog(
            title = "Simple Alert",
            message = "This is a simple alert dialog with only an OK button.",
            onOk = {}
        )
    }
}
