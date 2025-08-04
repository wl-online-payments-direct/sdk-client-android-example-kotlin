/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.compose.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

open class TextFieldState(
    text: String,
    keyboardOptions: KeyboardOptions,
    label: Any,
    enabled: Boolean,
    liveValidating: Boolean,
    private val validator: (String) -> IsValid = { IsValid.Unknown },
    private val errorText: (Any) -> Any = { "" }

) {
    var text: String by mutableStateOf(text)
    var keyboardOptions: KeyboardOptions by mutableStateOf(keyboardOptions)
    var label: Any by mutableStateOf(label)
    var enabled: Boolean by mutableStateOf(enabled)
    var liveValidating: Boolean by mutableStateOf(liveValidating)
    var displayError: Boolean by mutableStateOf(false)

    open val isValid: IsValid
        get() = validator(text)

    open fun getError(): Any? {
        return if (displayError) {
            isValid.errorMessage?.let { errorText(it) }
        } else {
            null
        }
    }
}

sealed class IsValid(
    val errorMessage: Any? = null
) {
    data object Yes : IsValid()
    class No(errorMessage: Any) : IsValid(errorMessage)
    data object Unknown : IsValid()
}
