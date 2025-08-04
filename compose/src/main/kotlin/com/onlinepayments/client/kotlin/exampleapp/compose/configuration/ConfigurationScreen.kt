/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.compose.configuration

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.onlinepayments.client.kotlin.exampleapp.common.BuildConfig
import com.onlinepayments.client.kotlin.exampleapp.common.PaymentSharedViewModel
import com.onlinepayments.client.kotlin.exampleapp.common.R
import com.onlinepayments.client.kotlin.exampleapp.common.googlepay.GooglePayConfiguration
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants
import com.onlinepayments.client.kotlin.exampleapp.common.utils.PaymentScreen
import com.onlinepayments.client.kotlin.exampleapp.compose.components.BottomSheetContent
import com.onlinepayments.client.kotlin.exampleapp.compose.components.CheckBoxField
import com.onlinepayments.client.kotlin.exampleapp.compose.components.LabelledCheckbox
import com.onlinepayments.client.kotlin.exampleapp.compose.components.PrimaryButton
import com.onlinepayments.client.kotlin.exampleapp.compose.components.SecondaryButton
import com.onlinepayments.client.kotlin.exampleapp.compose.components.SectionTitle
import com.onlinepayments.client.kotlin.exampleapp.compose.theme.OnlinePaymentsTheme
import com.onlinepayments.sdk.client.android.model.AmountOfMoney
import com.onlinepayments.sdk.client.android.model.PaymentContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@Composable
fun ConfigurationScreen(
    navController: NavHostController,
    paymentSharedViewModel: PaymentSharedViewModel,
    configurationViewModel: ConfigurationViewModel,
    showBottomSheet: (BottomSheetContent) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val uiState = configurationViewModel.uiState
    loadConfigurationFieldsFromDataStore(paymentSharedViewModel) { configurationFields ->
        uiState.sessionDetailFields[0].text =
            configurationFields[PaymentSharedViewModel.CLIENT_SESSION_ID_KEY.name] ?: ""
        uiState.sessionDetailFields[1].text =
            configurationFields[PaymentSharedViewModel.CUSTOMER_ID_KEY.name] ?: ""
        uiState.sessionDetailFields[2].text =
            configurationFields[PaymentSharedViewModel.CLIENT_API_URL_KEY.name] ?: ""
        uiState.sessionDetailFields[3].text =
            configurationFields[PaymentSharedViewModel.ASSET_URL_KEY.name] ?: ""
        uiState.paymentDetailsFields[0].text =
            configurationFields[PaymentSharedViewModel.AMOUNT_KEY.name] ?: ""
        uiState.paymentDetailsFields[1].text =
            configurationFields[PaymentSharedViewModel.COUNTRY_CODE_KEY.name] ?: ""
        uiState.paymentDetailsFields[2].text =
            configurationFields[PaymentSharedViewModel.CURRENCY_CODE_KEY.name] ?: ""
    }

    if (uiState.configurationStatus.value is ConfigurationStatus.Valid) {
        paymentSharedViewModel.setPaymentContext(
            PaymentContext(
                AmountOfMoney(
                    uiState.paymentDetailsFields[0].text.toLong(),
                    uiState.paymentDetailsFields[2].text
                ),
                uiState.paymentDetailsFields[1].text,
                uiState.otherOptionsFields[0].isChecked.value,
            )
        )

        paymentSharedViewModel.initializeSession(
            uiState.sessionDetailFields[0].text,
            uiState.sessionDetailFields[1].text,
            uiState.sessionDetailFields[2].text,
            uiState.sessionDetailFields[3].text,
            uiState.otherOptionsFields[1].isChecked.value,
            Constants.APPLICATION_IDENTIFIER,
            BuildConfig.LOGGING_ENABLED
        )

        if (uiState.otherOptionsFields[2].isChecked.value) {
            paymentSharedViewModel.googlePayConfiguration = GooglePayConfiguration(
                true,
                uiState.googlePayFields[0].text,
                uiState.googlePayFields[1].text
            )
        }

        uiState.configurationStatus.value = ConfigurationStatus.None
        navController.navigate(PaymentScreen.PRODUCT.route)
    }

    ConfigurationContent(uiState,
        onPrimaryButtonClicked = { configurationViewModel.validateForm() },
        onSecondaryButtonClicked = {
            configurationViewModel.parseClipBoardData(
                clipboard.getText().toString()
            )
        },
        showBottomSheet = { showBottomSheet(it) })
}

