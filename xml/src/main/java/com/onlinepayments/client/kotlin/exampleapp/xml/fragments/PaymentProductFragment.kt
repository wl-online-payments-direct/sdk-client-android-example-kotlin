/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.xml.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.onlinepayments.client.kotlin.exampleapp.common.PaymentSharedViewModel
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants
import com.onlinepayments.client.kotlin.exampleapp.common.utils.PaymentScreen
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Status
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Translator
import com.onlinepayments.client.kotlin.exampleapp.xml.R
import com.onlinepayments.client.kotlin.exampleapp.xml.databinding.FragmentPaymentProductBinding
import com.onlinepayments.client.kotlin.exampleapp.xml.product.PaymentProductAdapter
import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFile
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItem
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItems
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProduct

class PaymentProductFragment : Fragment() {
    private var _binding: FragmentPaymentProductBinding? = null
    private val binding get() = _binding!!

    private val paymentSharedViewModel: PaymentSharedViewModel by activityViewModels()

    private val paymentProductAdapter by lazy {
        PaymentProductAdapter(
            ::onPaymentProductClicked,
            ::onSavedPaymentProductClicked
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaymentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvPaymentProductItems.adapter = paymentProductAdapter
        observePaymentSharedViewModel()
    }

    override fun onResume() {
        super.onResume()
        paymentSharedViewModel.activePaymentScreen.value = PaymentScreen.PRODUCT
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observePaymentSharedViewModel() {
        paymentSharedViewModel.paymentProductsStatus.observe(viewLifecycleOwner) { paymentProductsStatus ->
            if (paymentProductsStatus is Status.Success) {
                val paymentProducts = mutableListOf<Any>()
                val basicPaymentItems = paymentProductsStatus.data as BasicPaymentItems
                if (basicPaymentItems.accountsOnFile.isNotEmpty()) {
                    paymentProducts.add(
                        Translator.translateString(
                            "gc.app.paymentProductSelection.accountsOnFileTitle",
                            requireContext()
                        )
                    )
                    paymentProducts.addAll(basicPaymentItems.accountsOnFile)
                    paymentProducts.add(
                        Translator.translateString(
                            "gc.app.paymentProductSelection.paymentProductsTitle",
                            requireContext()
                        )
                    )
                }
                paymentProducts.addAll(basicPaymentItems.basicPaymentItems)
                paymentProductAdapter.paymentProducts = paymentProducts.toList()
            }
        }
    }

    private fun onPaymentProductClicked(basicPaymentItem: BasicPaymentItem) {
        paymentSharedViewModel.selectedPaymentProduct = basicPaymentItem
        when (basicPaymentItem) {
            is BasicPaymentProduct -> {
                when {
                    basicPaymentItem.paymentMethod.equals(Constants.PAYMENT_METHOD_CARD, ignoreCase = true) -> {
                        findNavController().navigate(PaymentProductFragmentDirections.navigateToPaymentCardFragment())
                    }
                    basicPaymentItem.id.equals(Constants.GOOGLE_PAY_PRODUCT_ID) -> {
                        findNavController().navigate(PaymentProductFragmentDirections.navigateToGooglePayFragment())
                    }
                    else -> {
                        showPaymentProductNotImplementedBottomSheetDialog()
                    }
                }
            }
            else -> {
                showPaymentProductNotImplementedBottomSheetDialog()
            }
        }
    }

    private fun onSavedPaymentProductClicked(accountOnFile: AccountOnFile) {
        paymentSharedViewModel.selectedPaymentProduct = accountOnFile
        findNavController().navigate(PaymentProductFragmentDirections.navigateToPaymentCardFragment())
    }

    private fun showPaymentProductNotImplementedBottomSheetDialog() {
        BottomSheetDialog(requireContext()).apply {
            setContentView(R.layout.bottomsheet_information)
            findViewById<TextView>(R.id.tvInformationText)?.apply {
                text = Translator.translateString("gc.general.errors.productUnavailable", requireContext())
            }
        }.show()
    }
}
