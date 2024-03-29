/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.common.utils

/**
 * This class is used to easily copy the data from the clipboard into the Session configuration input fields
 */
data class SessionConfiguration(
    val clientSessionId: String,
    val customerId: String,
    val clientApiUrl: String,
    val assetUrl: String,
)
