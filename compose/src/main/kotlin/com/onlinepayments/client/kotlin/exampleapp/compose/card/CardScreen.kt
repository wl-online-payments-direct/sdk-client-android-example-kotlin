/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.compose.card

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.onlinepayments.client.kotlin.exampleapp.common.PaymentCardViewModel
import com.onlinepayments.client.kotlin.exampleapp.common.PaymentSharedViewModel
import com.onlinepayments.client.kotlin.exampleapp.common.utils.FormValidationResult
import com.onlinepayments.client.kotlin.exampleapp.common.utils.PaymentCardUIState
import com.onlinepayments.client.kotlin.exampleapp.common.utils.PaymentScreen
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Status
import com.onlinepayments.client.kotlin.exampleapp.common.utils.StringProvider
import com.onlinepayments.client.kotlin.exampleapp.compose.card.textfield.CardNumberField
import com.onlinepayments.client.kotlin.exampleapp.compose.card.textfield.CardTextField
import com.onlinepayments.client.kotlin.exampleapp.compose.components.BottomSheetContent
import com.onlinepayments.client.kotlin.exampleapp.compose.components.FailedText
import com.onlinepayments.client.kotlin.exampleapp.compose.components.LabelledCheckbox
import com.onlinepayments.client.kotlin.exampleapp.compose.components.PrimaryButton
import com.onlinepayments.client.kotlin.exampleapp.compose.components.ProgressIndicator
import com.onlinepayments.sdk.client.android.model.PreparedPaymentRequest
import com.onlinepayments.sdk.client.android.model.iin.IinStatus
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField

@ExperimentalComposeUiApi
@Composable
fun CardScreen(
    navController: NavHostController,
    paymentSharedViewModel: PaymentSharedViewModel,
    showBottomSheet: (BottomSheetContent) -> Unit,
    paymentCardViewModel: PaymentCardViewModel = viewModel(),
    cardScreenViewModel: CardScreenViewModel = viewModel()
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val cardFields = cardScreenViewModel.cardFields

    LaunchedEffect(true) {
        paymentCardViewModel.session = paymentSharedViewModel.session
        paymentCardViewModel.paymentContext = paymentSharedViewModel.paymentContext
        paymentCardViewModel.getPaymentProduct(paymentSharedViewModel.selectedPaymentProduct)
    }

    val formValidationResult by
    paymentCardViewModel.formValidationResult.observeAsState(FormValidationResult.NotValidated)

    analyzeFormValidationResult(formValidationResult, cardScreenViewModel)

    val encryptedPaymentRequestStatus by paymentCardViewModel.encryptedPaymentRequestStatus.observeAsState(
        Status.None
    )

    when (encryptedPaymentRequestStatus) {
        is Status.ApiError -> {
            cardScreenViewModel.cardFieldsEnabled(true)
        }

        is Status.Loading -> {
            cardScreenViewModel.cardFieldsEnabled(false)
        }

        is Status.Success -> {
            val encryptedDataFields =
                ((encryptedPaymentRequestStatus as Status.Success).data as PreparedPaymentRequest).encryptedFields

            updateValuesOnSuccessStatus(cardScreenViewModel, paymentCardViewModel)

            keyboardController?.hide()
            navController.navigate("${PaymentScreen.RESULT.route}/$encryptedDataFields") {
                popUpTo(PaymentScreen.CONFIGURATION.route)
            }
        }

        is Status.Failed -> {
            // Show generic error
        }

        is Status.None -> {
            // Init status; nothing to do here
        }
    }

    val paymentProductFieldsUiState by paymentCardViewModel.paymentProductFieldsUIState.observeAsState(
        PaymentCardUIState.Loading
    )

    if (paymentProductFieldsUiState is PaymentCardUIState.Success) {
        val productId = paymentSharedViewModel.selectedPaymentProductId ?: return
        updateValuesOnSuccessUIState(
            paymentProductFieldsUiState,
            productId,
            cardScreenViewModel,
            paymentCardViewModel,
            cardFields,
            LocalContext.current
        )
    }

    CardContent(
        uiState = paymentProductFieldsUiState,
        cardFields = cardFields,
        isFormValid = formValidationResult is FormValidationResult.Valid,
        isFormSubmitted = encryptedPaymentRequestStatus is Status.Loading,
        onPrimaryButtonClicked = { paymentCardViewModel.onPayClicked() },
        showBottomSheet = { showBottomSheet(it) },
        issuerIdentificationNumberChanged = { issuerIdentificationNumber ->
            paymentCardViewModel.getIINDetails(issuerIdentificationNumber)
        },
        onValueChanged = { paymentProductField, value ->
            paymentCardViewModel.fieldChanged(paymentProductField, value)
        },
        rememberCardValue = { paymentCardViewModel.saveCardForLater(it) }
    )
}

