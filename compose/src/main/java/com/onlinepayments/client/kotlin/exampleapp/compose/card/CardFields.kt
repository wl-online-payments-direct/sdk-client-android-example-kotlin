/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.compose.card

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.text.input.KeyboardType
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants.CARD_HOLDER
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants.CARD_NUMBER
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants.EXPIRY_DATE
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants.SECURITY_NUMBER
import com.onlinepayments.client.kotlin.exampleapp.compose.card.textfield.CardNumberTextFieldState
import com.onlinepayments.client.kotlin.exampleapp.compose.card.textfield.CardTextFieldState
import com.onlinepayments.client.kotlin.exampleapp.compose.components.CheckBoxField

data class CardFields(
    val cardNumberField: CardNumberTextFieldState = CardNumberTextFieldState(
        leadingIcon = Icons.Filled.CreditCard,
        id = CARD_NUMBER,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

    ),
    val expiryDateField: CardTextFieldState = CardTextFieldState(
        leadingIcon = Icons.Filled.CalendarToday,
        id = EXPIRY_DATE,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    ),
    val securityNumberField: CardTextFieldState = CardTextFieldState(
        leadingIcon = Icons.Filled.Lock,
        trailingIcon = Icons.Outlined.Info,
        id = SECURITY_NUMBER,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    ),
    val cardHolderField: CardTextFieldState = CardTextFieldState(
        leadingIcon = Icons.Filled.Person,
        id = CARD_HOLDER
    ),
    val rememberCardField: CheckBoxField = CheckBoxField("paymentProductDetails_rememberMe")
)
