/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.xml.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

fun Context.getStringFromClipboard(): String {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return clipboard.primaryClip?.getItemAt(0)?.text.toString()
}

fun Context.copyToClipboard(label: String, text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}
