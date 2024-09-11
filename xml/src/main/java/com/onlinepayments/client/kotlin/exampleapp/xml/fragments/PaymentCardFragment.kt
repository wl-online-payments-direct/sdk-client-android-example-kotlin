/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.xml.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.onlinepayments.client.kotlin.exampleapp.xml.databinding.FragmentPaymentCardBinding
import com.onlinepayments.client.kotlin.exampleapp.common.PaymentCardViewModel
import com.onlinepayments.client.kotlin.exampleapp.common.PaymentSharedViewModel
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants.CARD_HOLDER
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants.CARD_NUMBER
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants.EXPIRY_DATE
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants.SECURITY_NUMBER
import com.onlinepayments.client.kotlin.exampleapp.common.utils.FormValidationResult
import com.onlinepayments.client.kotlin.exampleapp.common.utils.PaymentCardUIState
import com.onlinepayments.client.kotlin.exampleapp.common.utils.PaymentScreen
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Status
import com.onlinepayments.client.kotlin.exampleapp.common.utils.StringProvider
import com.onlinepayments.client.kotlin.exampleapp.common.utils.ValidationErrorMessageMapper
import com.onlinepayments.client.kotlin.exampleapp.xml.R
import com.onlinepayments.client.kotlin.exampleapp.xml.card.CardFieldAfterTextChangedListener
import com.onlinepayments.client.kotlin.exampleapp.xml.card.PaymentCardField
import com.onlinepayments.client.kotlin.exampleapp.xml.extensions.deepForEach
import com.onlinepayments.client.kotlin.exampleapp.xml.extensions.hideKeyboard
import com.onlinepayments.sdk.client.android.model.PreparedPaymentRequest
import com.onlinepayments.sdk.client.android.model.iin.IinStatus
import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFile
import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFileAttribute
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField
import com.onlinepayments.sdk.client.android.model.paymentproduct.Tooltip
import com.onlinepayments.sdk.client.android.model.validation.ValidationErrorMessage
import com.squareup.picasso.Picasso

class PaymentCardFragment : Fragment() {
    private var _binding: FragmentPaymentCardBinding? = null
    private val binding get() = _binding!!

    private val paymentCardViewModel: PaymentCardViewModel by viewModels()
    private val paymentSharedViewModel: PaymentSharedViewModel by activityViewModels()

    private val implementedPaymentProductFields = mutableMapOf<String, PaymentCardField>()

    private var accountOnFilePaymentProductId: String? = null

