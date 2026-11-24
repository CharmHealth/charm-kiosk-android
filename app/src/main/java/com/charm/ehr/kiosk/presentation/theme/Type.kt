package com.charm.ehr.kiosk.presentation.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

//Regular w400
//Medium w500
//SemiBold w600
//Bold w700

@Preview(showBackground = true)
@Composable
fun typographyPreview(){
    MdTheme {
        Column {

            Text("Heading Small: The quick brown ", style = MaterialTheme.typography.headlineSmall)
            Text("Heading Medium: The quick brown ", style = MaterialTheme.typography.headlineMedium)
            Text("Heading Large: The quick brown ", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Title Small: The quick brown ", style = MaterialTheme.typography.titleSmall)
            Text("Title Medium: The quick brown ", style = MaterialTheme.typography.titleMedium)
            Text("Title Large: The quick brown ", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Body Small: The quick brown ", style = MaterialTheme.typography.bodySmall)
            Text("Body Medium: The quick brown ", style = MaterialTheme.typography.bodyMedium)
            Text("Body Large: The quick brown ", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Label Small: The quick brown ", style = MaterialTheme.typography.labelSmall)
            Text("Label Medium: The quick brown ", style = MaterialTheme.typography.labelMedium)
            Text("Label Large: The quick brown ", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Regular 20: The quick brown ", style = MdTheme.typography.regular20TextStyle)
            Text("Regular 18: The quick brown ", style = MdTheme.typography.regular18TextStyle)
            Text("Regular 16: The quick brown ", style = MdTheme.typography.regular16TextStyle)
            Text("Regular 14: The quick brown ", style = MdTheme.typography.regular14TextStyle)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Medium 20: The quick brown ", style = MdTheme.typography.medium20TextStyle)
            Text("Medium 18: The quick brown ", style = MdTheme.typography.medium18TextStyle)
            Text("Medium 16: The quick brown ", style = MdTheme.typography.medium16TextStyle)
            Text("Medium 14: The quick brown ", style = MdTheme.typography.medium14TextStyle)
            Spacer(modifier = Modifier.height(8.dp))
            Text("SemiBold 20: The quick brown ", style = MdTheme.typography.semiBold20TextStyle)
            Text("SemiBold 18: The quick brown ", style = MdTheme.typography.semiBold18TextStyle)
            Text("SemiBold 16: The quick brown ", style = MdTheme.typography.semiBold16TextStyle)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Bold 24: The quick brown ", style = MdTheme.typography.bold24TextStyle)
            Text("Bold 22: The quick brown ", style = MdTheme.typography.bold22TextStyle)

        }
    }
}




