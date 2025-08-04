/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.client.kotlin.exampleapp.common

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.onlinepayments.client.kotlin.exampleapp.common.googlepay.GooglePayConfiguration
import com.onlinepayments.client.kotlin.exampleapp.common.utils.PaymentScreen
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Status
import com.onlinepayments.sdk.client.android.model.PaymentContext
import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFile
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProduct
import com.onlinepayments.sdk.client.android.session.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Shared ViewModel for sharing objects with multiple fragments.
 * As a result, the Session object is created only once and can be used by multiple fragments.
 */
class PaymentSharedViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStoreCoroutineScope = CoroutineScope(Dispatchers.IO)

    lateinit var session: Session
        private set

    lateinit var paymentContext: PaymentContext
        private set

    var googlePayConfiguration = GooglePayConfiguration(false, "", "")
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("sessionInputPreferences")

    val globalErrorMessage = MutableLiveData("")
    val paymentProductsStatus = MutableLiveData<Status>()
    var selectedPaymentProduct: Any? = null
    val selectedPaymentProductId: String?
        get() = when (val paymentProduct = selectedPaymentProduct) {
            is BasicPaymentProduct -> {
                paymentProduct.getId()
            }

            is AccountOnFile -> {
                paymentProduct.paymentProductId
            }

            else -> {
                // This case should not happen
                ""
            }
        }

    // Only used in XML example
    val activePaymentScreen = MutableLiveData(PaymentScreen.CONFIGURATION)

    // Only used in Compose example
    val googlePayData = MutableLiveData<String>()

    /**
     * Initialize a Session which can be used to do API calls
     * The result is saved in this shared ViewModel
     */
    fun initializeSession(
        clientSessionId: String,
        customerId: String,
        clientApiUrl: String,
        assetUrl: String,
        environmentIsProduction: Boolean,
        appIdentifier: String,
        loggingEnabled: Boolean = false
    ) {
        session = Session(
            clientSessionId,
            customerId,
            clientApiUrl,
            assetUrl,
            environmentIsProduction,
            appIdentifier,
            loggingEnabled,
            getApplication<Application>().applicationContext
        )
        dataStoreCoroutineScope.launch {
            saveInputToDataStore(CLIENT_SESSION_ID_KEY, clientSessionId)
            saveInputToDataStore(CUSTOMER_ID_KEY, customerId)
            saveInputToDataStore(CLIENT_API_URL_KEY, clientApiUrl)
            saveInputToDataStore(ASSET_URL_KEY, assetUrl)
        }

        getBasicPaymentItems(paymentContext)
    }

    /**
     * Set paymentContext, so it can be retrieved at a later stage
     * The result is saved in this shared ViewModel
     */
    fun setPaymentContext(paymentContext: PaymentContext) {
        this.paymentContext = paymentContext
        dataStoreCoroutineScope.launch {
            paymentContext.amountOfMoney?.amount?.toString()?.let { amount ->
                saveInputToDataStore(AMOUNT_KEY, amount)
            }

            paymentContext.countryCode?.let { countryCode ->
                saveInputToDataStore(COUNTRY_CODE_KEY, countryCode)
            }

            paymentContext.amountOfMoney?.currencyCode?.let { currencyCode ->
                saveInputToDataStore(CURRENCY_CODE_KEY, currencyCode)
            }
        }
    }

    /**
     * Retrieves all Payment Items for a provided Payment Context
     */
    private fun getBasicPaymentItems(paymentContext: PaymentContext) {
        paymentProductsStatus.postValue(Status.Loading)

        viewModelScope.launch {
            try {
                val items = session.getBasicPaymentItems(paymentContext)
                paymentProductsStatus.postValue(Status.Success(items))
            } catch (e: Exception) {
                paymentProductsStatus.postValue(Status.Failed(e))
            }
        }
    }

    /**
     * Save input to DataStore, so it can be retrieved when you next open the app
     */
    private suspend fun saveInputToDataStore(
        key: Preferences.Key<String>,
        input: String
    ) {
        try {
            getApplication<Application>().applicationContext.dataStore.edit { preferences ->
                preferences[key] = input
            }
        } catch (e: IOException) {
            // An error occurred when writing the input to DataStore
            Log.e(javaClass.name, e.toString())
        }
    }

    /**
     * Read input from DataStore and return it as a Map<String, String>
     */
    fun loadFromDataStore(): Flow<Map<String, String>> {
        return getApplication<Application>().applicationContext.dataStore.data.map { preferences ->
            mapOf(
                CLIENT_SESSION_ID_KEY.name to (preferences[CLIENT_SESSION_ID_KEY] ?: ""),
                CUSTOMER_ID_KEY.name to (preferences[CUSTOMER_ID_KEY] ?: ""),
                CLIENT_API_URL_KEY.name to (preferences[CLIENT_API_URL_KEY] ?: ""),
                ASSET_URL_KEY.name to (preferences[ASSET_URL_KEY] ?: ""),
                AMOUNT_KEY.name to (preferences[AMOUNT_KEY] ?: ""),
                COUNTRY_CODE_KEY.name to (preferences[COUNTRY_CODE_KEY] ?: ""),
                CURRENCY_CODE_KEY.name to (preferences[CURRENCY_CODE_KEY] ?: "")
            )
        }
    }

    /**
     * Keys used to store/retrieve data from DataStore
     */
    companion object {
        val CLIENT_SESSION_ID_KEY = stringPreferencesKey("clientSessionId")
        val CUSTOMER_ID_KEY = stringPreferencesKey("customerId")
        val CLIENT_API_URL_KEY = stringPreferencesKey("clientApiUrl")
        val ASSET_URL_KEY = stringPreferencesKey("assetUrl")
        val AMOUNT_KEY = stringPreferencesKey("amount")
        val COUNTRY_CODE_KEY = stringPreferencesKey("countryCode")
        val CURRENCY_CODE_KEY = stringPreferencesKey("currencyCode")
    }
}
