package com.nt118.joliecafeadmin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.ProductItemInBillLayoutBinding
import com.nt118.joliecafeadmin.models.BillProduct
import com.nt118.joliecafeadmin.util.extenstions.ItemDiffUtil
import java.text.NumberFormat
import java.util.*

class BillProductAdapter(
    val context: Context
) : RecyclerView.Adapter<BillProductAdapter.MyViewHolder>() {

    var products = emptyList<BillProduct>()

    class MyViewHolder(var binding: ProductItemInBillLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ProductItemInBillLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val product = products[position]
        holder.binding.itemImg.load(
            data = product.product.thumbnail
        ) {
            crossfade(600)
            error(R.drawable.placeholder_image)
            placeholder(R.drawable.placeholder_image)
        }
        holder.binding.oderItemPrice.text = context.getString(
            R.string.product_price,
            NumberFormat.getNumberInstance(Locale.US).format(product.price)
        )
        holder.binding.oderItemQuantityNumber.text = product.quantity.toString()
        holder.binding.tvOrderItemName.text = product.product.name
    }

    override fun getItemCount(): Int {
        return products.size
    }

    fun setData(newData: List<BillProduct>) {
        val recipesDiffUtil = ItemDiffUtil(products, newData)
        val diffUtilResult = DiffUtil.calculateDiff(recipesDiffUtil)
        products = newData
        diffUtilResult.dispatchUpdatesTo(this)
    }
}