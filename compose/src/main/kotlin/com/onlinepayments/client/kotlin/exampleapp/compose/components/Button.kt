/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.compose.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onlinepayments.client.kotlin.exampleapp.compose.theme.Green
import com.onlinepayments.client.kotlin.exampleapp.compose.theme.Mint
import com.onlinepayments.client.kotlin.exampleapp.compose.theme.OnlinePaymentsTheme

@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = ButtonDefaults.elevation(),
    shape: Shape = MaterialTheme.shapes.small,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    showLoadingStatus: Boolean = false,
    text: String
) {
    Button(
        onClick = { onClick() },
        modifier = modifier,
        enabled = if (showLoadingStatus) false else enabled,
        interactionSource = interactionSource,
        elevation = elevation,
        shape = shape,
        border = border,
        colors = ButtonDefaults.buttonColors(backgroundColor = Green),
        contentPadding = contentPadding,
    ) {
        if (showLoadingStatus) {
            CircularProgressIndicator()
        } else {
            Text(text = text, color = Color.White, modifier = Modifier.padding(8.dp))
        }
    }

}

@Composable
fun SecondaryButton(text: String, modifier: Modifier = Modifier, onSecondaryButtonClicked: () -> Unit) {
    OutlinedButton(
        onClick = { onSecondaryButtonClicked() },
        modifier = modifier,
        border = BorderStroke(1.dp, Mint)
    ) {
        Text(text = text, color = Mint)
    }
}

@Preview
@Composable
fun PrimaryButtonPreview() {
    OnlinePaymentsTheme {
        PrimaryButton(text = "Button text", onClick = {})
    }
}

@Preview
@Composable
fun PrimaryButtonLoadingPreview() {
    OnlinePaymentsTheme {
        PrimaryButton(text = "Button Loading", onClick = {}, showLoadingStatus = true)
    }
}

@Preview
@Composable
fun SecondaryButtonPreview() {
    OnlinePaymentsTheme {
        SecondaryButton(text = "Button text", onSecondaryButtonClicked = {})
    }
}