    // Use this listener for all card fields to receive updates when a text field changes.
    // It is not necessary to create a separate listener for each field.
    private val cardFieldAfterTextChangedListener = object : CardFieldAfterTextChangedListener {
        override fun afterTextChanged(paymentProductField: PaymentProductField, value: String) {
            paymentCardViewModel.fieldChanged(paymentProductField, value)
        }

        override fun issuerIdentificationNumberChanged(currentCardNumber: String) {
            if (accountOnFilePaymentProductId == null) {
                paymentCardViewModel.getIINDetails(currentCardNumber)
            }
        }

        override fun onToolTipClicked(paymentProductField: PaymentProductField) {
            view?.clearFocus()
            showCardFieldTooltipBottomSheetDialog(paymentProductField.displayHints.tooltip)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaymentCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shouldShowFormLoadingIndicator(true)
        // bind a productFieldId to an cardField
        implementedPaymentProductFields.putAll(
            mapOf(
                CARD_NUMBER to binding.paymentCardFieldCardNumber,
                EXPIRY_DATE to binding.paymentCardFieldCardExpiryDate,
                SECURITY_NUMBER to binding.paymentCardFieldSecurityCode,
                CARD_HOLDER to binding.paymentCardFieldCardholderName
            )
        )

        if (paymentSharedViewModel.selectedPaymentProduct is AccountOnFile) {
            accountOnFilePaymentProductId =
                (paymentSharedViewModel.selectedPaymentProduct as AccountOnFile).paymentProductId
        }

        paymentCardViewModel.session = paymentSharedViewModel.session
        paymentCardViewModel.paymentContext = paymentSharedViewModel.paymentContext

        paymentCardViewModel.getPaymentProduct(
            paymentSharedViewModel.selectedPaymentProduct
        )

        initLayout()
        observePaymentProductFieldsUiState()
        observeFormValidationResult()
        observeEncryptedPaymentRequestStatus()
    }

    override fun onResume() {
        super.onResume()
        paymentSharedViewModel.activePaymentScreen.value = PaymentScreen.CARD
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initLayout() {
        binding.btnPaymentCardPayProduct.loadingButtonMaterialButton.setOnClickListener {
            view?.clearFocus()
            requireContext().hideKeyboard(it)
            paymentCardViewModel.onPayClicked()
        }

        if (accountOnFilePaymentProductId != null) {
            binding.cbPaymentCardSaveCard.visibility = View.GONE
        } else {
            binding.cbPaymentCardSaveCard.setOnCheckedChangeListener { _, isChecked ->
                paymentCardViewModel.saveCardForLater(isChecked)
            }
        }

        // init listener for issuer identification number changes
        binding.paymentCardFieldCardNumber.setIssuerIdentificationNumberListener()
    }

    private fun observePaymentProductFieldsUiState() {
        paymentCardViewModel.paymentProductFieldsUIState.observe(viewLifecycleOwner) { paymentCardUiState ->
            when (paymentCardUiState) {
                is PaymentCardUIState.ApiError -> {
                    shouldShowFormLoadingIndicator(false)
                    paymentSharedViewModel.globalErrorMessage.value = paymentCardUiState.apiError.message
                }
                is PaymentCardUIState.IinFailed -> {
                    when (paymentCardUiState.throwable.message) {
                        IinStatus.UNKNOWN.name -> binding.paymentCardFieldCardNumber.setError(
                            StringProvider.retrieveString(
                                "validationErrors_iin_label",
                                requireContext()
                            )
                        )
                        IinStatus.EXISTING_BUT_NOT_ALLOWED.name -> binding.paymentCardFieldCardNumber.setError(
                            StringProvider.retrieveString(
                                "validationErrors_allowedInContext_label",
                                requireContext()
                            )
                        )
                        else -> {
                            binding.paymentCardFieldCardNumber.hideAllToolTips()
                            paymentSharedViewModel.globalErrorMessage.value = paymentCardUiState.throwable.message
                        }
                    }
                }
                is PaymentCardUIState.Loading -> {
                    shouldShowFormLoadingIndicator(true)
                }
                is PaymentCardUIState.Success -> {
                    shouldShowFormLoadingIndicator(false)
                    updateCardFields(paymentCardUiState.paymentFields)

                    paymentCardUiState.accountOnFile?.let { accountOnFile ->
                        updateCardFieldByAccountOnFilePaymentProduct(accountOnFile)
                        binding.cbPaymentCardSaveCard.visibility = View.GONE
                    }

                    paymentCardUiState.logoUrl?.let { logoUrl ->
                        binding.paymentCardFieldCardNumber.setImage(logoUrl)
                    }


                }
                is PaymentCardUIState.Failed -> {
                    shouldShowFormLoadingIndicator(false)
                    paymentSharedViewModel.globalErrorMessage.value = paymentCardUiState.throwable.message
                }
                PaymentCardUIState.None -> {
                    // Init status; nothing to do here
                }
            }
        }
    }

    private fun observeFormValidationResult() {
        paymentCardViewModel.formValidationResult.observe(viewLifecycleOwner) { formValidationResult ->
            when (formValidationResult) {
                is FormValidationResult.Invalid -> {
                    binding.btnPaymentCardPayProduct.isButtonEnabled = false
                }
                is FormValidationResult.InvalidWithValidationErrorMessages -> {
                    binding.btnPaymentCardPayProduct.isButtonEnabled = false
                    setFieldErrors(formValidationResult.errorMessages)
                }
                is FormValidationResult.Valid -> {
                    binding.btnPaymentCardPayProduct.isButtonEnabled = true
                    setFieldErrors(emptyList())
                }
                FormValidationResult.NotValidated -> {
                    // No option, when form is invalid it will always come in the
                    // FormValidationResult.InvalidWithValidationErrorMessage case
                }
            }
        }
    }

    private fun observeEncryptedPaymentRequestStatus() {
        paymentCardViewModel.encryptedPaymentRequestStatus.observe(
            viewLifecycleOwner
        ) { encryptedPaymentRequestStatus ->
            when (encryptedPaymentRequestStatus) {
                is Status.ApiError -> {
                    paymentSharedViewModel.globalErrorMessage.value =
                        encryptedPaymentRequestStatus.apiError.message
                    binding.clPaymentCardInputForm.deepForEach { isEnabled = true }
                    binding.btnPaymentCardPayProduct.hideLoadingIndicator()
                }
                is Status.Loading -> {
                    binding.clPaymentCardInputForm.deepForEach { isEnabled = false }
                    binding.btnPaymentCardPayProduct.showLoadingIndicator()
                }
                is Status.Success -> {
                    val encryptedFieldsData =
                        (encryptedPaymentRequestStatus.data as PreparedPaymentRequest).encryptedFields
                    findNavController().navigate(
                        PaymentCardFragmentDirections.navigateToPaymentResultFragment(encryptedFieldsData)
                    )
                }
                is Status.Failed -> {
                    paymentSharedViewModel.globalErrorMessage.value = encryptedPaymentRequestStatus.throwable.message
                    binding.clPaymentCardInputForm.deepForEach { isEnabled = true }
                    binding.btnPaymentCardPayProduct.hideLoadingIndicator()
                }
                is Status.None -> {
                    // Init status; nothing to do here
                }
            }
        }
    }

    private fun shouldShowFormLoadingIndicator(showLoadingIndicator: Boolean) {
        if (showLoadingIndicator) {
            binding.clPaymentCardInputForm.visibility = View.GONE
            binding.pbPaymentCardLoadingIndicator.visibility = View.VISIBLE
        } else {
            binding.pbPaymentCardLoadingIndicator.visibility = View.GONE
            binding.clPaymentCardInputForm.visibility = View.VISIBLE
        }
    }

    private fun setFieldErrors(fieldErrors: List<ValidationErrorMessage>) {
        implementedPaymentProductFields.forEach { paymentProductsField ->
            paymentProductsField.value.hideError()
            fieldErrors.forEach { validationErrorMessage ->
                if (paymentProductsField.key == validationErrorMessage.paymentProductFieldId) {
                    paymentProductsField.value.setError(
                        ValidationErrorMessageMapper.mapValidationErrorMessageToString(
                            requireContext(),
                            validationErrorMessage
                        )
                    )
                }
            }
        }
    }

    private fun updateCardFields(paymentProductFields: List<PaymentProductField>) {
        paymentProductFields.forEach { paymentProductField ->
            updateCardFieldById(paymentProductField)
        }
    }

    private fun updateCardFieldById(paymentProductField: PaymentProductField) {
        implementedPaymentProductFields.forEach { implementedPaymentProductsField ->
            if (implementedPaymentProductsField.key == paymentProductField.id) {
                implementedPaymentProductsField.value.setPaymentProductField(
                    paymentSharedViewModel.selectedPaymentProductId,
                    paymentProductField,
                    cardFieldAfterTextChangedListener
                )

                paymentCardViewModel.updateValueInPaymentRequest(
                    paymentProductField,
                    implementedPaymentProductsField.value.getPaymentProductFieldValue()
                )
            }
        }
    }

    private fun updateCardFieldByAccountOnFilePaymentProduct(accountOnFile: AccountOnFile) {
        implementedPaymentProductFields.forEach { implementedPaymentProductField ->
            accountOnFile.accountOnFileAttributes.firstOrNull { it.key == implementedPaymentProductField.key }
                ?.let { attribute ->
                    if (!attribute.isEditingAllowed() || attribute.key == CARD_NUMBER) {
                        // CARD_NUMBER field should always be disabled for AccountOnFile
                        implementedPaymentProductField.value.removePaymentCardMaskTextWatcher()
                        implementedPaymentProductField.value.deepForEach { isEnabled = false }
                    }

                    setPaymentProductFieldValue(
                        implementedPaymentProductField,
                        accountOnFile,
                        attribute
                    )
                }
        }
    }

    private fun setPaymentProductFieldValue(
        paymentProductField: Map.Entry<String, PaymentCardField>,
        accountOnFile: AccountOnFile,
        attribute: AccountOnFileAttribute
    ) {
        when (attribute.key) {
            CARD_NUMBER -> {
                val formattedValue = accountOnFile.displayHints.labelTemplate[0].mask?.let { mask ->
                    accountOnFile.getMaskedValue(CARD_NUMBER, mask)
                } ?: run {
                    accountOnFile.label
                }

                paymentProductField.value.setPaymentProductFieldValue(
                    formattedValue
                )
            }
            EXPIRY_DATE -> {
                paymentProductField.value.setPaymentProductFieldValue(
                    attribute.value.replaceRange(2, 2, "/")
                )
            }
            else -> {
                paymentProductField.value.setPaymentProductFieldValue(
                    attribute.value
                )
            }
        }
    }

    private fun showCardFieldTooltipBottomSheetDialog(tooltip: Tooltip) {
        BottomSheetDialog(requireContext()).apply {
            setContentView(R.layout.bottomsheet_information)
            findViewById<TextView>(R.id.tvInformationText)?.apply {
                text = tooltip.label
            }
            val informationImage = findViewById<ImageView>(R.id.ivInformationImage)?.apply {
                visibility = View.VISIBLE
            }
            Picasso.get()
                .load(tooltip.imageURL)
                .into(informationImage)

        }.show()
    }
}
