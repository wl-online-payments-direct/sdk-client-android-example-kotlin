/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.common.utils

import com.onlinepayments.sdk.client.android.model.api.ErrorResponse
import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFile
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField

sealed class PaymentCardUIState {
    data object Loading : PaymentCardUIState()
    data object None: PaymentCardUIState()
    class ApiError(val apiError: ErrorResponse) : PaymentCardUIState()
    class Failed(val throwable: Throwable) : PaymentCardUIState()
    class IinFailed(val throwable: Throwable) : PaymentCardUIState()
    class Success(
        val paymentFields: List<PaymentProductField>,
        val logoUrl: String?,
        val accountOnFile: AccountOnFile?
    ) : PaymentCardUIState()
}
