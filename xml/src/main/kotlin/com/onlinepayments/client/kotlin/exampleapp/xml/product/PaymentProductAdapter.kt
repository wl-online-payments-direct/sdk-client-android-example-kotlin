/**
 * Copyright © 2024 Global Collect Services. All rights reserved.
 */

package com.onlinepayments.client.kotlin.exampleapp.xml.product

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onlinepayments.client.kotlin.exampleapp.common.utils.Constants
import com.onlinepayments.client.kotlin.exampleapp.xml.databinding.ListitemPaymentProductBinding
import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFile
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItem
import com.squareup.picasso.Picasso

class PaymentProductAdapter(
    val onBasicPaymentItemClicked: ((BasicPaymentItem) -> Unit),
    val onAccountOnFileClicked: ((AccountOnFile) -> Unit)

) : RecyclerView.Adapter<PaymentProductAdapter.PaymentProductViewHolder>() {

    var paymentProducts: List<Any> = emptyList()
        set(value) {
            field = value
            notifyItemRangeChanged(0, paymentProducts.size)
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PaymentProductViewHolder {
        val itemBinding = ListitemPaymentProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PaymentProductViewHolder(itemBinding)
    }

    override fun onBindViewHolder(
        holder: PaymentProductViewHolder,
        position: Int
    ) {
        holder.setIsRecyclable(false)
        when (val product = paymentProducts[position]) {
            is BasicPaymentItem -> {
                holder.bindBasicPaymentItem(product)
            }

            is AccountOnFile -> {
                val basicPaymentItem =
                    paymentProducts.find {
                        it is BasicPaymentItem && it.getId() == product.paymentProductId
                    } as? BasicPaymentItem

                holder.bindAccountOnFile(product, basicPaymentItem)
            }

            is String -> {
                holder.bindHeader(product)
            }
        }
    }

    override fun getItemCount(): Int {
        return paymentProducts.size
    }

    inner class PaymentProductViewHolder(
        private val binding: ListitemPaymentProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bindBasicPaymentItem(basicPaymentItem: BasicPaymentItem) {
            binding.root.run {
                Picasso.get()
                    .load(basicPaymentItem.getDisplayHintsList().firstOrNull()?.logoUrl)
                    .into(binding.ivPaymentProductLogo)
                binding.apply {
                    tvPaymentProductLabel.text =
                        basicPaymentItem.getDisplayHintsList().firstOrNull()?.label
                    paymentProductItem.setOnClickListener {
                        onBasicPaymentItemClicked.invoke(
                            basicPaymentItem
                        )
                    }
                }
            }
        }

        fun bindAccountOnFile(accountOnFile: AccountOnFile, basicPaymentItem: BasicPaymentItem?) {
            itemView.run {
                basicPaymentItem?.let {
                    Picasso.get()
                        .load(basicPaymentItem.getDisplayHintsList().firstOrNull()?.logoUrl)
                        .into(binding.ivPaymentProductLogo)
                }
                binding.apply {
                    tvPaymentProductLabel.text =
                        accountOnFile.displayHints?.labelTemplate?.firstOrNull()?.mask?.let { mask ->
                            accountOnFile.getMaskedValue(Constants.CARD_NUMBER, mask)
                        } ?: run {
                            accountOnFile.getLabel()
                        }
                    paymentProductItem.setOnClickListener {
                        onAccountOnFileClicked.invoke(
                            accountOnFile
                        )
                    }
                }
            }
        }

        fun bindHeader(headerLabel: String) {
            itemView.run {
                binding.apply {
                    paymentProductItem.visibility = View.GONE
                    tvPaymentProductHeader.visibility = View.VISIBLE
                    tvPaymentProductHeader.text = headerLabel
                }
            }
        }
    }
}
