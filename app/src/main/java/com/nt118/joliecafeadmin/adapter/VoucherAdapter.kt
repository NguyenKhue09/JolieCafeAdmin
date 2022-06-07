package com.nt118.joliecafeadmin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nt118.joliecafeadmin.databinding.ItemRvVoucherBinding

class VoucherAdapter(private val item : ArrayList<String>) : RecyclerView.Adapter<VoucherAdapter.MyViewHolder>() {
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
        holder.binding.tvQuantityVoucher.text = item[position]

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

    override fun getItemCount(): Int {
        return  item.size
    }
}