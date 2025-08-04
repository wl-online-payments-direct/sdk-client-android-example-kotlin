/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.compose.configuration

import android.util.Log
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.onlinepayments.client.kotlin.exampleapp.common.R
import com.onlinepayments.client.kotlin.exampleapp.common.utils.SessionConfiguration
import com.onlinepayments.client.kotlin.exampleapp.compose.components.BottomSheetContent
import com.onlinepayments.client.kotlin.exampleapp.compose.components.CheckBoxField
import com.onlinepayments.client.kotlin.exampleapp.compose.components.IsValid
import com.onlinepayments.client.kotlin.exampleapp.compose.extensions.concatenate
import java.util.Currency
import java.util.Locale

class ConfigurationViewModel : ViewModel() {

    var uiState by mutableStateOf(ConfigurationUiState())
        private set

    fun parseClipBoardData(jsonString: String) {
        try {
            Gson().fromJson(jsonString, SessionConfiguration::class.java).apply {
                uiState.sessionDetailFields[0].text = clientSessionId
                uiState.sessionDetailFields[1].text = customerId
                uiState.sessionDetailFields[2].text = clientApiUrl
                uiState.sessionDetailFields[3].text = assetUrl
            }
        } catch (exception: JsonSyntaxException) {
            // Could not parse clipboard data due to a malformed JSON element
            Log.e(javaClass.name, exception.toString())
        }
    }

    fun validateForm() {
        val googlePayFields =
            if (uiState.otherOptionsFields[2].isChecked.value) uiState.googlePayFields else emptyList()
        val inputFields =
            concatenate(uiState.sessionDetailFields, uiState.paymentDetailsFields, googlePayFields)
        inputFields.forEach { textField ->
            if (textField.isValid is IsValid.No) {
                textField.displayError = true
            }
        }

        if (inputFields.any { it.isValid is IsValid.No }) {
            uiState.configurationStatus.value = ConfigurationStatus.InValid
        } else {
            uiState.configurationStatus.value = ConfigurationStatus.Valid
        }
    }
}

data class ConfigurationUiState(
    val configurationStatus: MutableState<ConfigurationStatus> = mutableStateOf(ConfigurationStatus.None),
    var sessionDetailFields: List<ConfigurationTextFieldState> = listOf(
        ConfigurationTextFieldState(
            label = R.string.payment_configuration_client_session_id_hint,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            bottomSheetContent = BottomSheetContent(R.string.payment_configuration_client_session_id_helper_text)
        ),
        ConfigurationTextFieldState(
            label = R.string.payment_configuration_customer_id_hint,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            bottomSheetContent = BottomSheetContent(R.string.payment_configuration_customer_id_helper_text)
        ),
        ConfigurationTextFieldState(
            label = R.string.payment_configuration_clientApiUrl_hint,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            bottomSheetContent = BottomSheetContent(R.string.payment_configuration_clientApiUrl_helper_text)
        ),
        ConfigurationTextFieldState(
            label = R.string.payment_configuration_assetsUrl_hint,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            bottomSheetContent = BottomSheetContent(R.string.payment_configuration_assetsUrl_helper_text)
        )
    ),
    val paymentDetailsFields: List<ConfigurationTextFieldState> = listOf(
        ConfigurationTextFieldState(
            label = R.string.payment_configuration_amount_hint,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            bottomSheetContent = BottomSheetContent(R.string.payment_configuration_amount_helper_text)
        ),
        ConfigurationTextFieldState(
            text = Locale.getDefault().country,
            label = R.string.payment_configuration_country_code_hint,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            bottomSheetContent = BottomSheetContent(R.string.payment_configuration_country_code_helper_text),
        ),
        ConfigurationTextFieldState(
            text = Currency.getInstance(Locale.getDefault()).currencyCode,
            label = R.string.payment_configuration_currency_code_hint,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            bottomSheetContent = BottomSheetContent(R.string.payment_configuration_currency_code_helper_text)
        )
    ),
    var otherOptionsFields: List<CheckBoxField> = listOf(
        CheckBoxField(
            stringResource = "payment_configuration_recurring_payment",
            bottomSheetContent = BottomSheetContent(R.string.payment_configuration_recurring_payment_helper_text)
        ),
        CheckBoxField(
            stringResource = "payment_configuration_configure_environment_production"
        ),
        CheckBoxField(
            stringResource = "payment_configuration_configure_google_pay"
        )
    ),
    var googlePayFields: List<ConfigurationTextFieldState> = listOf(
        ConfigurationTextFieldState(
            label = R.string.payment_configuration_merchant_id_hint,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            bottomSheetContent = BottomSheetContent(R.string.payment_configuration_merchant_name_helper_text)
        ),
        ConfigurationTextFieldState(
            label = R.string.payment_configuration_merchant_name_hint,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            bottomSheetContent = BottomSheetContent(R.string.payment_configuration_merchant_name_helper_text)
        )
    )
)