private fun analyzeFormValidationResult(
    formValidationResult: FormValidationResult,
    cardScreenViewModel: CardScreenViewModel
) {
    when (formValidationResult) {
        is FormValidationResult.InvalidWithValidationErrorMessages -> {
            cardScreenViewModel.setFieldErrors(
                (formValidationResult).errorMessages
            )
        }

        is FormValidationResult.Valid -> {
            cardScreenViewModel.setFieldErrors(emptyList())
        }

        is FormValidationResult.NotValidated -> {
            cardScreenViewModel.setFieldErrors(emptyList())
        }

        is FormValidationResult.Invalid -> {
            // No option, when form is invalid it will always come in the
            // FormValidationResult.InvalidWithValidationErrorMessage case
        }
    }
}

private fun updateValuesOnSuccessStatus(
    cardScreenViewModel: CardScreenViewModel,
    paymentCardViewModel: PaymentCardViewModel
) {
    cardScreenViewModel.cardFieldsEnabled(true)

    paymentCardViewModel.formValidationResult.value = FormValidationResult.NotValidated
    paymentCardViewModel.encryptedPaymentRequestStatus.value = Status.None
    paymentCardViewModel.paymentProductFieldsUIState.value = PaymentCardUIState.None
}

private fun updateValuesOnSuccessUIState(
    paymentCardUiState: PaymentCardUIState,
    paymentProductId: String,
    cardScreenViewModel: CardScreenViewModel,
    paymentCardViewModel: PaymentCardViewModel,
    cardFields: CardFields,
    context: Context
) {
    cardFields.cardNumberField.isLoading = false

    val paymentFields = (paymentCardUiState as PaymentCardUIState.Success)

    cardScreenViewModel.updateFields(
        paymentProductId = paymentProductId,
        paymentProductFields = paymentFields.paymentFields,
        logoUrl = paymentFields.logoUrl,
        accountOnFile = paymentFields.accountOnFile,
        context = context
    )

    cardFields.cardNumberField.paymentProductField?.let { paymentProductField ->
        paymentCardViewModel.updateValueInPaymentRequest(
            paymentProductField,
            cardFields.cardNumberField.text
        )
    }
    cardFields.expiryDateField.paymentProductField?.let { paymentProductField ->
        paymentCardViewModel.updateValueInPaymentRequest(
            paymentProductField,
            cardFields.expiryDateField.text
        )
    }
    cardFields.securityNumberField.paymentProductField?.let { paymentProductField ->
        paymentCardViewModel.updateValueInPaymentRequest(
            paymentProductField,
            cardFields.securityNumberField.text
        )
    }
    cardFields.cardHolderField.paymentProductField?.let { paymentProductField ->
        paymentCardViewModel.updateValueInPaymentRequest(
            paymentProductField,
            cardFields.cardHolderField.text
        )
    }

    paymentCardViewModel.shouldEnablePayButton()
}

@Composable
fun CardContent(
    uiState: PaymentCardUIState,
    cardFields: CardFields,
    isFormValid: Boolean,
    isFormSubmitted: Boolean,
    onPrimaryButtonClicked: () -> Unit,
    showBottomSheet: (BottomSheetContent) -> Unit,
    issuerIdentificationNumberChanged: (String) -> Unit,
    onValueChanged: (PaymentProductField, String) -> Unit,
    rememberCardValue: (Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is PaymentCardUIState.ApiError -> {
                FailedText()
            }

            is PaymentCardUIState.IinFailed -> {
                IinFailedStateContent(
                    uiState = uiState,
                    cardFields = cardFields,
                    isFormValid = isFormValid,
                    isFormSubmitted = isFormSubmitted,
                    onPrimaryButtonClicked = { onPrimaryButtonClicked() },
                    showBottomSheet = { showBottomSheet(it) },
                    issuerIdentificationNumberChanged = { issuerIdentificationNumberChanged(it) },
                    onValueChanged = { paymentProductField, value ->
                        onValueChanged(
                            paymentProductField,
                            value
                        )
                    },
                    rememberCardValue = { rememberCardValue(it) }
                )
            }

            is PaymentCardUIState.Loading -> {
                ProgressIndicator()
            }

            is PaymentCardUIState.Success -> {
                CardItems(
                    cardFields = cardFields,
                    isFormValid = isFormValid,
                    isFormSubmitted = isFormSubmitted,
                    onPrimaryButtonClicked = { onPrimaryButtonClicked() },
                    showBottomSheet = { showBottomSheet(it) },
                    issuerIdentificationNumberChanged = { issuerIdentificationNumberChanged(it) },
                    onValueChanged = { paymentProductField, value ->
                        onValueChanged(
                            paymentProductField,
                            value
                        )
                    },
                    rememberCardValue = { rememberCardValue(it) }
                )
            }

            is PaymentCardUIState.None -> {
                // Init status; nothing to do here
            }

            is PaymentCardUIState.Failed -> {
                // Show generic error
            }
        }
    }
}

