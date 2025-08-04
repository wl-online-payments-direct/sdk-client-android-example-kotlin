/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.common.utils

/**
 * Enum class containing all payment screens.
 * This class can be used to navigate and to assign a navigation icon
 */
enum class PaymentScreen(val route: String) {
    CONFIGURATION("configuration"),
    PRODUCT("product"),
    CARD("card"),
    RESULT("result")
}
