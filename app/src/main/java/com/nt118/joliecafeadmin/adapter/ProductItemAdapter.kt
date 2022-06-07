package com.nt118.joliecafeadmin.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.ProductItemRowLayoutBinding
import com.nt118.joliecafeadmin.models.Product
import com.nt118.joliecafeadmin.ui.fragments.products.ProductsFragment

class ProductItemAdapter(
    private val productFragment: ProductsFragment,
    diffUtil: DiffUtil.ItemCallback<Product>,
    private val onProductClicked: (String) -> Unit,
    private val onEditProductClicked: (String) -> Unit,
    private val onNotificationClicked: (String, String, String) -> Unit
) : PagingDataAdapter<Product, ProductItemAdapter.MyViewHolder>(diffCallback = diffUtil) {

    class MyViewHolder(var binding: ProductItemRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ProductItemRowLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val product = getItem(position)

        product?.let {

            holder.binding.itemCard.setOnClickListener {
                onProductClicked(product.id)
            }

            holder.binding.btnEditProduct.setOnClickListener {
                onEditProductClicked(product.id)
            }

            holder.binding.btnProductNotice.setOnClickListener {
                onNotificationClicked(product.id, product.name, product.thumbnail)
            }

            holder.binding.itemImg.load(product.thumbnail) {
                crossfade(600)
                error(R.drawable.image_logo)
                placeholder(R.drawable.image_logo)
            }

            holder.binding.productName.text = product.name

            holder.binding.productCategory.text = product.type

            productFragment.context?.let { context ->
                holder.binding.itemPrice.text = context.getString(R.string.product_price, product.originPrice.toString())
            }
        }
    }

}