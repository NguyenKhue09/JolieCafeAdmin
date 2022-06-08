package com.nt118.joliecafeadmin.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.ItemRvVoucherBinding
import com.nt118.joliecafeadmin.models.Voucher
import com.nt118.joliecafeadmin.ui.DeleteVoucherDialog
import com.nt118.joliecafeadmin.ui.activities.edit_voucher.EditVoucherActivity
import com.nt118.joliecafeadmin.util.Constants.Companion.VOUCHER_DATA
import com.nt118.joliecafeadmin.util.Constants.Companion.VOUCHER_FLAG
import com.nt118.joliecafeadmin.util.Constants.Companion.VOUCHER_FLAG_DETAIL
import com.nt118.joliecafeadmin.util.Constants.Companion.VOUCHER_FLAG_EDIT
import com.nt118.joliecafeadmin.util.DateTimeUtil
import com.nt118.joliecafeadmin.viewmodels.VouchersViewModel

class VoucherAdapter(
    private val dataset: MutableList<Voucher>,
    private val context: Context,
    private val viewModel: VouchersViewModel
) : RecyclerView.Adapter<VoucherAdapter.MyViewHolder>() {

    private var deleteIndex: Int? = null

    class MyViewHolder(
        var binding: ItemRvVoucherBinding,
        onItemClick: (Int) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                onItemClick(absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemRvVoucherBinding.inflate(layoutInflater, parent, false)
        return MyViewHolder(binding) { position ->
            val intent = Intent(context, EditVoucherActivity::class.java)
            intent.putExtra(VOUCHER_DATA, Gson().toJson(dataset[position]))
            intent.putExtra(VOUCHER_FLAG, VOUCHER_FLAG_DETAIL)
            context.startActivity(intent)
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.tvVoucherCode.text = dataset[position].code
        holder.binding.tvVoucherDate.text = context.getString(
            R.string.voucher_date,
            DateTimeUtil.dateFormatter(dataset[position].startDate),
            DateTimeUtil.dateFormatter(dataset[position].endDate)
        )
        holder.binding.tvVoucherDiscount.text = context.getString(
            R.string.number_with_percent,
            dataset[position].discountPercent.toString()
        )
        holder.binding.tvVoucherQuantity.text = dataset[position].quantity.toString()

        holder.binding.btnEdit.setOnClickListener {
            val intent = Intent(context, EditVoucherActivity::class.java)
            val voucherJson = Gson().toJson(dataset[position])
            intent.putExtra(VOUCHER_DATA, voucherJson)
            intent.putExtra(VOUCHER_FLAG, VOUCHER_FLAG_EDIT)
            startActivity(context, intent, null)
        }

        holder.binding.btnDelete.setOnClickListener {
            val dialog = DeleteVoucherDialog(context){
                deleteIndex = holder.bindingAdapterPosition
                viewModel.deleteVoucher(dataset[position].id)
            }
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }
    }

    override fun getItemCount() = dataset.size

    fun deleteItem() {
        if (deleteIndex != null) {
            dataset.removeAt(deleteIndex!!)
            notifyItemRemoved(deleteIndex!!)
            notifyItemRangeChanged(deleteIndex!!, dataset.size)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun fetchData(data: List<Voucher>) {
        dataset.clear()
        dataset.addAll(data)
        notifyDataSetChanged()
    }
}