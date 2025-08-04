/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.compose.card.textfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlinepayments.client.kotlin.exampleapp.compose.card.CardFieldVisualTransformation
import com.onlinepayments.client.kotlin.exampleapp.compose.components.OutlinedTextFieldWithError
import com.onlinepayments.client.kotlin.exampleapp.compose.extensions.convertToString
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField

@Composable
fun CardTextField(
    modifier: Modifier = Modifier,
    cardTextFieldState: CardTextFieldState,
    onTrailingIconClicked: (() -> Unit)? = null,
    onValueChanged: (paymentProductField : PaymentProductField, value: String) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextFieldWithError(
        value = cardTextFieldState.text,
        onValueChange = {
            if (it.length <= cardTextFieldState.maxSize) cardTextFieldState.text = it

            cardTextFieldState.paymentProductField?.let { paymentProductField ->
                onValueChanged(
                    paymentProductField,
                    it
                )
            }
        },
        modifier = modifier
            .fillMaxWidth(),
        enabled = cardTextFieldState.enabled,
        placeholder = {
            Text(text = cardTextFieldState.label.convertToString())
        },
        leadingIcon = {
            Icon(imageVector = cardTextFieldState.leadingIcon, null)
        },
        trailingIcon = {
            cardTextFieldState.trailingIcon?.let {
                IconButton(onClick = {
                    if (onTrailingIconClicked != null) {
                        onTrailingIconClicked()
                    }
                }) {
                    Icon(imageVector = it, null)
                }
            }
        },
        error = {
            if (cardTextFieldState.networkErrorMessage.isNotBlank()) {
                Text(
                    text = cardTextFieldState.networkErrorMessage,
                    color = MaterialTheme.colors.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        },
        isError = cardTextFieldState.networkErrorMessage.isNotBlank(),
        visualTransformation = CardFieldVisualTransformation(
            cardTextFieldState.mask
        ),
        keyboardOptions = cardTextFieldState.keyboardOptions,
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            },
            onDone = {
                focusManager.clearFocus()
            }
        )
    )
}
