/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.compose.configuration

sealed class ConfigurationStatus {
    data object InValid : ConfigurationStatus()
    data object Valid : ConfigurationStatus()
    data object None : ConfigurationStatus()
}
