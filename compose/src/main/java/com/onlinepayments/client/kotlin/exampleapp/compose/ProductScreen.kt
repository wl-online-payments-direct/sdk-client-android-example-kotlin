/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.compose

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.onlinepayments.client.kotlin.exampleapp.common.PaymentSharedViewModel
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants.CARD_NUMBER
import com.onlinepayments.client.kotlin.exampleapp.common.utils.PaymentScreen
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Status
import com.onlinepayments.client.kotlin.exampleapp.common.utils.StringProvider
import com.onlinepayments.client.kotlin.exampleapp.compose.components.BottomSheetContent
import com.onlinepayments.client.kotlin.exampleapp.compose.components.FailedText
import com.onlinepayments.client.kotlin.exampleapp.compose.components.ProgressIndicator
import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFile
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItems
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProduct

@Composable
fun ProductScreen(
    navController: NavHostController,
    paymentSharedViewModel: PaymentSharedViewModel,
    showBottomSheet: (BottomSheetContent) -> Unit,
    launchGooglePay: () -> Unit
) {

    val paymentProductStatus by paymentSharedViewModel.paymentProductsStatus.observeAsState(Status.None)
    val context = LocalContext.current

    ProductContent(
        paymentProductStatus = paymentProductStatus,
        assetsBaseUrl = "",
        onItemClicked = { selectedPaymentProduct ->
            paymentSharedViewModel.selectedPaymentProduct = selectedPaymentProduct
            navigateToScreen(navController, context, selectedPaymentProduct, showBottomSheet = {
                showBottomSheet(it)
            }, launchGooglePay = { launchGooglePay() })
        })
}

@Composable
fun ProductContent(
    paymentProductStatus: Status,
    assetsBaseUrl: String,
    onItemClicked: (Any) -> Unit
) {
    when (paymentProductStatus) {
        is Status.ApiError -> {
            FailedText()
        }
        is Status.Loading -> {
            ProgressIndicator()
        }
        is Status.Success -> {
            PaymentProductItems(
                basicPaymentItems = paymentProductStatus.data as BasicPaymentItems,
                assetsBaseUrl = assetsBaseUrl,
                onItemClicked = { onItemClicked(it) }
            )
        }
        is Status.None -> {
            // Init status; nothing to do here
        }
        is Status.Failed -> {
            FailedText()
        }
    }
}

@Composable
private fun PaymentProductItems(
    basicPaymentItems: BasicPaymentItems,
    assetsBaseUrl: String,
    onItemClicked: (Any) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (basicPaymentItems.accountsOnFile.isNotEmpty()) {
            item {
                SectionHeader(
                    text = StringProvider.retrieveString(
                        "paymentProductSelection_accountsOnFileTitle",
                        LocalContext.current
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(basicPaymentItems.accountsOnFile) { accountOnFile ->
                val basicPaymentItem =
                    basicPaymentItems.basicPaymentItems.find {
                        it.id == accountOnFile.paymentProductId
                    }
                PaymentProductItem(
                    imageUrl = basicPaymentItem?.displayHintsList?.get(0)?.logoUrl,
                    label = accountOnFile.displayHints.labelTemplate[0].mask?.let { mask ->
                        accountOnFile.getMaskedValue(CARD_NUMBER, mask)}
                    ?: run {
                        accountOnFile.label
                    },
                    onItemClicked = {
                        onItemClicked(accountOnFile)
                    }
                )
            }

            item {
                SectionHeader(
                    text = StringProvider.retrieveString(
                        "paymentProductSelection_paymentProductsTitle",
                        LocalContext.current
                    ),
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                )
            }
        }

        items(basicPaymentItems.basicPaymentItems) { basicPaymentItem ->
            PaymentProductItem(
                imageUrl = assetsBaseUrl + basicPaymentItem.displayHintsList[0].logoUrl,
                label = basicPaymentItem.displayHintsList[0].label,
                onItemClicked = {
                    onItemClicked(basicPaymentItem)
                })
        }
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.h6,
        modifier = modifier
    )
}

@Composable
private fun PaymentProductItem(imageUrl: String?, label: String, onItemClicked: () -> Unit) {
    Surface(
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(vertical = 6.dp)
            .clickable { onItemClicked() }) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .width(50.dp)
                    .height(25.dp)
            )

            Text(
                text = label,
                style = MaterialTheme.typography.body1,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(alignment = Alignment.CenterVertically)
            )
        }
    }
}

private fun navigateToScreen(
    navController: NavHostController,
    context: Context,
    selectedPaymentProduct: Any,
    showBottomSheet: (BottomSheetContent) -> Unit,
    launchGooglePay: () -> Unit
) {
    when (selectedPaymentProduct) {

        is AccountOnFile -> {
            navController.navigate(PaymentScreen.CARD.route)
        }

        is BasicPaymentProduct -> {
            when {
                selectedPaymentProduct.paymentMethod.equals(
                    Constants.PAYMENT_METHOD_CARD,
                    ignoreCase = true
                ) -> {
                    navController.navigate(PaymentScreen.CARD.route)
                }
                selectedPaymentProduct.id.equals(Constants.GOOGLE_PAY_PRODUCT_ID) -> {
                    launchGooglePay()
                }
                else -> {
                    showBottomSheet(
                        BottomSheetContent(StringProvider.retrieveString("errors.productUnavailable", context))
                    )
                }
            }
        }
        else -> {
            showBottomSheet(
                BottomSheetContent(StringProvider.retrieveString("errors.productUnavailable", context))
            )
        }
    }
}
