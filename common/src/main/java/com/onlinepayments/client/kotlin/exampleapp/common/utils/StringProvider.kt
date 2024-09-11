/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.common.utils

import android.content.Context

object StringProvider {
    // Prefixes for loading validation and tooltip messages
    private const val PAYMENT_PRODUCT_FIELD_PREFIX = "paymentProductFields_"
    private const val VALIDATION_ERROR_PREFIX =
        "validationErrors_"
    private const val PAYMENT_PRODUCT_PREFIX = "paymentProducts_"

    // Postfixes for loading validation and tooltip messages
    private const val LABEL_POSTFIX = "_label"
    private const val PRODUCT_FIELDS_INFIX = "_paymentProductFields_"
    private const val PLACEHOLDER_POSTFIX = "_placeholder"

    // Marker for keys that could not be found
    private const val BAD_IDENTIFIER_KEY_MARKER = "???"

    /**
     * Gets Validation message from the strings file
     * @param errorMessageId, the String which is to be retrieved
     * @return the retrieved value
     */
    fun getValidationMessage(errorMessageId: String, context: Context): String {
        return retrieveString(VALIDATION_ERROR_PREFIX + errorMessageId + LABEL_POSTFIX, context)
    }

    /**
     * Gets the PaymentProductField placeholder value.
     * The placeholder can be different through overrides per payment product.
     * @param paymentProductId, the identifier of the payment product which is needed for retrieving
     * @param paymentProductFieldId, the identifier of the payment product which is needed for retrieving
     * @return the retrieved value
     */
    fun getPaymentProductFieldPlaceholderText(
        paymentProductId: String,
        paymentProductFieldId: String,
        context: Context
    ): String {
        // Check for an overridden version first
        val identifierKeyOverride: String =
            PAYMENT_PRODUCT_PREFIX +
                    paymentProductId +
                    PRODUCT_FIELDS_INFIX +
                    paymentProductFieldId +
                    PLACEHOLDER_POSTFIX
        val result = retrieveString(identifierKeyOverride, context)
        return if (!isBadIdentifierKey(result)) {
            result
        } else retrieveString(
            PAYMENT_PRODUCT_FIELD_PREFIX +
                    paymentProductFieldId +
                    PLACEHOLDER_POSTFIX,
            context
        )
    }


    /**
     * Retrieves a string with the values from strings.xml
     * @param stringResource, the string resource which needs to be retrieved
     * @return the retrieved value
     */
    @SuppressWarnings("DiscouragedApi", "deprecated")
    fun retrieveString(stringResource: String, context: Context): String {
        val resourceId =
            context.resources.getIdentifier(stringResource, "string", context.packageName)
        return if (resourceId == 0) {

            // If the String could not be found,
            // return the key marked with question marks to show that the String could not be found
            BAD_IDENTIFIER_KEY_MARKER + stringResource + BAD_IDENTIFIER_KEY_MARKER
        } else {
            context.resources.getString(resourceId)
        }
    }

    private fun isBadIdentifierKey(key: String): Boolean {
        return key.startsWith(BAD_IDENTIFIER_KEY_MARKER)
    }
}