@Composable
private fun IinFailedStateContent(
    uiState: PaymentCardUIState.IinFailed,
    cardFields: CardFields,
    isFormValid: Boolean,
    isFormSubmitted: Boolean,
    onPrimaryButtonClicked: () -> Unit,
    showBottomSheet: (BottomSheetContent) -> Unit,
    issuerIdentificationNumberChanged: (String) -> Unit,
    onValueChanged: (PaymentProductField, String) -> Unit,
    rememberCardValue: (Boolean) -> Unit
) {
    when (uiState.throwable.message) {
        IinStatus.UNKNOWN.name -> {
            cardFields.cardNumberField.networkErrorMessage =
                StringProvider.retrieveString(
                    "validationErrors_iin_label",
                    LocalContext.current
                )
        }

        IinStatus.EXISTING_BUT_NOT_ALLOWED.name -> {
            cardFields.cardNumberField.networkErrorMessage =
                StringProvider.retrieveString(
                    "validationErrors_allowedInContext_label",
                    LocalContext.current
                )
        }
    }
    cardFields.cardNumberField.isLoading = false
    CardItems(
        cardFields = cardFields,
        isFormValid = isFormValid,
        isFormSubmitted = isFormSubmitted,
        onPrimaryButtonClicked = { onPrimaryButtonClicked() },
        showBottomSheet = { showBottomSheet(it) },
        issuerIdentificationNumberChanged = { issuerIdentificationNumberChanged(it) },
        onValueChanged = { paymentProductField, value ->
            onValueChanged(
                paymentProductField,
                value
            )
        },
        rememberCardValue = { rememberCardValue(it) }
    )
}

@Composable
fun CardItems(
    cardFields: CardFields,
    isFormValid: Boolean,
    isFormSubmitted: Boolean,
    onPrimaryButtonClicked: () -> Unit,
    showBottomSheet: (BottomSheetContent) -> Unit,
    issuerIdentificationNumberChanged: (String) -> Unit,
    onValueChanged: (PaymentProductField, String) -> Unit,
    rememberCardValue: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        CardNumberField(
            cardNumberTextFieldState = cardFields.cardNumberField,
            issuerIdentificationNumberChanged = { issuerIdentificationNumberChanged(it) },
            onValueChanged = { paymentProductField, value ->
                onValueChanged(
                    paymentProductField,
                    value
                )
            })
        ExpiryDateSecurityNumberRow(cardFields, showBottomSheet, onValueChanged)
        CardTextField(
            cardTextFieldState = cardFields.cardHolderField,
            onValueChanged = { paymentProductField, value ->
                onValueChanged(
                    paymentProductField,
                    value
                )
            }
        )
        LabelledCheckbox(
            checkBoxField = cardFields.rememberCardField,
            onCheckedChange = { rememberCardValue(it) })
        PrimaryButton(
            onClick = { onPrimaryButtonClicked() },
            text = StringProvider.retrieveString(
                "paymentProductDetails_payButton",
                LocalContext.current
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid,
            showLoadingStatus = isFormSubmitted
        )
    }
}

@Composable
private fun ExpiryDateSecurityNumberRow(
    cardFields: CardFields,
    showBottomSheet: (BottomSheetContent) -> Unit,
    onValueChanged: (PaymentProductField, String) -> Unit
) {
    Row {
        Column(modifier = Modifier.weight(2f)) {
            CardTextField(
                cardTextFieldState = cardFields.expiryDateField,
                onValueChanged = { paymentProductField, value ->
                    onValueChanged(
                        paymentProductField,
                        value
                    )
                }
            )
        }
        Column(
            modifier = Modifier
                .weight(2f)
                .padding(start = 8.dp)
        ) {
            CardTextField(
                cardTextFieldState = cardFields.securityNumberField,
                onTrailingIconClicked = {
                    showBottomSheet(
                        BottomSheetContent(
                            text = cardFields.securityNumberField.tooltipText ?: "",
                            imageUrl = cardFields.securityNumberField.tooltipImageUrl
                        )
                    )
                },
                onValueChanged = { paymentProductField, value ->
                    onValueChanged(
                        paymentProductField,
                        value
                    )
                }
            )
        }
    }
}

@Preview
@Composable
fun CardScreenPreview() {
    CardItems(
        cardFields = CardFields(),
        isFormValid = false,
        isFormSubmitted = false,
        onPrimaryButtonClicked = {},
        showBottomSheet = {},
        issuerIdentificationNumberChanged = {},
        onValueChanged = { _, _ -> },
        rememberCardValue = {}
    )
}
