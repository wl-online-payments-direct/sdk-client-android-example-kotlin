/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.xml.card

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField

/**
 * TextWatcher that applies the masking to a cardField when necessary.
 * examples of masking are:
 * cardNumber: 1234 1234 1234 1234 {{9999 9999 9999 9999}}
 * expiryDate: 11/11 {{99/99}}
 */
class PaymentCardMaskTextWatcher(
    private val editText: EditText,
    private val paymentProductField: PaymentProductField
) : TextWatcher {
    private var oldValue: String? = null
    private var start = 0
    private var count = 0
    private var after = 0
    private var isRunning = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        oldValue = s.toString()
        this.start = start
        this.count = count
        this.after = after
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (isRunning) {
            return
        }

        isRunning = true
        val formatResult =
            paymentProductField.applyMask(s.toString(), oldValue, start, count, after)
        formatResult?.let {
            editText.setText(it.formattedResult)
            editText.setSelection(it.cursorIndex ?: 0)
        }
        isRunning = false
    }
}
