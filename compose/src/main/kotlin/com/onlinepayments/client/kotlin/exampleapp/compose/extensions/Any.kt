/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.compose.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun Any.convertToString(): String {
    return when (this) {
        is Int -> stringResource(id = this)
        is String -> this
        else -> "no supported value"
    }
}
