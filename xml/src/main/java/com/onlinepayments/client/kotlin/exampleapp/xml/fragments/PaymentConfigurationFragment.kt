/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.xml.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.onlinepayments.client.kotlin.exampleapp.common.BuildConfig
import com.onlinepayments.client.kotlin.exampleapp.common.PaymentSharedViewModel
import com.onlinepayments.client.kotlin.exampleapp.common.googlepay.GooglePayConfiguration
import com.onlinepayments.client.kotlin.exampleapp.common.R.string
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants
import com.onlinepayments.client.kotlin.exampleapp.common.utils.PaymentScreen
import com.onlinepayments.client.kotlin.exampleapp.common.utils.SessionConfiguration
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Status
import com.onlinepayments.client.kotlin.exampleapp.xml.R
import com.onlinepayments.client.kotlin.exampleapp.xml.databinding.FragmentPaymentConfigurationBinding
import com.onlinepayments.client.kotlin.exampleapp.xml.extensions.deepForEach
import com.onlinepayments.client.kotlin.exampleapp.xml.extensions.getStringFromClipboard
import com.onlinepayments.client.kotlin.exampleapp.xml.extensions.hideKeyboard
import com.onlinepayments.sdk.client.android.model.AmountOfMoney
import com.onlinepayments.sdk.client.android.model.PaymentContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Currency
import java.util.Locale

class PaymentConfigurationFragment : Fragment() {

    private var _binding: FragmentPaymentConfigurationBinding? = null
    private val binding get() = _binding!!

    private lateinit var configurationInputFields: List<Pair<TextInputLayout, TextInputEditText>>

