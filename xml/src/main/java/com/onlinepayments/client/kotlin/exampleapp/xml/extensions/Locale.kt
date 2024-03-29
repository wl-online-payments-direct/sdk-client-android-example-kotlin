/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.xml.extensions

import android.content.Context
import android.os.Build
import java.util.Locale

fun Context.getCurrentLocale(): Locale {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        this.resources.configuration.locales[0]
    } else {
        this.resources.configuration.locale
    }
}
