package com.charm.ehr.kiosk.presentation.composable

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.charm.ehr.kiosk.R
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme
import se.warting.signaturepad.SignaturePadAdapter
import se.warting.signaturepad.SignaturePadView


@Composable
fun PatientSignatureComponent(
    signatureBitmap: Bitmap?,
    onRequestSign: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isRetry: Boolean = false,
    onRetry: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MdColors.BrandLightWhitishPink).border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .clickable { onRequestSign() },
        contentAlignment = Alignment.Center
    ) {
        if (signatureBitmap != null) {
            Image(
                bitmap = signatureBitmap.asImageBitmap(),
                contentDescription = "Signature",
                modifier = Modifier.matchParentSize()
            )
        } else if (isLoading) {
            // Overlay a loading indicator
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0x80FFFFFF), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                MdCircularProgressBar()
            }
        } else if (isRetry) {
            // Overlay a retry button
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0x80FFFFFF), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = { onRetry() }) {
                    Text("Retry")
                }
            }
        } else {
            Text(
                style = MdTheme.typography.medium16TextStyle,
                text = "Click here to sign",
                color = MdColors.Black86,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SignatureDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onSave: (Bitmap?) -> Unit,
) {
    if (!show) return
    var adapter by remember { mutableStateOf<SignaturePadAdapter?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Signature",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(painter = painterResource(R.drawable.ic_close), contentDescription = "Close")
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    SignaturePadView(
                        onReady = { adapter = it },
                        modifier = Modifier.matchParentSize(),
                        penMinWidth = 2.dp,
                        penMaxWidth = 4.dp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                ) {
                    TextButton(onClick = { adapter?.clear() }) {
                        Text("Clear")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val bmp = adapter?.getTransparentSignatureBitmap()
                        onSave(bmp)
                        onDismiss()
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}


@Composable
fun PatientSignatureWithDialog(
    modifier: Modifier,
    signatureBitmap: Bitmap?,
    onSignatureChanged: (Bitmap?) -> Unit,
    isLoading: Boolean = false,
    isRetry: Boolean = false,
    onRetry: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier.padding(10.dp)) {
        Box {
            PatientSignatureComponent(
                signatureBitmap = signatureBitmap,
                onRequestSign = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                isLoading = isLoading,
                isRetry = isRetry,
                onRetry = onRetry

            )
        }
        if (signatureBitmap != null) {
            Row(Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { showDialog = true }) {
                    Text("Re-sign")
                }
            }
        }
        SignatureDialog(
            show = showDialog,
            onDismiss = { showDialog = false },
            onSave = { bmp -> onSignatureChanged(bmp) }
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewPatientSignatureComponent() {
    Column(Modifier.padding(10.dp)) {
        PatientSignatureComponent(
            signatureBitmap = null,
            onRequestSign = {},
            isLoading = false,
            onRetry = {}
        )
    }

}

@Preview(showSystemUi = true)
@Composable
fun PreviewPatientSignatureWithDialog() {
    var signatureBitmap by remember { mutableStateOf<Bitmap?>(null) }
    MdTheme {
        Box(modifier = Modifier.padding(10.dp)) {
            PatientSignatureWithDialog(
                modifier = Modifier,
                signatureBitmap = signatureBitmap,
                onSignatureChanged = { signatureBitmap = it }
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewSignatureDialog() {
    var showDialog by remember { mutableStateOf(true) }
    MdTheme {
        Box(modifier = Modifier.padding(10.dp)) {
            SignatureDialog(
                show = showDialog,
                onDismiss = { showDialog = false },
                onSave = { /* No-op for preview */ }
            )
        }
    }
}
