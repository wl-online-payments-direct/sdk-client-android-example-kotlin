/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.compose.card

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants.CARD_NUMBER
import com.onlinepayments.client.kotlin.exampleapp.common.utils.StringProvider
import com.onlinepayments.client.kotlin.exampleapp.common.utils.ValidationErrorMessageMapper
import com.onlinepayments.client.kotlin.exampleapp.compose.components.TextFieldState
import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFile
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField
import com.onlinepayments.sdk.client.android.model.validation.ValidationErrorMessage
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleLength

class CardScreenViewModel(application: Application) : AndroidViewModel(application) {

    var cardFields by mutableStateOf(CardFields())
        private set

    private var isAccountOnFileDataLoaded = false

    /**
     * When the payment product fields properties change.
     * Update the card fields with the new properties values.
     */
    fun updateFields(
        paymentProductId: String,
        paymentProductFields: List<PaymentProductField>,
        logoUrl: String?,
        accountOnFile: AccountOnFile?,
        context: Context
    ) {
        paymentProductFields.forEach { paymentProductField ->
            var validationRuleLength: ValidationRuleLength? = null

            for (validationRule in paymentProductField.dataRestrictions.validationRules) {
                validationRuleLength = validationRule as? ValidationRuleLength
            }

            when (paymentProductField.id) {
                cardFields.cardNumberField.id -> {
                    updateCardNumberField(
                        logoUrl,
                        accountOnFile,
                        paymentProductId,
                        paymentProductField,
                        validationRuleLength,
                        context
                    )
                }

                cardFields.expiryDateField.id -> {
                    updateExpiryDateField(
                        accountOnFile,
                        paymentProductId,
                        paymentProductField,
                        validationRuleLength,
                        context
                    )
                }

                cardFields.securityNumberField.id -> {
                    updateSecurityNumberField(
                        accountOnFile,
                        paymentProductId,
                        paymentProductField,
                        validationRuleLength,
                        context
                    )
                }

                cardFields.cardHolderField.id -> {
                    updateCardHolderField(accountOnFile, paymentProductId, paymentProductField, context)
                }
            }
        }

        if (accountOnFile != null && !isAccountOnFileDataLoaded) isAccountOnFileDataLoaded = true
    }

    private fun updateCardNumberField(
        logoUrl: String?,
        accountOnFile: AccountOnFile?,
        paymentProductId: String,
        paymentProductField: PaymentProductField,
        validationRuleLength: ValidationRuleLength?,
        context: Context
    ) {
        cardFields.cardNumberField.apply {
            label = StringProvider.getPaymentProductFieldPlaceholderText(
                paymentProductId,
                paymentProductField.id,
                context
            )
            mask = paymentProductField.displayHints.mask
            maxSize = validationRuleLength?.maxLength ?: Int.MAX_VALUE
            trailingImageUrl = logoUrl
            this.paymentProductField = paymentProductField
        }

        accountOnFile?.let {
            accountOnFileAttributes(
                accountOnFile,
                paymentProductField.id,
                cardFields.cardNumberField
            )
        }
    }

    private fun updateExpiryDateField(
        accountOnFile: AccountOnFile?,
        paymentProductId: String,
        paymentProductField: PaymentProductField,
        validationRuleLength: ValidationRuleLength?,
        context: Context
    ) {
        cardFields.expiryDateField.apply {
            label = StringProvider.getPaymentProductFieldPlaceholderText(
                paymentProductId,
                paymentProductField.id,
                context
            )
            mask = paymentProductField.displayHints.mask
            maxSize = validationRuleLength?.maxLength ?: Int.MAX_VALUE
            this.paymentProductField = paymentProductField
        }

        if (accountOnFile != null && !isAccountOnFileDataLoaded) {
            accountOnFileAttributes(
                accountOnFile,
                paymentProductField.id,
                cardFields.expiryDateField
            )
        }
    }