private fun loadConfigurationFieldsFromDataStore(
    paymentSharedViewModel: PaymentSharedViewModel,
    onLoadingComplete: (Map<String, String>) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        paymentSharedViewModel.loadFromDataStore()
            .catch { exception ->
                // An error occurred when reading the input from DataStore
                Log.e(javaClass.name, exception.toString())
            }
            .collect { sessionConfiguration ->
                onLoadingComplete(sessionConfiguration)
            }
    }
}

@Composable
private fun ConfigurationContent(
    uiState: ConfigurationUiState,
    onPrimaryButtonClicked: () -> Unit,
    onSecondaryButtonClicked: () -> Unit,
    showBottomSheet: (BottomSheetContent) -> Unit
) {

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        SectionTitle(stringResource(id = R.string.payment_configuration_client_session_details), modifier = Modifier)
        uiState.sessionDetailFields.forEach { configurationInputField ->
            ConfigurationTextField(
                textFieldState = configurationInputField,
                onTrailingIconClicked = {
                    showBottomSheet(configurationInputField.bottomSheetContent)
                })
        }
        SecondaryButton(
            text = stringResource(id = R.string.payment_configuration_paste_json),
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.End), onSecondaryButtonClicked = { onSecondaryButtonClicked() })
        SectionTitle(
            stringResource(
            id = R.string.payment_configuration_payment_details),
            modifier = Modifier.padding(top = 32.dp)
        )
        uiState.paymentDetailsFields.forEach { configurationInputField ->
            ConfigurationTextField(
                textFieldState = configurationInputField,
                onTrailingIconClicked = {
                    showBottomSheet(configurationInputField.bottomSheetContent)
                })
        }
        SectionTitle(
            text = stringResource(id = R.string.payment_configuration_other_options),
            modifier = Modifier.padding(top = 32.dp)
        )
        LabelledCheckbox(checkBoxField = uiState.otherOptionsFields[0], onTrailingIconClicked = {
            showBottomSheet(uiState.otherOptionsFields[0].bottomSheetContent)
        })
        LabelledCheckbox(checkBoxField = uiState.otherOptionsFields[1])
        GooglePaySection(
            checkBoxField = uiState.otherOptionsFields[2],
            uiState.googlePayFields,
            showBottomSheet = { showBottomSheet(it) }
        )
        PrimaryButton(
            text = stringResource(id = R.string.payment_configuration_proceed_to_checkout),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .align(Alignment.CenterHorizontally),

            onClick = { onPrimaryButtonClicked() }
        )
    }
}

@Composable
private fun GooglePaySection(
    checkBoxField: CheckBoxField,
    googlePayFields: List<ConfigurationTextFieldState>,
    showBottomSheet: (BottomSheetContent) -> Unit
) {
    Row {
        Checkbox(
            checked = checkBoxField.isChecked.value,
            onCheckedChange = { checkBoxField.isChecked.value = it },
            enabled = checkBoxField.enabled.value
        )
        Column(modifier = Modifier.padding(start = 4.dp, top = 13.dp)) {
            Text(text = stringResource(id = R.string.payment_configuration_configure_google_pay))
            if (checkBoxField.isChecked.value) {
                googlePayFields.forEach { configurationInputField ->
                    ConfigurationTextField(
                        textFieldState = configurationInputField,
                        onTrailingIconClicked = {
                            showBottomSheet(configurationInputField.bottomSheetContent)
                        })
                }
            }
        }
    }
}

@Preview
@Composable
fun ConfigurationScreenPreview() {
    OnlinePaymentsTheme {
        ConfigurationContent(
            uiState = ConfigurationUiState(),
            onPrimaryButtonClicked = {},
            onSecondaryButtonClicked = {},
            showBottomSheet = {}
        )
    }
}
