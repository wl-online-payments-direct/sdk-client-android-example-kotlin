/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.common.utils

import android.content.Context

object Translator {
    // Prefixes for loading validation and tooltip translations
    private const val TRANSLATION_PREFIX_PRODUCTFIELD = "gc.general.paymentProductFields."
    private const val TRANSLATION_PREFIX_VALIDATION =
        "gc.general.paymentProductFields.validationErrors."
    private const val TRANSLATION_PREFIX_PRODUCT = "gc.general.paymentProducts."
    private const val TRANSLATION_PREFIX_PRODUCTGROUP = "gc.general.paymentProductGroups."
    private const val TRANSLATION_PREFIX_COBRAND = "gc.general.cobrands."

    // Postfixes for loading validation and tooltip translations
    private const val TRANSLATION_POSTFIX_NAME = ".name"
    private const val TRANSLATION_POSTFIX_LABEL = ".label"
    private const val TRANSLATION_POSTFIX_TOOLTIP_TEXT = ".tooltipText"
    private const val TRANSLATION_POSTFIX_TOOLTIP_IMAGE = ".tooltipImage"
    private const val TRANSLATION_POSTFIX_PRODUCTFIELD = ".paymentProductFields."
    private const val TRANSLATION_POSTFIX_PLACEHOLDER = ".placeholder"

    // Marker for keys that could not be found
    private const val BAD_TRANSLATION_KEY_MARKER = "???"

    /**
     * Gets Validation message from the translations file
     * @param errorMessageId, the String which is to be translated
     * @return the translated value
     */
    fun getValidationMessage(errorMessageId: String, context: Context): String {
        return translateString(TRANSLATION_PREFIX_VALIDATION + errorMessageId + TRANSLATION_POSTFIX_LABEL, context)
    }


    /**
     * Gets the PaymentProduct name from the translations file
     * @param paymentProductId, the identifier of the payment product which is needed for translating
     * @return the translated value
     */
    fun getPaymentProductName(paymentProductId: String, context: Context): String {
        return translateString(TRANSLATION_PREFIX_PRODUCT + paymentProductId + TRANSLATION_POSTFIX_NAME, context)
    }


    /**
     * Gets the PaymentProductGroup name from the translations file
     * @param paymentProductGroupId, the identifier of the payment product group which is needed for translating
     * @return the translated value
     */
    fun getPaymentProductGroupName(paymentProductGroupId: String, context: Context): String {
        return translateString(
            TRANSLATION_PREFIX_PRODUCTGROUP +
                    paymentProductGroupId +
                    TRANSLATION_POSTFIX_NAME,
                    context
        )
    }

    /**
     * Gets the PaymentProductField name from the translations file
     * The translation can be different per payment product.
     * @param paymentProductFieldId, the identifier of the payment product which is needed for translating
     * @return the translated value
     */
    fun getPaymentProductFieldName(
        paymentProductFieldId: String,
        context: Context
    ): String {
        return translateString(
            TRANSLATION_PREFIX_PRODUCTFIELD +
                    paymentProductFieldId +
                    TRANSLATION_POSTFIX_NAME, context
        )
    }

    /**
     * Gets the PaymentProductField label from the translations file
     * The translation can be different per payment product.
     * @param paymentProductId, the identifier of the payment product which is needed for translating
     * @param paymentProductFieldId, the identifier of the payment product which is needed for translating
     * @return the translated value
     */
    fun getPaymentProductFieldLabel(
        paymentProductId: String,
        paymentProductFieldId: String,
        context: Context
    ): String {
        // Check for an overridden version first
        val translationKeyOverride: String =
            TRANSLATION_PREFIX_PRODUCT +
                    paymentProductId +
                    TRANSLATION_POSTFIX_PRODUCTFIELD +
                    paymentProductFieldId +
                    TRANSLATION_POSTFIX_LABEL
        return if (!isBadTranslationKey(translateString(translationKeyOverride, context))) {
            translateString(translationKeyOverride, context)
        } else translateString(
            TRANSLATION_PREFIX_PRODUCTFIELD +
                    paymentProductFieldId +
                    TRANSLATION_POSTFIX_LABEL,
            context
        )
    }

