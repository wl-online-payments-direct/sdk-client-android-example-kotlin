/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.compose.extensions

import androidx.compose.ui.text.input.KeyboardType

fun String.isValidInput(keyboardType: KeyboardType): Boolean {
    if (this == "") return true
    val regex = when (keyboardType) {
        KeyboardType.Text -> "^[ -~]*\$"
        KeyboardType.Number -> "[0-9]+"
        else -> "^[ -~]*\$"
    }
    return (regex.toRegex().matches(this))
}

fun String.isTabEvent(): Boolean {
    return this == "\t"
}