    private fun updateSecurityNumberField(
        accountOnFile: AccountOnFile?,
        paymentProductId: String,
        paymentProductField: PaymentProductField,
        validationRuleLength: ValidationRuleLength?,
        context: Context
    ) {
        cardFields.securityNumberField.apply {
            label = StringProvider.getPaymentProductFieldPlaceholderText(
                paymentProductId,
                paymentProductField.id,
                context
            )
            mask = paymentProductField.displayHints.mask
            maxSize = validationRuleLength?.maxLength ?: Int.MAX_VALUE
            tooltipImageUrl = paymentProductField.displayHints.tooltip.imageURL
            tooltipText = paymentProductField.displayHints.tooltip.label
            this.paymentProductField = paymentProductField
        }

        if (accountOnFile != null && !isAccountOnFileDataLoaded) {
            accountOnFileAttributes(
                accountOnFile,
                paymentProductField.id,
                cardFields.securityNumberField
            )
        }
    }

    private fun updateCardHolderField(
        accountOnFile: AccountOnFile?,
        paymentProductId: String,
        paymentProductField: PaymentProductField,
        context: Context
    ) {
        cardFields.cardHolderField.apply {
            label = StringProvider.getPaymentProductFieldPlaceholderText(
                paymentProductId,
                paymentProductField.id,
                context
            )
            this.paymentProductField = paymentProductField
        }
        if (accountOnFile != null && !isAccountOnFileDataLoaded) {
            accountOnFileAttributes(
                accountOnFile,
                paymentProductField.id,
                cardFields.cardHolderField
            )
        }
    }

    /**
     * Update field errors after modification in one of the fields.
     */
    fun setFieldErrors(fieldErrors: List<ValidationErrorMessage>) { cardFields.cardNumberField.networkErrorMessage = ""
        cardFields.expiryDateField.networkErrorMessage = ""
        cardFields.securityNumberField.networkErrorMessage = ""
        cardFields.cardHolderField.networkErrorMessage = ""
        fieldErrors.forEach { validationErrorMessage ->
            val errorMessage =
                ValidationErrorMessageMapper.mapValidationErrorMessageToString(
                    getApplication<Application>().applicationContext,
                    validationErrorMessage
                )
            when (validationErrorMessage.paymentProductFieldId) {
                cardFields.cardNumberField.id -> {
                    cardFields.cardNumberField.networkErrorMessage = errorMessage
                }
                cardFields.expiryDateField.id -> {
                    cardFields.expiryDateField.networkErrorMessage = errorMessage
                }
                cardFields.securityNumberField.id -> {
                    cardFields.securityNumberField.networkErrorMessage = errorMessage
                }
                cardFields.cardHolderField.id -> {
                    cardFields.cardHolderField.networkErrorMessage = errorMessage
                }
            }
        }
    }

    /**
     * Disable/Enable all fields
     */
    fun cardFieldsEnabled(enabled: Boolean) {
        cardFields.apply {
            cardNumberField.enabled = enabled
            expiryDateField.enabled = enabled
            securityNumberField.enabled = enabled
            cardHolderField.enabled = enabled
        }
    }

    private fun accountOnFileAttributes(
        accountOnFile: AccountOnFile,
        paymentProductFieldId: String,
        textFieldState: TextFieldState
    ) {
        cardFields.rememberCardField.visible.value = false
        accountOnFile.accountOnFileAttributes.firstOrNull { it.key == paymentProductFieldId }
            ?.let { attribute ->
                textFieldState.text = if (paymentProductFieldId == cardFields.cardNumberField.id) {
                    accountOnFile.displayHints.labelTemplate[0].mask?.let { mask ->
                        cardFields.cardNumberField.mask =
                            mask.replace(
                                "9",
                                "*"
                            )
                    }
                    accountOnFile.label
                } else {
                    attribute.value
                }

                // CARD_NUMBER field should always be disabled for AccountOnFile) {
                if (!attribute.isEditingAllowed() || attribute.key == CARD_NUMBER) {
                    textFieldState.enabled = false
                }
            }
    }
}
