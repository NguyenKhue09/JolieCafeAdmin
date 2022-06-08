package com.nt118.joliecafeadmin.ui.activities.edit_voucher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.asLiveData
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.ActivityEditVoucherBinding
import com.nt118.joliecafeadmin.models.Voucher
import com.nt118.joliecafeadmin.util.*
import com.nt118.joliecafeadmin.util.Constants.Companion.VOUCHER_DATA
import com.nt118.joliecafeadmin.util.extenstions.setCustomBackground
import com.nt118.joliecafeadmin.util.extenstions.setIcon
import com.nt118.joliecafeadmin.viewmodels.EditVoucherViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditVoucherActivity : AppCompatActivity() {

    private var _binding: ActivityEditVoucherBinding? = null
    private val binding get() = _binding!!

    private val editVoucherViewModel: EditVoucherViewModel by viewModels()
    private lateinit var networkListener: NetworkListener

    private val voucherFormState
        get() = editVoucherViewModel.voucherFormState

    // Views
    private lateinit var etCode: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etStartDate: TextInputEditText
    private lateinit var etEndDate: TextInputEditText
    private lateinit var etType: AutoCompleteTextView
    private lateinit var etCondition: TextInputEditText
    private lateinit var etDiscountPercent: TextInputEditText
    private lateinit var etQuantity: TextInputEditText
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityEditVoucherBinding.inflate(layoutInflater, FrameLayout(this), false)
        setContentView(binding.root)

        updateBackOnlineStatus()
        updateNetworkStatus()

        initViews()
        initListeners()
        initDropdownData()
        listenFieldChange()
        listenFormFieldError()
        fillVoucherData()
        observeResponse()
        observeNetworkMessage()

        setVoucherTypeDropdownMenu()
    }

    private fun fillVoucherData() {
        val voucherJson = intent.getStringExtra(VOUCHER_DATA)
        val voucher = Gson().fromJson(voucherJson, Voucher::class.java)

        editVoucherViewModel.voucherId = voucher.id

        etCode.setText(voucher.code)
        etDescription.setText(voucher.description)
        etStartDate.setText(DateTimeUtil.dateToString(voucher.startDate))
        etEndDate.setText(DateTimeUtil.dateToString(voucher.endDate))
        etType.setText(voucher.type)
        etCondition.setText(voucher.condition.toString())
        etDiscountPercent.setText(voucher.discountPercent.toString())
        etQuantity.setText(voucher.quantity.toString())
    }

    private fun observeNetworkMessage() {
        editVoucherViewModel.networkMessage.observe(this) { message ->
            if (!editVoucherViewModel.networkStatus) {
                showSnackBar(message = message, status = Constants.SNACK_BAR_STATUS_DISABLE, icon = R.drawable.ic_wifi_off)
            } else if (editVoucherViewModel.networkStatus) {
                if (editVoucherViewModel.backOnline) {
                    showSnackBar(message = message, status = Constants.SNACK_BAR_STATUS_SUCCESS, icon = R.drawable.ic_wifi)
                }
            }
        }
    }

    private fun updateBackOnlineStatus() {
        editVoucherViewModel.readBackOnline.asLiveData().observe(this) { status ->
            editVoucherViewModel.backOnline = status
        }
    }


    private fun updateNetworkStatus() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(this)
            .asLiveData().observe(this) { status ->
                editVoucherViewModel.networkStatus = status
                editVoucherViewModel.showNetworkStatus()
            }
    }

    private fun setVoucherTypeDropdownMenu() {
        val voucherTypesAdapter = ArrayAdapter(this, R.layout.drop_down_item, Constants.listVoucherTypes)
        etType.setAdapter(voucherTypesAdapter)
    }

    private fun observeResponse() {
        editVoucherViewModel.updateVoucherResponse.asLiveData().observe(this) { response ->
            when (response) {
                is ApiResult.Loading -> {
                    progressIndicator.visibility = View.VISIBLE
                    btnSave.isEnabled = false
                    btnCancel.isEnabled = false
                }
                is ApiResult.Success -> {
                    progressIndicator.visibility = View.GONE
                    showSnackBar("Voucher updated successfully", status = Constants.SNACK_BAR_STATUS_SUCCESS, icon = R.drawable.ic_success)
                    btnSave.isEnabled = true
                    btnCancel.isEnabled = true
                }
                is ApiResult.Error -> {
                    progressIndicator.visibility = View.GONE
                    showSnackBar("Update voucher failed", status = Constants.SNACK_BAR_STATUS_ERROR, icon = R.drawable.ic_error)
                    btnSave.isEnabled = true
                    btnCancel.isEnabled = true
                }
                else -> {
                    btnSave.isEnabled = true
                    btnCancel.isEnabled = true
                }
            }
        }
    }


    private fun initDropdownData() {
        etType.setText(Constants.listVoucherTypes[0])
    }

    private fun listenFormFieldError() {
        voucherFormState.asLiveData().observe(this) { voucherFormState ->
            etCode.error = voucherFormState.voucherCodeError
            etDescription.error = voucherFormState.voucherDescriptionError
            etStartDate.error = voucherFormState.voucherStartDateError
            etEndDate.error = voucherFormState.voucherEndDateError
            etCondition.error = voucherFormState.voucherConditionError
            etDiscountPercent.error = voucherFormState.voucherDiscountPercentError
            etQuantity.error = voucherFormState.voucherQuantityError
        }
    }

    private fun listenFieldChange() {

        etCode.addTextChangedListener { code ->
            editVoucherViewModel.onVoucherFormEvent(
                event = VoucherFormStateEvent.OnCodeChanged(
                    code = code.toString()
                )
            )
        }

        etDescription.addTextChangedListener { description ->
            editVoucherViewModel.onVoucherFormEvent(
                event = VoucherFormStateEvent.OnDescriptionChanged(
                    description = description.toString()
                )
            )
        }

        etStartDate.addTextChangedListener(DateTimeUtil.getTextChangeListener(etStartDate) {
            editVoucherViewModel.onVoucherFormEvent(
                event = VoucherFormStateEvent.OnStartDateChanged(
                    startDate = etStartDate.text.toString()
                )
            )
        })

        etEndDate.addTextChangedListener(DateTimeUtil.getTextChangeListener(etEndDate) {
            editVoucherViewModel.onVoucherFormEvent(
                event = VoucherFormStateEvent.OnEndDateChanged(
                    endDate = etEndDate.text.toString()
                )
            )
        })

        etType.addTextChangedListener { type ->
            editVoucherViewModel.onVoucherFormEvent(
                event = VoucherFormStateEvent.OnTypeChanged(
                    type = type.toString()
                )
            )
        }

        etCondition.addTextChangedListener { condition ->
            editVoucherViewModel.onVoucherFormEvent(
                event = VoucherFormStateEvent.OnConditionChanged(
                    condition = condition.toString()
                )
            )
        }

        etDiscountPercent.addTextChangedListener { discountPercent ->
            editVoucherViewModel.onVoucherFormEvent(
                event = VoucherFormStateEvent.OnDiscountPercentChanged(
                    discountPercent = discountPercent.toString()
                )
            )
        }

        etQuantity.addTextChangedListener { quantity ->
            editVoucherViewModel.onVoucherFormEvent(
                event = VoucherFormStateEvent.OnQuantityChanged(
                    quantity = quantity.toString()
                )
            )
        }
    }

    private fun initListeners() {
        btnSave.setOnClickListener {
            if (!editVoucherViewModel.networkStatus) {
                showSnackBar(
                    message = "No internet connection",
                    status = Constants.SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
                return@setOnClickListener
            }
            editVoucherViewModel.onVoucherFormEvent(
                event = VoucherFormStateEvent.Submit
            )
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun showSnackBar(message: String, status: Int, icon: Int) {
        val drawable = getDrawable(icon)

        val snackBarContentColor = when(status) {
            Constants.SNACK_BAR_STATUS_SUCCESS -> R.color.text_color_2
            Constants.SNACK_BAR_STATUS_DISABLE -> R.color.dark_text_color
            Constants.SNACK_BAR_STATUS_ERROR -> R.color.error_color
            else -> R.color.text_color_2
        }


        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Ok") {
            }
            .setActionTextColor(ContextCompat.getColor(this, R.color.grey_primary))
            .setTextColor(ContextCompat.getColor(this, snackBarContentColor))
            .setIcon(
                drawable = drawable!!,
                colorTint = ContextCompat.getColor(this, snackBarContentColor),
                iconPadding = resources.getDimensionPixelOffset(R.dimen.small_margin)
            )
            .setCustomBackground(getDrawable(R.drawable.snackbar_normal_custom_bg)!!)

        snackBar.show()
    }

    private fun initViews() {
        etCode = binding.etCode
        etDescription = binding.etDescription
        etStartDate = binding.etStartDate
        etEndDate = binding.etEndDate
        etType = binding.etType
        etCondition = binding.etCondition
        etDiscountPercent = binding.etDiscountPercent
        etQuantity = binding.etQuantity
        btnSave = binding.btnSave
        btnCancel = binding.btnCancel
        progressIndicator = binding.progressIndicator
    }
}