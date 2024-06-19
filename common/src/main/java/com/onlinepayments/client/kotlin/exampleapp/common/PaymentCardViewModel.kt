/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.common

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants
import com.onlinepayments.client.kotlin.exampleapp.common.utils.FormValidationResult
import com.onlinepayments.client.kotlin.exampleapp.common.utils.PaymentCardUIState
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Status
import com.onlinepayments.sdk.client.android.exception.EncryptDataException
import com.onlinepayments.sdk.client.android.listener.IinLookupResponseListener
import com.onlinepayments.sdk.client.android.listener.PaymentProductResponseListener
import com.onlinepayments.sdk.client.android.listener.PaymentRequestPreparedListener
import com.onlinepayments.sdk.client.android.model.PaymentContext
import com.onlinepayments.sdk.client.android.model.PaymentRequest
import com.onlinepayments.sdk.client.android.model.PreparedPaymentRequest
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsResponse
import com.onlinepayments.sdk.client.android.model.iin.IinStatus
import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFile
import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFileAttribute
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProduct
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField
import com.onlinepayments.sdk.client.android.session.Session

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
            var allRequiredFieldsNotEmpty = true
            if (
                paymentRequest.paymentProduct != null &&
                paymentProductFieldsUIState.value is PaymentCardUIState.Success
            ) {
                paymentRequest.values.forEach { paymentRequestValue ->
                    if (paymentRequestValue.value.isNullOrBlank() &&
                        paymentRequest.paymentProduct.getPaymentProductFieldById(
                            paymentRequestValue.key
                        ).dataRestrictions.isRequired
                    ) {
                        allRequiredFieldsNotEmpty = false
                        return@forEach
                    }
                }
            } else {
                allRequiredFieldsNotEmpty = false
            }

            return allRequiredFieldsNotEmpty
        }

    /**
     * Based on the selected payment product,
     * the correct data is retrieved from the SDK and parameters are set correctly.
     */
    fun getPaymentProduct(
        selectedPaymentProduct: Any?,
    ) {
        when (selectedPaymentProduct) {
            is BasicPaymentProduct -> {
                getPaymentProduct(
                    selectedPaymentProduct.id,
                    null,
                    true
                )
            }
            is AccountOnFile -> {
                getPaymentProduct(
                    selectedPaymentProduct.paymentProductId,
                    selectedPaymentProduct.id.toString(),
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
        val iinLookupResponseListener = object : IinLookupResponseListener {
            override fun onSuccess(response: IinDetailsResponse) {
                setUIStateBasedOnStatus(response)
            }

            override fun onApiError(error: ErrorResponse) {
                paymentProductFieldsUIState.postValue(PaymentCardUIState.IinFailed(Exception(IinStatus.UNKNOWN.name)))
            }

            override fun onException(t: Throwable) {
                paymentProductFieldsUIState.postValue(PaymentCardUIState.Failed(t))
            }
        }

        session.getIinDetails(
            getApplication<Application>().applicationContext,
            issuerIdentificationNumber,
            iinLookupResponseListener,
            paymentContext
            )
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
            IinStatus.SUPPORTED ->
                getPaymentProduct(
                    iinDetailsResponse.paymentProductId, null, false
                )
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
    fun getPaymentProduct(
        paymentProductId: String,
        accountOnFileId: String?,
        showLoadingIndicator: Boolean
    ) {
        if (showLoadingIndicator) {
            paymentProductFieldsUIState.postValue(PaymentCardUIState.Loading)
        }

        val paymentProductResponseListener = object : PaymentProductResponseListener {
            override fun onSuccess(response: PaymentProduct) {
                paymentProductFieldsUIState.postValue(
                    PaymentCardUIState.Success(
                        response.paymentProductFields,
                        response.displayHintsList[0].logoUrl,
                        accountOnFileId?.let { response.getAccountOnFileById(it) }
                    )
                )

                paymentRequest.paymentProduct = response
                if (!accountOnFileId.isNullOrBlank() && response.getAccountOnFileById(
                        accountOnFileId) != null
                ) {
                    paymentRequest.accountOnFile =
                        response.getAccountOnFileById(accountOnFileId)
                }

                if (liveFormValidating) validateAllFields()
            }

            override fun onApiError(error: ErrorResponse) {
                paymentProductFieldsUIState.postValue(
                    PaymentCardUIState.ApiError(error)
                )
            }

            override fun onException(t: Throwable) {
                paymentProductFieldsUIState.postValue(PaymentCardUIState.Failed(t))
            }

        }

        session.getPaymentProduct(
            getApplication<Application>().applicationContext,
            paymentProductId,
            paymentContext,
            paymentProductResponseListener
        )
    }

    /**
     * When all fields are filled in this function validates all fields and when valid a payment is encrypted.
     * Returns a preparedPaymentRequest in which all data of the fields is stored and merged into one encrypted key.
     */
    fun onPayClicked() {
        liveFormValidating = true

        if (validateAllFields()) {
            val paymentRequestPreparedListener = object : PaymentRequestPreparedListener {
                override fun onPaymentRequestPrepared(preparedPaymentRequest: PreparedPaymentRequest) {
                    encryptedPaymentRequestStatus.postValue(Status.Success(preparedPaymentRequest))
                }

                override fun onFailure(e: EncryptDataException) {
                    encryptedPaymentRequestStatus.postValue(Status.Failed(e))
                }
            }

            session.preparePaymentRequest(
                paymentRequest,
                getApplication<Application>().applicationContext,
                paymentRequestPreparedListener
            )
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

        if (paymentRequest.accountOnFile != null) {
            // Only fields that are allowed to be edited &
            // which do not have the unmasked value that was originally in the AOF,
            // should be added to the paymentRequest
            // CardNumber should NEVER be added to the paymentRequest in case of an AOF
            for (aofAttribute in paymentRequest.accountOnFile.accountOnFileAttributes) {
                if(shouldBeAddedToPaymentRequest(paymentProductField, aofAttribute, value)) {
                    paymentRequest.setValue(paymentProductFieldId, value)
                } else if (aofAttribute.value == paymentProductField.removeMask(value)) {
                    // This else-if is to ensure that when users adjust a field and then
                    // discard changes back to the original AOF value, it is not added to the paymentRequest
                    paymentRequest.removeValue(paymentProductFieldId)
                }
            }
        } else {
            val unmaskedValue = paymentProductField.removeMask(value)
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
        return if (validationErrors.isNullOrEmpty()) {
            formValidationResult.postValue(FormValidationResult.Valid)
            true
        } else {
            formValidationResult.postValue(FormValidationResult.InvalidWithValidationErrorMessages(validationErrors))
            false
        }
    }
}
