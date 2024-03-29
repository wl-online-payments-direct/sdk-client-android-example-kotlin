/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.compose

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.onlinepayments.client.kotlin.exampleapp.common.PaymentSharedViewModel
import com.onlinepayments.client.kotlin.exampleapp.common.R
import com.onlinepayments.client.kotlin.exampleapp.common.utils.PaymentScreen
import com.onlinepayments.client.kotlin.exampleapp.compose.card.CardScreen
import com.onlinepayments.client.kotlin.exampleapp.compose.components.BottomSheetContent
import com.onlinepayments.client.kotlin.exampleapp.compose.components.DefaultBottomSheet
import com.onlinepayments.client.kotlin.exampleapp.compose.components.Header
import com.onlinepayments.client.kotlin.exampleapp.compose.configuration.ConfigurationScreen
import com.onlinepayments.client.kotlin.exampleapp.compose.configuration.ConfigurationViewModel
import com.onlinepayments.client.kotlin.exampleapp.compose.theme.OnlinePaymentsTheme
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun ComposeApp(paymentSharedViewModel: PaymentSharedViewModel, launchGooglePay: () -> Unit) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    val configurationViewModel: ConfigurationViewModel = viewModel()

    val encryptedFieldsData = paymentSharedViewModel.googlePayData.observeAsState("")
    if (encryptedFieldsData.value.isNotBlank()){
        navController.navigate("${PaymentScreen.RESULT.route}/${encryptedFieldsData.value}")
    }

    val focusManager = LocalFocusManager.current
    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    var informationBottomSheetContent by
    remember { mutableStateOf(BottomSheetContent(R.string.payment_configuration_merchant_name_helper_text)) }

    OnlinePaymentsTheme {
        ModalBottomSheetLayout(
            sheetState = bottomSheetState,
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            sheetContent = {
                DefaultBottomSheet(informationBottomSheetContent)
            },
            content = {
                Scaffold(
                    topBar = {
                        Header(navController = navController)
                    },
                    snackbarHost = {
                        SnackbarHost(
                            hostState = it,
                            modifier = Modifier.systemBarsPadding(),
                            snackbar = { snackbarData -> Snackbar(snackbarData = snackbarData) })
                    },
                ) { paddingValues ->
                    AnimatedNavigation(
                        modifier = Modifier.padding(paddingValues),
                        navController = navController,
                        paymentSharedViewModel = paymentSharedViewModel,
                        configurationViewModel = configurationViewModel,
                        showBottomSheet = {
                            coroutineScope.launch {
                                informationBottomSheetContent = it
                                focusManager.clearFocus()
                                bottomSheetState.show()
                            }
                        },
                        launchGooglePay = { launchGooglePay() }
                    )
                }
            }
        )
    }
}

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
private fun AnimatedNavigation(
    modifier: Modifier,
    navController: NavHostController,
    paymentSharedViewModel: PaymentSharedViewModel,
    configurationViewModel: ConfigurationViewModel,
    showBottomSheet: (BottomSheetContent) -> Unit,
    launchGooglePay: () -> Unit
) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = PaymentScreen.CONFIGURATION.route
    ) {
        composable(
            PaymentScreen.CONFIGURATION.route,
            enterTransition = {
                enterTransitionConfigurationRoute(this)
            },
            exitTransition = {
                exitTransitionConfigurationRoute(this)
            }
        ) {
            ConfigurationScreen(
                navController = navController,
                paymentSharedViewModel = paymentSharedViewModel,
                configurationViewModel = configurationViewModel,
                showBottomSheet = { showBottomSheet(it) })
        }
        composable(
            PaymentScreen.PRODUCT.route,
            enterTransition = {
                enterTransitionProductRoute(this)
            },
            exitTransition = {
                exitTransitionProductRoute(this)
            }
        ) {
            ProductScreen(
                navController = navController,
                paymentSharedViewModel = paymentSharedViewModel,
                showBottomSheet = { showBottomSheet(it) },
                launchGooglePay = { launchGooglePay() })
        }
        composable(
            PaymentScreen.CARD.route,
            enterTransition = {
                enterTransitionCardRoute(this)
            },
            exitTransition = {
                exitTransitionCardRoute(this)
            }
        ) {
            CardScreen(
                navController = navController,
                paymentSharedViewModel = paymentSharedViewModel,
                showBottomSheet = { showBottomSheet(it) })
        }
        composable(
            "${PaymentScreen.RESULT.route}/{encryptedFieldsData}",
            enterTransition = {
                enterTransitionResultRoute(this)
            },
            exitTransition = {
                exitTransitionResultRoute(this)
            }
        ) { backStackEntry ->
            ResultScreen(
                navController = navController,
                encryptedFieldsData = backStackEntry.arguments?.getString("encryptedFieldsData")
            )
        }
    }
}

@ExperimentalAnimationApi
private fun enterTransitionConfigurationRoute(
    scope: AnimatedContentTransitionScope<NavBackStackEntry>
): EnterTransition? {
    return when (scope.initialState.destination.route) {
        PaymentScreen.PRODUCT.route ->
            scope.slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(700)
            )
        else -> null
    }
}

@ExperimentalAnimationApi
private fun exitTransitionConfigurationRoute(
    scope: AnimatedContentTransitionScope<NavBackStackEntry>
): ExitTransition? {
    return when (scope.targetState.destination.route) {
        PaymentScreen.PRODUCT.route ->
            scope.slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(700)
            )
        else -> null
    }
}

@ExperimentalAnimationApi
private fun enterTransitionProductRoute(scope: AnimatedContentTransitionScope<NavBackStackEntry>): EnterTransition? {
    return when (scope.initialState.destination.route) {
        PaymentScreen.CONFIGURATION.route ->
            scope.slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(700)
            )
        PaymentScreen.CARD.route ->
            scope.slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(700)
            )
        else -> null
    }
}

@ExperimentalAnimationApi
private fun exitTransitionProductRoute(scope: AnimatedContentTransitionScope<NavBackStackEntry>): ExitTransition? {
    return when (scope.targetState.destination.route) {
        PaymentScreen.CONFIGURATION.route ->
            scope.slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(700)
            )
        PaymentScreen.CARD.route ->
            scope.slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(700)
            )
        else -> null
    }
}

@ExperimentalAnimationApi
private fun enterTransitionCardRoute(scope: AnimatedContentTransitionScope<NavBackStackEntry>): EnterTransition? {
    return when (scope.initialState.destination.route) {
        PaymentScreen.PRODUCT.route ->
            scope.slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(700)
            )
        PaymentScreen.RESULT.route ->
            scope.slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(700)
            )
        else -> null
    }
}

@ExperimentalAnimationApi
private fun exitTransitionCardRoute(scope: AnimatedContentTransitionScope<NavBackStackEntry>): ExitTransition? {
    return when (scope.targetState.destination.route) {
        PaymentScreen.PRODUCT.route ->
            scope.slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(700)
            )
        PaymentScreen.RESULT.route ->
            scope.slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(700)
            )
        else -> null
    }
}

@ExperimentalAnimationApi
private fun enterTransitionResultRoute(scope: AnimatedContentTransitionScope<NavBackStackEntry>): EnterTransition? {
    return when (scope.initialState.destination.route) {
        PaymentScreen.CARD.route ->
            scope.slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(700)
            )
        else -> null
    }
}

@ExperimentalAnimationApi
private fun exitTransitionResultRoute(scope: AnimatedContentTransitionScope<NavBackStackEntry>): ExitTransition? {
    return when (scope.targetState.destination.route) {
        PaymentScreen.CARD.route ->
            scope.slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(700)
            )
        else -> null
    }
}
