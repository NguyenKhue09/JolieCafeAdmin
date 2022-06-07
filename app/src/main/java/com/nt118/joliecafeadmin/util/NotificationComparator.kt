package com.nt118.joliecafeadmin.util

import androidx.recyclerview.widget.DiffUtil
import com.nt118.joliecafeadmin.models.Notification
import com.nt118.joliecafeadmin.models.Product

object NotificationComparator: DiffUtil.ItemCallback<Notification>() {
    override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
        return oldItem == newItem
    }
}