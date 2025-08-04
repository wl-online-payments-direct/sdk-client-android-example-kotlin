/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.common.utils

import com.onlinepayments.sdk.client.android.model.api.ErrorResponse


/**
 * An example for how to store the result of async call
 */
sealed class Status {
    data class Success(val data: Any?) : Status()
    data class Failed(val throwable: Throwable) : Status()
    data class ApiError(val apiError: ErrorResponse) : Status()
    data object Loading : Status()
    data object None: Status()
}
