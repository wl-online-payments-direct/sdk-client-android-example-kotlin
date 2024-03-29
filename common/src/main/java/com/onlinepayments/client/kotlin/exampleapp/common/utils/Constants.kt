/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.common.utils

object Constants {
    // GooglePay identifier
    const val GOOGLE_PAY_PRODUCT_ID = "320"

    // Cards payment product group
    const val PAYMENT_PRODUCT_GROUP_CARDS = "cards"

    // Application Identifier, used for identifying the application in network calls
    const val APPLICATION_IDENTIFIER = "OnlinePayments Android Example Application Kotlin/v1.0.0"

    // Constants used for defining payment product fields
    const val CARD_NUMBER = "cardNumber"
    const val EXPIRY_DATE = "expiryDate"
    const val SECURITY_NUMBER = "cvv"
    const val CARD_HOLDER = "cardholderName"
}
