/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.onlinepayments.client.kotlin.exampleapp.common.utils.StringProvider
import com.onlinepayments.client.kotlin.exampleapp.compose.theme.DarkGray

@Composable
fun LabelledCheckbox(
    checkBoxField: CheckBoxField,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onTrailingIconClicked: (() -> Unit?)? = null
) {
    if (checkBoxField.visible.value) {
        Row(
            Modifier
            .fillMaxWidth()) {
            Checkbox(
                checked = checkBoxField.isChecked.value,
                onCheckedChange = {
                    checkBoxField.isChecked.value = it
                    if (onCheckedChange != null) {
                        onCheckedChange(it)
                    }
                },
                enabled = checkBoxField.enabled.value
            )
            Text(
                text = StringProvider.retrieveString(checkBoxField.stringResource, LocalContext.current),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .align(Alignment.CenterVertically)
            )
            if (checkBoxField.bottomSheetContent.text != null) {
                Box(Modifier.fillMaxWidth()) {
                    IconButton(onClick = { onTrailingIconClicked?.let { it() } }, modifier = Modifier.align(
                        Alignment.CenterEnd)) {
                        Icon(imageVector = Icons.Outlined.Info, null, tint = DarkGray)
                    }
                }
            }
        }
    }
}

data class CheckBoxField(
    val stringResource: String,
    val bottomSheetContent: BottomSheetContent = BottomSheetContent(text = null)
) {
    val isChecked = mutableStateOf(false)
    val enabled = mutableStateOf(true)
    val visible = mutableStateOf(true)
}
