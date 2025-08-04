/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.client.kotlin.exampleapp.xml.card

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.onlinepayments.client.kotlin.exampleapp.common.utils.StringProvider
import com.onlinepayments.client.kotlin.exampleapp.xml.R
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField
import com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsProductFields.PreferredInputType
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleLength
import com.squareup.picasso.Picasso

/**
 * View class for displaying a cardField.
 */
class PaymentCardField(context: Context, attributeSet: AttributeSet) :
    ConstraintLayout(context, attributeSet) {
    private var cardFieldAfterTextChangedListener: CardFieldAfterTextChangedListener? = null
    private var paymentCardMaskTextWatcher: PaymentCardMaskTextWatcher? = null
    private lateinit var paymentProductField: PaymentProductField

    private val cardField: ConstraintLayout
    private val cardFieldTextInputLayout: TextInputLayout
    private val cardFieldTextInputEditText: TextInputEditText
    private val cardFieldProgressBar: ProgressBar
    private val cardFieldImageView: ImageView
    private var lastCheckedCardValue = ""
    private var isIINListenerInitialized = false
    private var isAfterTextChangedListenerInitialized = false

    init {
        inflate(context, R.layout.view_payment_card_field, this)
        cardField = findViewById(R.id.paymentCardField)
        cardFieldTextInputLayout = findViewById(R.id.paymentCardFieldTextInputLayout)
        cardFieldTextInputEditText = findViewById(R.id.paymentCardFieldTextInputEditText)
        cardFieldProgressBar = findViewById(R.id.paymentCardFieldProgressBar)
        cardFieldImageView = findViewById(R.id.paymentCardFieldImageView)

        // Set attributes parameters from file
        context.theme.obtainStyledAttributes(attributeSet, R.styleable.PaymentCardField, 0, 0)
            .apply {
                try {
                    cardFieldTextInputLayout.startIconDrawable =
                        getDrawable(R.styleable.PaymentCardField_startIcon)
                } finally {
                    recycle()
                }
            }
    }

    /**
     * Init function for this field.
     * Essential for the proper functioning of this field.
     */
    fun setPaymentProductField(
        paymentProductId: String,
        paymentProductField: PaymentProductField,
        cardFieldAfterTextChangedListener: CardFieldAfterTextChangedListener
    ) {
        this.paymentProductField = paymentProductField
        this.cardFieldAfterTextChangedListener = cardFieldAfterTextChangedListener
        cardField.visibility = VISIBLE
        cardFieldTextInputEditText.hint =
            StringProvider.getPaymentProductFieldPlaceholderText(
                paymentProductId,
                paymentProductField.id,
                context
            )
        setEditTextInputType(
            this.paymentProductField.displayHints.preferredInputType
                ?: PreferredInputType.STRING_KEYBOARD
        )
        addTooltipIfAvailable()
        addMaskTextWatcherWhenAvailable()
        setAfterTextChangedListener()
        hideError()
        invalidate()
        requestLayout()
    }

    /**
     * Provides updates when iin changed.
     * Only necessary for the card number field.
     */
    fun setIssuerIdentificationNumberListener() {
        if (!isIINListenerInitialized) {
            isIINListenerInitialized = true

            cardFieldTextInputEditText.doAfterTextChanged { editable ->
                val currentCardNumber = editable.toString().replace(" ", "")
                val currentFormattedCardNumber = (currentCardNumber + "xxxxxxxx").take(8)

                if (currentCardNumber.length >= 6 && currentFormattedCardNumber != lastCheckedCardValue) {
                    cardFieldAfterTextChangedListener?.issuerIdentificationNumberChanged(
                        currentCardNumber
                    )
                }
                lastCheckedCardValue = currentFormattedCardNumber
            }
        }
    }

    fun setImage(imageURL: String) {
        cardFieldProgressBar.visibility = GONE
        Picasso.get()
            .load(imageURL)
            .into(cardFieldImageView)
        cardFieldImageView.visibility = VISIBLE
    }

    fun setError(errorText: String) {
        if (errorText != cardFieldTextInputLayout.error.toString()) {
            cardFieldImageView.visibility = GONE
            cardFieldProgressBar.visibility = GONE
            cardFieldTextInputLayout.isErrorEnabled = true
            cardFieldTextInputLayout.error = errorText
        }
    }

    fun hideError() {
        cardFieldTextInputLayout.isErrorEnabled = false
        cardFieldTextInputLayout.error = null
        cardFieldImageView.visibility = VISIBLE
    }

    fun hideAllToolTips() {
        cardFieldTextInputLayout.isErrorEnabled = false
        cardFieldTextInputLayout.error = null
        cardFieldProgressBar.visibility = GONE
        cardFieldImageView.visibility = GONE
    }

    fun getPaymentProductFieldValue(): String {
        return cardFieldTextInputEditText.text.toString()
    }

    fun setPaymentProductFieldValue(value: String) {
        cardFieldTextInputEditText.setText(value)
    }

    fun removePaymentCardMaskTextWatcher() {
        paymentCardMaskTextWatcher?.let {
            cardFieldTextInputEditText.removeTextChangedListener(it)
        }
        paymentCardMaskTextWatcher = null
    }

    private fun setEditTextInputType(preferredInputType: PreferredInputType) {
        val inputType = when (preferredInputType) {
            PreferredInputType.INTEGER_KEYBOARD -> InputType.TYPE_CLASS_NUMBER
            PreferredInputType.STRING_KEYBOARD -> InputType.TYPE_CLASS_TEXT
            PreferredInputType.PHONE_NUMBER_KEYBOARD -> InputType.TYPE_CLASS_PHONE
            PreferredInputType.EMAIL_ADDRESS_KEYBOARD -> InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            PreferredInputType.DATE_PICKER -> InputType.TYPE_DATETIME_VARIATION_DATE
            else -> InputType.TYPE_CLASS_TEXT
        }
        cardFieldTextInputEditText.inputType = inputType
    }

    private fun addTooltipIfAvailable() {
        if (paymentProductField.displayHints.tooltip != null) {
            cardFieldImageView.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_info
                )
            )
            cardFieldImageView.visibility = VISIBLE
            cardFieldImageView.setOnClickListener {
                cardFieldAfterTextChangedListener?.onToolTipClicked(paymentProductField)
            }
        }
    }

    /**
     * Add a mask to your field. If no mask is available, a maximum number of characters is set.
     */
    private fun addMaskTextWatcherWhenAvailable() {
        paymentCardMaskTextWatcher?.let {
            cardFieldTextInputEditText.removeTextChangedListener(it)
        }

        if (paymentProductField.displayHints.mask != null) {
            paymentCardMaskTextWatcher =
                PaymentCardMaskTextWatcher(cardFieldTextInputEditText, paymentProductField)
            cardFieldTextInputEditText.addTextChangedListener(paymentCardMaskTextWatcher)
            cardFieldTextInputEditText.setText(cardFieldTextInputEditText.text.toString())
        } else {
            for (validationRule in paymentProductField.dataRestrictions.getValidationRules()) {
                val validationRuleLength = validationRule as? ValidationRuleLength
                validationRuleLength?.let {
                    cardFieldTextInputEditText.filters += InputFilter.LengthFilter(
                        validationRuleLength.getMaxLength()
                    )
                }
            }
        }
    }

    /**
     * Listens for text changes. Is used to validate this field.
     */
    private fun setAfterTextChangedListener() {
        if (!isAfterTextChangedListenerInitialized) {
            isAfterTextChangedListenerInitialized = true
            cardFieldTextInputEditText.doAfterTextChanged { editable ->
                cardFieldAfterTextChangedListener?.afterTextChanged(
                    paymentProductField,
                    editable.toString()
                )
            }
        }
    }
}
