package com.nt118.joliecafeadmin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.ItemBestSellerBinding


class BestSellerAdapter: RecyclerView.Adapter<BestSellerAdapter.ViewHolder>() {
    class ViewHolder(val binding: ItemBestSellerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBestSellerBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (position) {
            0 -> holder.binding.ivBadge.setImageResource(R.drawable.badge_1)
            1 -> holder.binding.ivBadge.setImageResource(R.drawable.badge_2)
            2 -> holder.binding.ivBadge.setImageResource(R.drawable.badge_3)
            else -> holder.binding.ivBadge.setImageResource(0)
        }
        holder.setIsRecyclable(false)
    }

    override fun getItemCount() = 10
}