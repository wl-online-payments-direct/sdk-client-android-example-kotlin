/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.common.googlepay

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Status
import com.onlinepayments.sdk.client.android.exception.EncryptDataException
import com.onlinepayments.sdk.client.android.listener.PaymentProductResponseListener
import com.onlinepayments.sdk.client.android.listener.PaymentRequestPreparedListener
import com.onlinepayments.sdk.client.android.model.PaymentContext
import com.onlinepayments.sdk.client.android.model.PaymentRequest
import com.onlinepayments.sdk.client.android.model.PreparedPaymentRequest
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct
import com.onlinepayments.sdk.client.android.session.Session

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
        val paymentProductResponseListener = object : PaymentProductResponseListener {
            override fun onSuccess(response: PaymentProduct) {
                paymentRequest.paymentProduct = response
                paymentProductStatus.postValue(Status.Success(response))
            }

            override fun onApiError(error: ErrorResponse) {
                paymentProductStatus.postValue(Status.ApiError(error))
            }

            override fun onException(t: Throwable) {
                paymentProductStatus.postValue(Status.Failed(t))
            }

        }

        session?.getPaymentProduct(
            getApplication<Application>().applicationContext,
            Constants.GOOGLE_PAY_PRODUCT_ID,
            paymentContext,
            paymentProductResponseListener
        )
    }

    fun encryptGooglePayPayment() {
        val paymentRequestPreparedListener = object : PaymentRequestPreparedListener {
            override fun onPaymentRequestPrepared(preparedPaymentRequest: PreparedPaymentRequest) {
                encryptedPaymentRequestStatus.postValue(Status.Success(preparedPaymentRequest))
            }

            override fun onFailure(e: EncryptDataException) {
                encryptedPaymentRequestStatus.postValue(Status.Failed(e))
            }
        }

        session?.preparePaymentRequest(
            paymentRequest,
            getApplication<Application>().applicationContext,
            paymentRequestPreparedListener
        )
    }
}
