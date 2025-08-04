/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.client.kotlin.exampleapp.common.googlepay

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Status
import com.onlinepayments.sdk.client.android.exception.ApiException
import com.onlinepayments.sdk.client.android.exception.EncryptDataException
import com.onlinepayments.sdk.client.android.model.PaymentContext
import com.onlinepayments.sdk.client.android.model.PaymentRequest
import com.onlinepayments.sdk.client.android.session.Session
import kotlinx.coroutines.launch

/**
 * ViewModel for retrieving Google Pay payment product and preparing Google Pay payment request
 */
class GooglePayPaymentViewModel(application: Application) : AndroidViewModel(application) {
    private var session: Session? = null
    val paymentProductStatus = MutableLiveData<Status>()
    val encryptedPaymentRequestStatus = MutableLiveData<Status>()
    val paymentRequest = PaymentRequest()

    fun setSession(session: Session) {
        this.session = session
    }

    fun getGooglePayPaymentProductDetails(paymentContext: PaymentContext) {
        viewModelScope.launch {
            try {
                val paymentProduct = session!!.getPaymentProduct(Constants.GOOGLE_PAY_PRODUCT_ID, paymentContext)
                paymentRequest.paymentProduct = paymentProduct
                paymentProductStatus.postValue(Status.Success(paymentProduct))
            } catch (e: ApiException) {
                val error = e.errorResponse
                if (error != null) {
                    paymentProductStatus.postValue(Status.ApiError(error))
                } else {
                    paymentProductStatus.postValue(Status.Failed(Exception("Unknown API error")))
                }
            } catch (e: Throwable) {
                paymentProductStatus.postValue(Status.Failed(e))
            }
        }
    }

    fun encryptGooglePayPayment() {
        viewModelScope.launch {
            try {
                val preparedPaymentRequest = session?.preparePaymentRequest(paymentRequest)
                encryptedPaymentRequestStatus.postValue(Status.Success(preparedPaymentRequest))
            } catch (e: EncryptDataException) {
                encryptedPaymentRequestStatus.postValue(Status.Failed(e))
            } catch (e: Exception) {
                encryptedPaymentRequestStatus.postValue(Status.Failed(e))
            }
        }
    }
}
