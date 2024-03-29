/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.common.utils

import android.content.Context
import com.onlinepayments.sdk.client.android.model.validation.ValidationErrorMessage
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleLength
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleRange

/**
 * Maps validation error message id to complete error formatted in the currently selected locale
 */
object ValidationErrorMessageMapper {
    private const val VALIDATION_LENGTH_EXCEPTION_EXACT = "length.exact"
    private const val VALIDATION_LENGTH_EXCEPTION_MAX = "length.max"
    private const val VALIDATION_LENGTH_EXCEPTION_BETWEEN = "length.between"
    private const val VALIDATION_LENGTH_MIN_PLACEHOLDER = "{minLength}"
    private const val VALIDATION_LENGTH_MAX_PLACEHOLDER = "{maxLength}"

    fun mapValidationErrorMessageToString(context: Context, validationErrorMessage: ValidationErrorMessage): String {
        var errorMessage = Translator.getValidationMessage(validationErrorMessage.errorMessage, context)

        return when (val validationRule = validationErrorMessage.rule) {
            is ValidationRuleLength -> {
                errorMessage =
                    Translator.getValidationMessage(mapLengthExceptionToString(validationRule), context)
                replaceLengthPlaceholders(errorMessage, validationRule.minLength, validationRule.maxLength)
            }
            is ValidationRuleRange -> {
                replaceLengthPlaceholders(errorMessage, validationRule.minValue, validationRule.maxValue)
            }

            else -> {
                errorMessage
            }
        }
    }

    private fun replaceLengthPlaceholders(errorMessage: String, minLength: Int, maxLength: Int): String {
        var errorMessageWithoutPlaceholders =
            errorMessage.replace(VALIDATION_LENGTH_MIN_PLACEHOLDER, minLength.toString())
        errorMessageWithoutPlaceholders = errorMessageWithoutPlaceholders.replace(
            VALIDATION_LENGTH_MAX_PLACEHOLDER, maxLength.toString())
        return errorMessageWithoutPlaceholders
    }

    private fun mapLengthExceptionToString(validationRuleLength: ValidationRuleLength): String {
        return when (validationRuleLength.minLength) {
            validationRuleLength.maxLength -> {
                VALIDATION_LENGTH_EXCEPTION_EXACT
            }
            null, 0 -> {
                VALIDATION_LENGTH_EXCEPTION_MAX
            }
            else -> {
                VALIDATION_LENGTH_EXCEPTION_BETWEEN
            }
        }
    }
}
