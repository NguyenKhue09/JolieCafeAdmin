package com.nt118.joliecafeadmin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.ItemBestSellerBinding
import com.nt118.joliecafeadmin.models.BestSeller


class BestSellerAdapter(
    private val dataset: List<BestSeller>,
    private val context: Context
): RecyclerView.Adapter<BestSellerAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemBestSellerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBestSellerBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataset[position]

        holder.binding.tvProductName.text = item.product.name
        holder.binding.tvProductSold.text = context.getString( R.string.sold,item.sold)
        holder.binding.ivProductThumbnail.load(item.product.thumbnail) {
            crossfade(true)
            placeholder(R.drawable.placeholder_image)
        }

        when (position) {
            0 -> holder.binding.ivBadge.setImageResource(R.drawable.badge_1)
            1 -> holder.binding.ivBadge.setImageResource(R.drawable.badge_2)
            2 -> holder.binding.ivBadge.setImageResource(R.drawable.badge_3)
            else -> holder.binding.ivBadge.setImageResource(0)
        }
        holder.setIsRecyclable(false)
    }

    override fun getItemCount() = dataset.size
}