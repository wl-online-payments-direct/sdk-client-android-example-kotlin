/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.client.kotlin.exampleapp.common

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants
import com.onlinepayments.client.kotlin.exampleapp.common.utils.FormValidationResult
import com.onlinepayments.client.kotlin.exampleapp.common.utils.PaymentCardUIState
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Status
import com.onlinepayments.sdk.client.android.exception.ApiException
import com.onlinepayments.sdk.client.android.exception.EncryptDataException
import com.onlinepayments.sdk.client.android.model.PaymentContext
import com.onlinepayments.sdk.client.android.model.PaymentRequest
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsResponse
import com.onlinepayments.sdk.client.android.model.iin.IinStatus
import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFile
import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFileAttribute
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProduct
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField
import com.onlinepayments.sdk.client.android.session.Session
import kotlinx.coroutines.launch

/**
 * ViewModel for retrieving payment products, also validates all card fields
 */
class PaymentCardViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var session: Session
    lateinit var paymentContext: PaymentContext

    val encryptedPaymentRequestStatus = MutableLiveData<Status>()
    private val paymentRequest = PaymentRequest()

    val paymentProductFieldsUIState = MutableLiveData<PaymentCardUIState>(PaymentCardUIState.None)

    // When property 'liveFormValidating' is true, card fields are validated after each input change
    private var liveFormValidating = false
    val formValidationResult = MutableLiveData<FormValidationResult>()

    private val hasNoEmptyRequiredFields: Boolean
        get() {
            val product = paymentRequest.paymentProduct

            if (paymentProductFieldsUIState.value !is PaymentCardUIState.Success) {
                return false
            }

            paymentRequest.getValues().forEach { (fieldId, fieldValue) ->
                if (fieldValue.isBlank()) {
                    val productField = product?.getPaymentProductFieldById(fieldId)

                    if (productField?.dataRestrictions?.isRequired() == true) {
                        return false
                    }
                }
            }

            return true
        }

    /**
     * Based on the selected payment product,
     * the correct data is retrieved from the SDK and parameters are set correctly.
     */
    fun getPaymentProduct(selectedPaymentProduct: Any?) {
        when (selectedPaymentProduct) {
            is BasicPaymentProduct -> {
                getPaymentProduct(
                    selectedPaymentProduct.getId() ?: return,
                    null,
                    true
                )
            }

            is AccountOnFile -> {
                getPaymentProduct(
                    selectedPaymentProduct.paymentProductId,
                    selectedPaymentProduct.id,
                    true
                )
            }
        }
    }

    /**
     * After at least 6 digits have been entered(issuerIdentificationNumber) in the card number field,
     * the corresponding card type is searched.
     * The returned 'paymentProductId' can be used to provide visual feedback to the user
     * by showing the appropriate payment product logo and specific card type details.
     */
    fun getIINDetails(issuerIdentificationNumber: String) {
        viewModelScope.launch {
            try {
                val response = session.getIinDetails(issuerIdentificationNumber, paymentContext)
                setUIStateBasedOnStatus(response)
            } catch (_: ApiException) {
                paymentProductFieldsUIState.postValue(
                    PaymentCardUIState.IinFailed(Exception(IinStatus.UNKNOWN.name))
                )
            } catch (e: Throwable) {
                paymentProductFieldsUIState.postValue(
                    PaymentCardUIState.Failed(e)
                )
            }
        }
    }

    private fun setUIStateBasedOnStatus(iinDetailsResponse: IinDetailsResponse) {
        when (iinDetailsResponse.status) {
            IinStatus.UNKNOWN ->
                paymentProductFieldsUIState.postValue(
                    PaymentCardUIState.IinFailed(Exception(IinStatus.UNKNOWN.name))
                )

            IinStatus.NOT_ENOUGH_DIGITS ->
                paymentProductFieldsUIState.postValue(
                    PaymentCardUIState.IinFailed(Exception(IinStatus.NOT_ENOUGH_DIGITS.name))
                )

            IinStatus.EXISTING_BUT_NOT_ALLOWED ->
                paymentProductFieldsUIState.postValue(
                    PaymentCardUIState.IinFailed(Exception(IinStatus.EXISTING_BUT_NOT_ALLOWED.name))
                )

            IinStatus.SUPPORTED -> {
                val paymentProductId = iinDetailsResponse.paymentProductId
                if (!paymentProductId.isNullOrBlank()) {
                    getPaymentProduct(paymentProductId, null, false)
                } else {
                    paymentProductFieldsUIState.postValue(
                        PaymentCardUIState.IinFailed(Exception("Payment product ID is missing"))
                    )
                }
            }

            else ->
                paymentProductFieldsUIState.postValue(
                    PaymentCardUIState.IinFailed(Exception(IinStatus.UNKNOWN.name))
                )
        }
    }

    /**
     * Returns all details of a specific payment product. Such as a logo, hints, masking and validation rules.
     *
     * You can use this method if you have first retrieved a payment productId with the 'getIINDetails' function
     * or if you have already selected a specific payment product.
     */
    private fun getPaymentProduct(
        paymentProductId: String,
        accountOnFileId: String?,
        showLoadingIndicator: Boolean
    ) {
        if (showLoadingIndicator) {
            paymentProductFieldsUIState.postValue(PaymentCardUIState.Loading)
        }

        viewModelScope.launch {
            try {
                val paymentProduct = session.getPaymentProduct(paymentProductId, paymentContext)

                if (paymentProduct != null) {
                    val accountOnFile = accountOnFileId?.let {
                        paymentProduct.getAccountOnFileById(it)
                    }

                    paymentProductFieldsUIState.postValue(
                        PaymentCardUIState.Success(
                            paymentProduct.getPaymentProductFields(),
                            paymentProduct.getDisplayHintsList().firstOrNull()?.logoUrl.orEmpty(),
                            accountOnFile
                        )
                    )

                    paymentRequest.paymentProduct = paymentProduct
                    if (!accountOnFileId.isNullOrBlank() && accountOnFile != null) {
                        paymentRequest.accountOnFile = accountOnFile
                    }

                    if (liveFormValidating) {
                        validateAllFields()
                    }
                } else {
                    paymentProductFieldsUIState.postValue(
                        PaymentCardUIState.IinFailed(Exception("Payment product not found"))
                    )
                }
            } catch (e: ApiException) {
                val error = e.errorResponse
                if (error != null) {
                    paymentProductFieldsUIState.postValue(PaymentCardUIState.ApiError(error))
                } else {
                    paymentProductFieldsUIState.postValue(
                        PaymentCardUIState.Failed(Exception("Unknown API error"))
                    )
                }
            } catch (e: Throwable) {
                paymentProductFieldsUIState.postValue(
                    PaymentCardUIState.Failed(e)
                )
            }
        }
    }

    /**
     * When all fields are filled in this function validates all fields and when valid a payment is encrypted.
     * Returns a preparedPaymentRequest in which all data of the fields is stored and merged into one encrypted key.
     */
    fun onPayClicked() {
        liveFormValidating = true

        if (validateAllFields()) {
            viewModelScope.launch {
                try {
                    val preparedPaymentRequest = session.preparePaymentRequest(paymentRequest)
                    encryptedPaymentRequestStatus.postValue(Status.Success(preparedPaymentRequest))
                } catch (e: EncryptDataException) {
                    encryptedPaymentRequestStatus.postValue(Status.Failed(e))
                } catch (e: Exception) {
                    encryptedPaymentRequestStatus.postValue(Status.Failed(e))
                }
            }
        }
    }

    /**
     * When a field is changed, validate it
     */
    fun fieldChanged(paymentProductField: PaymentProductField, value: String) {
        updateValueInPaymentRequest(paymentProductField, value)
        if (liveFormValidating) {
            validateAllFields()
        } else {
            shouldEnablePayButton()
        }
    }

    /**
     * When opting for saving your card, the payment request needs to be tokenized
     */
    fun saveCardForLater(saveCard: Boolean) {
        paymentRequest.tokenize = saveCard
    }

    /**
     * When a field is changed, it must also be updated in the payment request object,
     * so that the correct input is used when validating or preparing a payment.
     */
    fun updateValueInPaymentRequest(paymentProductField: PaymentProductField, value: String) {
        val paymentProductFieldId = paymentProductField.id
        val accountOnFile = paymentRequest.accountOnFile
        if (accountOnFile != null) {
            // Only fields that are allowed to be edited &
            // which do not have the unmasked value that was originally in the AOF,
            // should be added to the paymentRequest
            // CardNumber should NEVER be added to the paymentRequest in case of an AOF
            for (aofAttribute in accountOnFile.attributes) {
                if (shouldBeAddedToPaymentRequest(paymentProductField, aofAttribute, value)) {
                    paymentRequest.setValue(paymentProductFieldId, value)
                } else if (aofAttribute.value == paymentProductField.removeMask(value)) {
                    // This else-if is to ensure that when users adjust a field and then
                    // discard changes back to the original AOF value, it is not added to the paymentRequest
                    paymentRequest.removeValue(paymentProductFieldId)
                }
            }
        } else {
            val unmaskedValue = paymentProductField.removeMask(value) ?: ""
            paymentRequest.setValue(paymentProductFieldId, unmaskedValue)
        }
    }

    private fun shouldBeAddedToPaymentRequest(
        paymentProductField: PaymentProductField,
        aofAttribute: AccountOnFileAttribute,
        value: String
    ): Boolean {
        return paymentProductField.id != Constants.CARD_NUMBER &&
            ((aofAttribute.key == paymentProductField.id && aofAttribute.isEditingAllowed()) ||
                paymentProductField.id == Constants.SECURITY_NUMBER) &&
            aofAttribute.value != paymentProductField.removeMask(value)
    }

    fun shouldEnablePayButton() {
        if (!liveFormValidating) {
            if (hasNoEmptyRequiredFields) {
                formValidationResult.value = FormValidationResult.Valid
            } else {
                formValidationResult.value = FormValidationResult.Invalid(null)
            }
        }
    }

    private fun validateAllFields(): Boolean {
        val validationErrors = paymentRequest.validate()
        return if (validationErrors.isEmpty()) {
            formValidationResult.postValue(FormValidationResult.Valid)
            true
        } else {
            formValidationResult.postValue(
                FormValidationResult.InvalidWithValidationErrorMessages(
                    validationErrors
                )
            )
            false
        }
    }
}