    /**
     * Gets the PaymentProductField placeholder value.
     * The translation can be different per payment product.
     * @param paymentProductId, the identifier of the payment product which is needed for translating
     * @param paymentProductFieldId, the identifier of the payment product which is needed for translating
     * @return the translated value
     */
    fun getPaymentProductFieldPlaceholderText(
        paymentProductId: String,
        paymentProductFieldId: String,
        context: Context
    ): String {
        // Check for an overridden version first
        val translationKeyOverride: String =
            TRANSLATION_PREFIX_PRODUCT +
                    paymentProductId +
                    TRANSLATION_POSTFIX_PRODUCTFIELD +
                    paymentProductFieldId +
                    TRANSLATION_POSTFIX_PLACEHOLDER
        return if (!isBadTranslationKey(translateString(translationKeyOverride, context))) {
            translateString(translationKeyOverride, context)
        } else translateString(
            TRANSLATION_PREFIX_PRODUCTFIELD +
                    paymentProductFieldId +
                    TRANSLATION_POSTFIX_PLACEHOLDER,
            context
        )
    }

    /**
     * Gets the co brand notification/tooltip value.
     * @param coBrandMessageId The identifier of the coBrand message that is requested
     * @return The translated value
     */
    fun getCoBrandNotificationText(coBrandMessageId: String, context: Context): String {
        return translateString(TRANSLATION_PREFIX_COBRAND + coBrandMessageId, context)
    }

    /**
     * Gets the PaymentProductField tooltip value.
     * The translation can be different per payment product.
     * @param paymentProductId, the identifier of the payment product which is needed for translating
     * @param paymentProductFieldId, the identifier of the payment product which is needed for translating
     * @return the translated value
     */
    fun getPaymentProductFieldTooltipText(
        paymentProductId: String,
        paymentProductFieldId: String,
        context: Context
    ): String {
        // Check for an overridden version first
        val translationKeyOverride: String =
            TRANSLATION_PREFIX_PRODUCT +
                    paymentProductId +
                    TRANSLATION_POSTFIX_PRODUCTFIELD +
                    paymentProductFieldId +
                    TRANSLATION_POSTFIX_TOOLTIP_TEXT
        return if (!isBadTranslationKey(translateString(translationKeyOverride, context))) {
            translateString(translationKeyOverride, context)
        } else translateString(
            TRANSLATION_PREFIX_PRODUCTFIELD +
                    paymentProductFieldId +
                    TRANSLATION_POSTFIX_TOOLTIP_TEXT,
            context
        )
    }


    /**
     * Gets the PaymentProductField tooltip image.
     * The image can be different per payment product.
     * @param paymentProductId, the identifier of the payment product which is needed for translating
     * @param paymentProductFieldId, the identifier of the payment product which is needed for translating
     * @return the drawable id of the tooltip image
     */
    fun getPaymentProductFieldTooltipImage(
        paymentProductId: String,
        paymentProductFieldId: String,
        context: Context
    ): Int? {
        // Find the belonging Drawable
        val translationKeyOverride: String =
            TRANSLATION_PREFIX_PRODUCT +
                    paymentProductId +
                    TRANSLATION_POSTFIX_PRODUCTFIELD +
                    paymentProductFieldId +
                    TRANSLATION_POSTFIX_TOOLTIP_IMAGE
        if (!isBadTranslationKey(translateString(translationKeyOverride, context))) {
            val drawableId = context.resources.getIdentifier(
                translateString(translationKeyOverride, context),
                "drawable",
                context.packageName
            )
            if (drawableId != 0) {
                return drawableId
            }
        }
        val translationKeyDefault: String =
            TRANSLATION_PREFIX_PRODUCTFIELD + paymentProductFieldId + TRANSLATION_POSTFIX_TOOLTIP_IMAGE
        if (!isBadTranslationKey(translateString(translationKeyDefault, context))) {
            val drawableId = context.resources.getIdentifier(
                translateString(translationKeyDefault, context),
                "drawable",
                context.packageName
            )
            if (drawableId != 0) {
                return drawableId
            }
        }
        return null
    }


    /**
     * Translates a string with the values from strings.xml
     * @param stringResource, the string resource which needs to be retrieved
     * @return the translated value
     */
    fun translateString(stringResource: String, context: Context): String {
        val resourceId =
            context.resources.getIdentifier(stringResource, "string", context.packageName)
        return if (resourceId == 0) {

            // If the translation could not be found,
            // return the key marked with question marks to show that the String could not be found
            BAD_TRANSLATION_KEY_MARKER + stringResource + BAD_TRANSLATION_KEY_MARKER
        } else {
            context.resources.getString(resourceId)
        }
    }

    private fun isBadTranslationKey(key: String): Boolean {
        return key.startsWith(BAD_TRANSLATION_KEY_MARKER)
    }
}
