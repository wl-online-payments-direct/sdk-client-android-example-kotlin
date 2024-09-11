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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.onlinepayments.client.kotlin.exampleapp.common.R
import com.onlinepayments.client.kotlin.exampleapp.common.utils.PaymentScreen
import com.onlinepayments.client.kotlin.exampleapp.common.utils.StringProvider
import com.onlinepayments.client.kotlin.exampleapp.compose.theme.Green

@Composable
fun Header(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val showBackButton = shouldShowBackButton(navBackStackEntry?.destination?.route)

    HeaderContent(showBackButton = showBackButton, onBackPressed = { navController.popBackStack() })
}

@Composable
fun HeaderContent(showBackButton: Boolean, onBackPressed: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        TopAppBar(
            title = {},
            navigationIcon = {
                if (showBackButton) {
                    IconButton(onClick = { onBackPressed() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            },
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 0.dp
        )

        Image(
            painter = painterResource(id = R.drawable.logo_example_merchant),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 16.dp)
                .width(250.dp)
                .height(50.dp)
                .align(Alignment.CenterHorizontally)
        )
        Row(
            modifier = Modifier
                .padding(top = 16.dp, end = 16.dp)
                .align(Alignment.End)
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = StringProvider.retrieveString("app_securePaymentText", LocalContext.current),
                color = Green,
                fontSize = 11.sp,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Preview
@Composable
fun HeaderPreview() {
    HeaderContent(showBackButton = true, onBackPressed = {})
}

private fun shouldShowBackButton(route: String?): Boolean {
    return when (route) {
        PaymentScreen.CONFIGURATION.route -> false
        PaymentScreen.PRODUCT.route -> true
        PaymentScreen.CARD.route -> true
        PaymentScreen.RESULT.route -> false
        else -> false
    }
}
