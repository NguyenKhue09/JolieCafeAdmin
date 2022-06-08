package com.nt118.joliecafeadmin.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import com.google.android.material.button.MaterialButton
import com.nt118.joliecafeadmin.R

class DeleteVoucherDialog(context: Context, private val callback: () -> Unit): Dialog(context), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_delete_voucher)
        val btnCancel = findViewById<MaterialButton>(R.id.btn_cancel)
        val btnDelete = findViewById<MaterialButton>(R.id.btn_delete)
        btnCancel.setOnClickListener(this)
        btnDelete.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_cancel -> dismiss()
            R.id.btn_delete -> {
                callback.invoke()
                dismiss()
            }
        }
    }
}