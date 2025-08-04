/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.xml.card

import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField

/**
 * Interface that listens for specific text changes.
 * This interface must be implemented in an activity/fragment so that you can validate fields and process iin changes.
 */
interface CardFieldAfterTextChangedListener {
    fun afterTextChanged(paymentProductField: PaymentProductField, value: String)
    fun issuerIdentificationNumberChanged(currentCardNumber: String)
    fun onToolTipClicked(paymentProductField: PaymentProductField)
}
