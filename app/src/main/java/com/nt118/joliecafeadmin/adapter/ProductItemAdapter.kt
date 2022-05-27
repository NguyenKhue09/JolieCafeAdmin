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
    diffUtil: DiffUtil.ItemCallback<Product>
) : PagingDataAdapter<Product, ProductItemAdapter.MyViewHolder>(diffCallback = diffUtil) {

    class MyViewHolder(var binding: ProductItemRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ProductItemRowLayoutBinding.inflate(layoutInflater)
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
//                val intent = Intent(prodcutFragment.context, DetailActivity::class.java)
//                prodcutFragment.context?.startActivity(intent)
            }

            holder.binding.btnEditProduct.setOnClickListener {
            }

            holder.binding.itemImg.load(product.thumbnail) {
                crossfade(600)
                error(R.drawable.image_logo)
            }

            holder.binding.productName.text = product.name

            productFragment.context?.let { context ->
                holder.binding.itemPrice.text = context.getString(R.string.product_price, product.originPrice.toString())
            }
        }
    }

}