    private val paymentSharedViewModel: PaymentSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPaymentConfigurationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resetInputData()
        initLayout()
        loadConfigurationFieldsFromDataStore()
        initInputFieldsDrawableEndClickListeners()
        observePaymentSharedViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        paymentSharedViewModel.activePaymentScreen.value = PaymentScreen.CONFIGURATION
    }

    private fun resetInputData() {
        paymentSharedViewModel.globalErrorMessage.value = null
        paymentSharedViewModel.paymentProductsStatus.value = null
    }

    private fun initLayout() {
        configurationInputFields = listOf(
            Pair(binding.tilPaymentConfigurationClientSessionId, binding.etPaymentConfigurationClientSessionId),
            Pair(binding.tilPaymentConfigurationCustomerId, binding.etPaymentConfigurationCustomerId),
            Pair(binding.tilPaymentConfigurationClientApiUrl, binding.etPaymentConfigurationClientApiUrl),
            Pair(binding.tilPaymentConfigurationAssetsUrl, binding.etPaymentConfigurationAssetsUrl),
            Pair(binding.tilPaymentConfigurationAmount, binding.etPaymentConfigurationAmount),
            Pair(binding.tilPaymentConfigurationCountryCode, binding.etPaymentConfigurationCountryCode),
            Pair(binding.tilPaymentConfigurationCurrencyCode, binding.etPaymentConfigurationCurrencyCode),
            Pair(binding.tilPaymentConfigurationMerchantId, binding.etPaymentConfigurationMerchantId),
            Pair(binding.tilPaymentConfigurationMerchantName, binding.etPaymentConfigurationMerchantName))

        binding.btnPaymentConfigurationProceedToCheckout.loadingButtonMaterialButton.setOnClickListener {
            view?.clearFocus()
            requireContext().hideKeyboard(it)
            validatePaymentConfiguration()
        }
        binding.btnPaymentConfigurationClientSessionJsonResponse.setOnClickListener {
            view?.clearFocus()
            requireContext().hideKeyboard(it)
            parseJsonDataFromClipboard()
        }

        // Prefill country and currency
        val currentLocale = Locale.getDefault()
        binding.apply {
            etPaymentConfigurationCountryCode.setText(currentLocale.country)
            etPaymentConfigurationCurrencyCode.setText(
                Currency.getInstance(currentLocale)
                .toString())
            cbPaymentConfigurationGooglePay.setOnCheckedChangeListener { _, isChecked ->
                clPaymentConfigurationGooglePayInputContainer.visibility =
                    if (isChecked) View.VISIBLE else View.GONE
            }
        }

        initFieldsRemoveErrorAfterTextChange()

    }

    private fun initInputFieldsDrawableEndClickListeners() {
        binding.apply {
            tilPaymentConfigurationClientSessionId.setEndIconOnClickListener {
                showInputFieldExplanationTextBottomSheetDialog(
                    getString(string.payment_configuration_client_session_id_helper_text)
                )
            }

            tilPaymentConfigurationCustomerId.setEndIconOnClickListener {
                showInputFieldExplanationTextBottomSheetDialog(
                    getString(string.payment_configuration_customer_id_helper_text)
                )
            }

            tilPaymentConfigurationClientApiUrl.setEndIconOnClickListener {
                showInputFieldExplanationTextBottomSheetDialog(
                    getString(string.payment_configuration_clientApiUrl_helper_text)
                )
            }

            tilPaymentConfigurationAssetsUrl.setEndIconOnClickListener {
                showInputFieldExplanationTextBottomSheetDialog(
                    getString(string.payment_configuration_assetsUrl_helper_text)
                )
            }

            tilPaymentConfigurationAmount.setEndIconOnClickListener {
                showInputFieldExplanationTextBottomSheetDialog(
                    getString(string.payment_configuration_amount_helper_text)
                )
            }

            tilPaymentConfigurationCountryCode.setEndIconOnClickListener {
                showInputFieldExplanationTextBottomSheetDialog(
                    getString(string.payment_configuration_country_code_helper_text)
                )
            }

            tilPaymentConfigurationCurrencyCode.setEndIconOnClickListener {
                showInputFieldExplanationTextBottomSheetDialog(
                    getString(string.payment_configuration_currency_code_helper_text)
                )
            }

            tilPaymentConfigurationMerchantId.setEndIconOnClickListener {
                showInputFieldExplanationTextBottomSheetDialog(
                    getString(string.payment_configuration_merchant_name_helper_text)
                )
            }

            tilPaymentConfigurationMerchantName.setEndIconOnClickListener {
                showInputFieldExplanationTextBottomSheetDialog(
                    getString(string.payment_configuration_merchant_name_helper_text)
                )
            }

            ivPaymentConfigurationRecurringPaymentHelperIcon.setOnClickListener {
                showInputFieldExplanationTextBottomSheetDialog(
                    getString(string.payment_configuration_recurring_payment_helper_text)
                )
            }

            ivPaymentConfigurationGroupPaymentProductsHelperIcon.setOnClickListener {
                showInputFieldExplanationTextBottomSheetDialog(
                    getString(string.payment_configuration_group_payment_products_helper_text)
                )
            }
        }
    }

    private fun loadConfigurationFieldsFromDataStore() {
        CoroutineScope(Dispatchers.IO).launch {
            paymentSharedViewModel.loadFromDataStore()
                .catch { exception ->
                    // An error occurred when reading the input from DataStore
                    Log.e(javaClass.name, exception.toString())
                }
                .collect { configurationFields ->
                    prefillPaymentConfigurationFields(configurationFields)
                }
        }
    }

    private suspend fun prefillPaymentConfigurationFields(
        configurationFields: Map<String, String>
    ) = withContext(Dispatchers.Main) {
        binding.apply {
            etPaymentConfigurationClientSessionId.setText(
                configurationFields[PaymentSharedViewModel.CLIENT_SESSION_ID_KEY.name] ?: ""
            )
            etPaymentConfigurationCustomerId.setText(
                configurationFields[PaymentSharedViewModel.CUSTOMER_ID_KEY.name] ?: ""
            )
            etPaymentConfigurationClientApiUrl.setText(
                configurationFields[PaymentSharedViewModel.CLIENT_API_URL_KEY.name] ?: ""
            )
            etPaymentConfigurationAssetsUrl.setText(
                configurationFields[PaymentSharedViewModel.ASSET_URL_KEY.name] ?: ""
            )
            etPaymentConfigurationAmount.setText(
                configurationFields[PaymentSharedViewModel.AMOUNT_KEY.name] ?: ""
            )
            etPaymentConfigurationCountryCode.setText(
                configurationFields[PaymentSharedViewModel.COUNTRY_CODE_KEY.name] ?: ""
            )
            etPaymentConfigurationCurrencyCode.setText(
                configurationFields[PaymentSharedViewModel.CURRENCY_CODE_KEY.name] ?: ""
            )
        }
    }

    private fun validatePaymentConfiguration() {
        var isFormValid = true
        configurationInputFields.forEach {
            isFormValid = isInputFieldValid(it)
        }

        if (isFormValid){
            configureSDK()
        }
    }

    private fun isInputFieldValid(textInput: Pair<TextInputLayout, TextInputEditText>): Boolean {
        if (
            textInput.second.text?.toString()?.isBlank() == true &&
            textInput.first.id != binding.tilPaymentConfigurationMerchantId.id &&
            textInput.first.id != binding.tilPaymentConfigurationMerchantName.id
        ) {
            textInput.first.error =
                getString(string.payment_configuration_field_not_valid_error)
            return false
        }

        if (binding.cbPaymentConfigurationGooglePay.isChecked) {
            if (
                textInput.first.id == binding.tilPaymentConfigurationMerchantId.id ||
                textInput.first.id == binding.tilPaymentConfigurationMerchantName.id
            ) {
                if (textInput.second.text?.isBlank() == true) {
                    textInput.first.error = getString(string.payment_configuration_field_not_valid_error)
                    return false
                }
            }
        }

        return true
    }

    private fun configureSDK() {
        val amount = try {
            binding.etPaymentConfigurationAmount.text.toString().toLong()
        } catch (e: NumberFormatException) {
            0
        }

        if (binding.cbPaymentConfigurationGooglePay.isChecked){
            paymentSharedViewModel.googlePayConfiguration =
                GooglePayConfiguration(
                    true,
                    binding.etPaymentConfigurationMerchantId.text.toString(),
                    binding.etPaymentConfigurationMerchantName.text.toString()
                )
        }

        paymentSharedViewModel.setPaymentContext(
            PaymentContext(
                AmountOfMoney(
                    amount, binding.etPaymentConfigurationCurrencyCode.text.toString()
                ),
                binding.etPaymentConfigurationCountryCode.text.toString(),
                binding.cbPaymentConfigurationRecurringPayment.isChecked
            )
        )

        paymentSharedViewModel.initializeSession(
            binding.etPaymentConfigurationClientSessionId.text.toString(),
            binding.etPaymentConfigurationCustomerId.text.toString(),
            binding.etPaymentConfigurationClientApiUrl.text.toString(),
            binding.etPaymentConfigurationAssetsUrl.text.toString(),
            binding.cbEnvironmentIsProduction.isChecked,
            Constants.APPLICATION_IDENTIFIER,
            BuildConfig.LOGGING_ENABLED,
            binding.cbPaymentConfigurationGroupPaymentProducts.isChecked
        )
    }

    private fun observePaymentSharedViewModel() {
        paymentSharedViewModel.paymentProductsStatus.observe(viewLifecycleOwner) { paymentProductStatus ->
            when (paymentProductStatus) {
                is Status.ApiError -> {
                    binding.clPaymentConfigurationInputForm.deepForEach { isEnabled = true }
                    binding.btnPaymentConfigurationProceedToCheckout.hideLoadingIndicator()
                    paymentSharedViewModel.globalErrorMessage.value =
                        paymentProductStatus.apiError.message
                }
                is Status.Loading -> {
                    binding.clPaymentConfigurationInputForm.deepForEach { isEnabled = false }
                    binding.btnPaymentConfigurationProceedToCheckout.showLoadingIndicator()
                }
                is Status.Success -> {
                    findNavController().navigate(
                        PaymentConfigurationFragmentDirections.navigateToPaymentProductFragment()
                    )
                }
                is Status.Failed -> {
                    binding.clPaymentConfigurationInputForm.deepForEach { isEnabled = true }
                    binding.btnPaymentConfigurationProceedToCheckout.hideLoadingIndicator()
                    paymentSharedViewModel.globalErrorMessage.value =
                        paymentProductStatus.throwable.message
                }
                Status.None, null -> {
                    // Init status; nothing to do here
                }
            }
        }
    }

    private fun parseJsonDataFromClipboard() {
        val jsonString = context?.getStringFromClipboard()
        try {
            Gson().fromJson(jsonString, SessionConfiguration::class.java).apply {
                binding.etPaymentConfigurationClientSessionId.setText(this.clientSessionId)
                binding.etPaymentConfigurationCustomerId.setText(this.customerId)
                binding.etPaymentConfigurationClientApiUrl.setText(this.clientApiUrl)
                binding.etPaymentConfigurationAssetsUrl.setText(this.assetUrl)
            }
        } catch (exception: JsonSyntaxException) {
            paymentSharedViewModel.globalErrorMessage.value = "Json data from clipboard can't be parsed."
            Log.e(javaClass.name, exception.toString())
        }
    }

    private fun initFieldsRemoveErrorAfterTextChange() {
        configurationInputFields.forEach { field ->
            field.second.doAfterTextChanged { editable ->
                if (editable?.isNotBlank() == true) {
                    field.first.isErrorEnabled = false
                    field.first.error = null
                    return@doAfterTextChanged
                }
            }
        }
    }

    private fun showInputFieldExplanationTextBottomSheetDialog(explanationText: String) {
        BottomSheetDialog(requireContext()).apply {
            setContentView(R.layout.bottomsheet_information)
            findViewById<TextView>(R.id.tvInformationText)?.apply {
                text = explanationText
            }
        }.show()
    }
}
