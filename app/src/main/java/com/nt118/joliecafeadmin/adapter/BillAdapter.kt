package com.nt118.joliecafeadmin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.BillItemLayoutBinding
import com.nt118.joliecafeadmin.models.Bill
import com.nt118.joliecafeadmin.ui.activities.bill.BillsActivity
import com.nt118.joliecafeadmin.util.Constants.Companion.LOCAL_TIME_FORMAT
import com.nt118.joliecafeadmin.util.extenstions.formatTo
import com.nt118.joliecafeadmin.util.extenstions.toDate
import java.text.NumberFormat
import java.util.*


class BillAdapter(
    private val billsActivity: BillsActivity,
    private val onNotificationClicked: (String, String, String?) -> Unit,
    private val onBillUpdateClicked: (String, String, Boolean) -> Unit,
    diffUtil: DiffUtil.ItemCallback<Bill>
) : PagingDataAdapter<Bill, BillAdapter.MyViewHolder>(diffCallback = diffUtil) {

    private lateinit var orderListIdObserver: Observer<MutableList<String>>

    class MyViewHolder(var binding: BillItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = BillItemLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var isExpanded: Boolean
        val orderHistory = getItem(position)

        val orderItemInBillAdapter = BillProductAdapter(billsActivity.baseContext)
        val orderItemInBillRecyclerView = holder.binding.rvOrderItem
        orderItemInBillRecyclerView.layoutManager = LinearLayoutManager(billsActivity, LinearLayoutManager.VERTICAL, false)
        orderItemInBillRecyclerView.adapter = orderItemInBillAdapter

        val orderItem = holder.binding.cardOrderHistory
        val cardContent = holder.binding.orderItemBody
        val btnCollapse = holder.binding.btnCollapse

        orderHistory?.let { bill ->

            orderListIdObserver = Observer<MutableList<String>> { listId ->
                isExpanded =  listId.contains(bill.id)

                if (isExpanded) {
                    cardContent.visibility = View.VISIBLE
                    holder.binding.tvOrderNumberItem.visibility = View.GONE
                    holder.binding.tvOrderTempTotalCost.visibility = View.GONE
                    btnCollapse.setImageResource(R.drawable.ic_arrow_up)
                } else {
                    holder.binding.tvOrderNumberItem.visibility = View.VISIBLE
                    holder.binding.tvOrderTempTotalCost.visibility = View.VISIBLE
                    cardContent.visibility = View.GONE
                    btnCollapse.setImageResource(R.drawable.ic_arrow_down)
                }
                TransitionManager.beginDelayedTransition(
                    orderItem,
                    AutoTransition()
                )
            }

            billsActivity.billClickedList.observeForever(orderListIdObserver)


            btnCollapse.setOnClickListener {
                billsActivity.addNewBillToClickList(bill.id)
            }

            holder.binding.btnBillNotice.setOnClickListener {
                onNotificationClicked(bill.orderId, bill.userInfo, bill.token)
            }

            holder.binding.btnEditBill.setOnClickListener {
                onBillUpdateClicked(bill.id, bill.status, bill.paid)
            }

            orderItemInBillAdapter.setData(newData = bill.products)

            val totalItem = bill.products.sumOf { it.quantity }

            holder.binding.tvOrderNumberItem.text = billsActivity.getString(R.string.product_quantity, totalItem.toString())
            holder.binding.tvOrderDate.text = bill.orderDate.toDate()?.formatTo(dateFormat = LOCAL_TIME_FORMAT)
            holder.binding.tvUserName.text = bill.address.userName
            holder.binding.tvUserPhone.text = bill.address.phone
            holder.binding.tvUserAddress.text = bill.address.address
            holder.binding.tvSubtotalCost.text = billsActivity.getString(
                R.string.product_price,
                NumberFormat.getNumberInstance(Locale.US).format(bill.totalCost - bill.shippingFee)
            )
            holder.binding.tvShippingFee.text = billsActivity.getString(
                    R.string.product_price,
            NumberFormat.getNumberInstance(Locale.US).format(bill.shippingFee)
            )
            holder.binding.tvTotalCost.text = billsActivity.getString(
                R.string.product_price,
                NumberFormat.getNumberInstance(Locale.US).format(bill.totalCost)
            )
            holder.binding.tvOrderTempTotalCost.text = billsActivity.getString(
                R.string.product_price,
                NumberFormat.getNumberInstance(Locale.US).format(bill.totalCost)
            )

            holder.binding.tvDiscountCost.text = billsActivity.getString(
                R.string.product_price,
                NumberFormat.getNumberInstance(Locale.US).format(if(bill.discountCost == 0.0) 0 else -bill.discountCost)
            )

            holder.binding.tvOrderId.text = billsActivity.getString(R.string.order_id, bill.orderId)

            holder.binding.tvPaidStatus.text = if(bill.paid) "Paid" else "Not paid"
            holder.binding.tvPaidMethod.text = bill.paymentMethod
        }
    }

    fun removeOrderListIdObserver() {
        if(!::orderListIdObserver.isInitialized) billsActivity.billClickedList.removeObserver(orderListIdObserver)
    }
}
