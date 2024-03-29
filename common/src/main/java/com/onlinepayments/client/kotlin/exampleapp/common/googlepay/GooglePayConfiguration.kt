/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.common.googlepay

data class GooglePayConfiguration(
    val configureGooglePay: Boolean,
    val merchantId: String,
    val merchantName: String
)
