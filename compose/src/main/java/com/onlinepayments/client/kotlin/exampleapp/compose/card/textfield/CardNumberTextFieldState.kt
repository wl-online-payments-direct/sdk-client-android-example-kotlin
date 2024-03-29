/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.compose.card.textfield

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.onlinepayments.client.kotlin.exampleapp.common.R
import com.onlinepayments.client.kotlin.exampleapp.compose.components.IsValid
import com.onlinepayments.client.kotlin.exampleapp.compose.components.TextFieldState
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField

/**
 * Class for holding the state of a Card number field.
 * Derivative of the parent class TextFieldState
 */
class CardNumberTextFieldState(
    text: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    label: Any = "",
    enabled: Boolean = true,
    liveValidating: Boolean = false,
    val leadingIcon: ImageVector,
    trailingIconUrl: String = "",
    isLoading: Boolean = false,
    val id : String,
    var mask: String? = null,
    var maxSize: Int = Int.MAX_VALUE,
    var lastCheckedCardValue: String = "",
    var paymentProductField: PaymentProductField? = null
) : TextFieldState(
    validator = ::isFieldValid,
    errorText = ::fieldValidationError,
    text = text,
    keyboardOptions = keyboardOptions,
    label = label,
    enabled = enabled,
    liveValidating = liveValidating
) {
    var trailingImageUrl: String? by mutableStateOf(trailingIconUrl)
    var isLoading: Boolean by mutableStateOf(isLoading)
    var networkErrorMessage: String by mutableStateOf("")
}

private fun fieldValidationError(errorText: Any): Any {
    return errorText
}

private fun isFieldValid(value: String): IsValid {
    return if (value.isNotBlank()) IsValid.Yes else IsValid.No(R.string.payment_configuration_field_not_valid_error)
}
