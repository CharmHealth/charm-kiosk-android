package com.charm.ehr.kiosk.presentation.tele_health

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TelehealthScreen(state: TelehealthState, onAction: (TelehealthAction) -> Unit) {
// .windowInsetsPadding(
//                WindowInsets.systemBars.union(WindowInsets.displayCutout)
//            )
}

@Composable
fun ItemTeleHealth() {

    Row {
        //Patient Image

        //Name Coloumn

        //Time Coloumn

        //Chenk In Button

    }
}

@Composable
@Preview(showBackground = true, widthDp = 700, heightDp = 1000)
fun ItemTeleHealthPreview() {
    ItemTeleHealth()
}
