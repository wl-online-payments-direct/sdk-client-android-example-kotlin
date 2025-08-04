/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.client.kotlin.exampleapp.common.googlepay

import android.app.Activity
import android.util.Log
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.onlinepayments.sdk.client.android.model.paymentproduct.specificdata.PaymentProduct320SpecificData
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Contains helper methods for dealing with the Payments API.
 * @see (https://developers.google.com/pay/api/android/guides/tutorial)
 */
class GooglePayPaymentUtil(
    private val activity: Activity,
    private val merchantId: String,
    private val merchantName: String,
    private val googlePaySpecificData: PaymentProduct320SpecificData
) {

    /**
     * Creates an instance of [PaymentsClient] for use in an [Activity].
     */
    val paymentsClient: PaymentsClient
        get() {
            val walletOptions = Wallet.WalletOptions.Builder()
                .setEnvironment(PAYMENTS_ENVIRONMENT)
                .build()
            return Wallet.getPaymentsClient(activity, walletOptions)
        }

    /**
     * Creates a Google Pay API base request object with properties used in all requests.
     *
     * @return Google Pay API base request object
     * @throws JSONException
     */
    private val baseRequest: JSONObject
        get() {
            return JSONObject().apply {
                put("apiVersion", GOOGLE_PAY_API_VERSION)
                put("apiVersionMinor", GOOGLE_PAY_API_VERSION_MINOR)
            }
        }

    /**
     * Information about the merchant requesting payment information.
     *
     * @return Information about the merchant
     * @throws JSONException
     */
    private val merchantInfo: JSONObject
        get() {
            return JSONObject().apply {
                put("merchantName", merchantName)
            }
        }

    /**
     * Gateway Integration: Identify your gateway and your app's gateway merchant identifier.
     *
     * The Google Pay API response will return an encrypted payment method capable of being charged
     * by a supported gateway after payer authorization.
     *
     * @return Payment data tokenization for the CARD payment method.
     * @throws JSONException
     */
    private val gatewayTokenizationSpecification: JSONObject
        get() {
            val parameters = JSONObject().apply {
                put("gateway", googlePaySpecificData.gateway)
                put("gatewayMerchantId", merchantId)
            }

            return JSONObject().apply {
                put("type", "PAYMENT_GATEWAY")
                put("parameters", parameters)
            }
        }

    /**
     * Card networks supported by your app and your gateway.
     *
     * @return Allowed card networks
     * @see (https://developers.google.com/pay/api/android/reference/request-objects#CardParameters)
     */
    private val allowedCardNetworks = JSONArray(googlePaySpecificData.networks)

    /**
     * Card authentication methods supported by your app and your gateway.
     *
     * @return Allowed card authentication methods
     */
    private val allowedCardAuthMethods = JSONArray(SUPPORTED_METHODS)

    /**
     * Describe your app's support for the CARD payment method.
     *
     * @return A CARD payment method object describing accepted cards.
     * @throws JSONException
     */
    private val baseCardPaymentMethod: JSONObject
        get() {
            val parameters = JSONObject().apply {
                put("allowedAuthMethods", allowedCardAuthMethods)
                put("allowedCardNetworks", allowedCardNetworks)
                put("billingAddressRequired", false)
            }

            return JSONObject().apply {
                put("type", "CARD")
                put("parameters", parameters)
            }
        }

    /**
     * Describe the expected returned payment data for the CARD payment method
     *
     * @return A CARD PaymentMethod describing accepted cards and optional fields.
     * @throws JSONException
     * @see (https://developers.google.com/pay/api/android/reference/object.PaymentMethod)
     */
    private val cardPaymentMethod: JSONObject
        get() {
            val cardPaymentMethod = baseCardPaymentMethod
            cardPaymentMethod.put("tokenizationSpecification", gatewayTokenizationSpecification)

            return cardPaymentMethod
        }

    /**
     * Provide Google Pay API with a payment amount, currency, and amount status.
     *
     * @return information about the requested payment.
     * @throws JSONException
     */
    @Throws(JSONException::class)
    private fun getTransactionInfo(price: Long, countryCode: String, currencyCode: String): JSONObject {
        return JSONObject().apply {
            put("totalPrice", (price / 100f).toString())
            put("totalPriceStatus", "FINAL")
            put("countryCode", countryCode)
            put("currencyCode", currencyCode)
        }
    }

    /**
     * An object describing information requested in a Google Pay payment sheet
     *
     * @return Payment data expected by your app.
     */
    fun getPaymentDataRequest(price: Long, countryCode: String, currencyCode: String): JSONObject? {
        return try {
            JSONObject(baseRequest.toString()).apply {
                put("allowedPaymentMethods", JSONArray().put(cardPaymentMethod))
                put("transactionInfo", getTransactionInfo(price, countryCode, currencyCode))
                put("merchantInfo", merchantInfo)
            }
        } catch (e: JSONException) {
            Log.e(javaClass.name, e.toString())
            null
        }
    }

    private companion object {
        /**
         * The Google Pay API may return cards on file on Google.com (PAN_ONLY) and/or a device token on
         * an Android device authenticated with a 3-D Secure cryptogram (CRYPTOGRAM_3DS).
         *
         * @value #SUPPORTED_METHODS
         */
        val SUPPORTED_METHODS = listOf(
            "PAN_ONLY",
            "CRYPTOGRAM_3DS"
        )

        /**
         * Changing this to ENVIRONMENT_PRODUCTION will make the API return chargeable card information.
         * Please refer to the documentation to read about the required steps needed to enable
         * ENVIRONMENT_PRODUCTION.
         *
         * @value #PAYMENTS_ENVIRONMENT
         */
        const val PAYMENTS_ENVIRONMENT = WalletConstants.ENVIRONMENT_TEST

        const val GOOGLE_PAY_API_VERSION = 2
        const val GOOGLE_PAY_API_VERSION_MINOR = 0
    }
}
