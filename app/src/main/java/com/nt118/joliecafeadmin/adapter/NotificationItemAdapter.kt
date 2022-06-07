package com.nt118.joliecafeadmin.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.NotificationItemLayoutBinding
import com.nt118.joliecafeadmin.databinding.ProductItemRowLayoutBinding
import com.nt118.joliecafeadmin.models.Notification
import com.nt118.joliecafeadmin.models.Product
import com.nt118.joliecafeadmin.ui.activities.notifications.NotificationActivity
import com.nt118.joliecafeadmin.ui.activities.notifications.NotificationsActivity
import com.nt118.joliecafeadmin.ui.fragments.products.ProductsFragment
import com.nt118.joliecafeadmin.util.Constants.Companion.LOCAL_TIME_FORMAT
import com.nt118.joliecafeadmin.util.extenstions.formatTo
import com.nt118.joliecafeadmin.util.extenstions.toDate

class NotificationItemAdapter(
    private val notificationActivity: NotificationsActivity,
    diffUtil: DiffUtil.ItemCallback<Notification>,
    private val onNotificationClicked: (String) -> Unit,
    private val onEditNotificationClicked: (String) -> Unit,
) : PagingDataAdapter<Notification, NotificationItemAdapter.MyViewHolder>(diffCallback = diffUtil) {

    class MyViewHolder(var binding: NotificationItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = NotificationItemLayoutBinding.inflate(layoutInflater, parent, false)
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
        val notification = getItem(position)

        notification?.let {
            holder.binding.tvNotificationTitle.text = notification.title
            holder.binding.tvNotificationBody.text = notification.message
            holder.binding.tvUpdateStatus.text =
                notificationActivity.getString(R.string.last_update, notification.updatedAt.toDate()?.formatTo(LOCAL_TIME_FORMAT))
            holder.binding.tvCreateStatus.text =
                notificationActivity.getString(R.string.create_at, notification.createdAt.toDate()?.formatTo(LOCAL_TIME_FORMAT))

            holder.binding.cardNotification.setOnClickListener {
                onNotificationClicked(notification.id)
            }
            holder.binding.btnEdit.setOnClickListener {
                onEditNotificationClicked(notification.id)
            }
        }
    }

}