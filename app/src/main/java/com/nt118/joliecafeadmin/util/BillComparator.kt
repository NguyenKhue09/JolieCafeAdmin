package com.nt118.joliecafeadmin.util

import androidx.recyclerview.widget.DiffUtil
import com.nt118.joliecafeadmin.models.Bill
import com.nt118.joliecafeadmin.models.Product

object BillComparator: DiffUtil.ItemCallback<Bill>() {
    override fun areItemsTheSame(oldItem: Bill, newItem: Bill): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Bill, newItem: Bill): Boolean {
        return oldItem == newItem
    }
}