package com.nt118.joliecafeadmin.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.ItemRvVoucherBinding
import com.nt118.joliecafeadmin.models.Voucher
import com.nt118.joliecafeadmin.util.DateTimeUtil

class VoucherAdapter(
    private val dataset: MutableList<Voucher>,
    private val context: Context
) : RecyclerView.Adapter<VoucherAdapter.MyViewHolder>() {
    class MyViewHolder(var binding: ItemRvVoucherBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRvVoucherBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
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
            holder.binding.voucher.visibility = View.GONE
            holder.binding.editVoucher.visibility = View.VISIBLE
        }

        holder.binding.btnSave.setOnClickListener {
            holder.binding.voucher.visibility = View.VISIBLE
            holder.binding.editVoucher.visibility = View.GONE
        }

        holder.binding.btnCancel.setOnClickListener {
            holder.binding.voucher.visibility = View.VISIBLE
            holder.binding.editVoucher.visibility = View.GONE
        }
    }

    override fun getItemCount() = dataset.size

    @SuppressLint("NotifyDataSetChanged")
    fun fetchData(data: List<Voucher>) {
        dataset.clear()
        dataset.addAll(data)
        notifyDataSetChanged()
    }
}