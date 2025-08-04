/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.common.utils

import com.onlinepayments.sdk.client.android.model.validation.ValidationErrorMessage

/**
 * An example for how to store the result of a form validation.
 */
sealed class FormValidationResult {
    data class Invalid(val exceptions: List<Exception>?) : FormValidationResult()
    data class InvalidWithValidationErrorMessages(val errorMessages: List<ValidationErrorMessage>) :
        FormValidationResult()
    data object Valid : FormValidationResult()
    data object NotValidated: FormValidationResult()
}
