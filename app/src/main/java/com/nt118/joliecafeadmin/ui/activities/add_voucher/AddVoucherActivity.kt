package com.nt118.joliecafeadmin.ui.activities.add_voucher

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.nt118.joliecafeadmin.databinding.ActivityAddVoucherBinding
import com.nt118.joliecafeadmin.util.DateTimeUtil
import com.nt118.joliecafeadmin.util.NetworkListener
import com.nt118.joliecafeadmin.viewmodels.AddVoucherViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddVoucherActivity : AppCompatActivity() {

    private var _binding: ActivityAddVoucherBinding? = null
    private val binding get() = _binding!!

    private val addVoucherViewModel: AddVoucherViewModel by viewModels()
    private lateinit var networkListener: NetworkListener
    private lateinit var btnAdd: MaterialButton
    private lateinit var btnCancel: MaterialButton

    // Views
    private lateinit var etStartDate: TextInputEditText
    private lateinit var etEndDate: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAddVoucherBinding.inflate(layoutInflater, FrameLayout(this), false)
        setContentView(binding.root)

        initViews()
        initListeners()

    }

    private fun initListeners() {
        TODO("Not yet implemented")
    }

    private fun initViews() {
        etStartDate = binding.etStartDate
        etEndDate = binding.etEndDate
        btnAdd = binding.btnAdd
        btnCancel = binding.btnCancel

        etStartDate.addTextChangedListener(DateTimeUtil.getTextChangeListener(etStartDate))
        etEndDate.addTextChangedListener(DateTimeUtil.getTextChangeListener(etEndDate))
    }
}