/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.onlinepayments.client.kotlin.exampleapp.compose.extensions.convertToString

@Composable
fun DefaultBottomSheet(bottomSheetContent: BottomSheetContent) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .width(75.dp)
                .height(5.dp)
                .align(Alignment.CenterHorizontally),
            shape = MaterialTheme.shapes.small,
            elevation = 2.dp,
            color = Color.LightGray
        ) {}
        Row(
            modifier = Modifier
                .padding(16.dp)
        ) {
            bottomSheetContent.text?.let {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = it.convertToString(),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }
            bottomSheetContent.imageUrl?.let {
                Column(Modifier.weight(1f)) {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = null,
                        modifier = Modifier
                            .width(180.dp)
                            .height(90.dp)
                    )
                }
            }
        }
    }
}

data class BottomSheetContent(
    val text: Any? = null,
    val imageUrl: String? = null
)
