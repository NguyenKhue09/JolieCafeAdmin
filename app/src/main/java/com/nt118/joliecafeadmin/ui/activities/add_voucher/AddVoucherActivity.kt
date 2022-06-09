package com.nt118.joliecafeadmin.ui.activities.add_voucher

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.asLiveData
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.ActivityAddVoucherBinding
import com.nt118.joliecafeadmin.util.*
import com.nt118.joliecafeadmin.util.Constants.Companion.SNACK_BAR_STATUS_ERROR
import com.nt118.joliecafeadmin.util.Constants.Companion.SNACK_BAR_STATUS_SUCCESS
import com.nt118.joliecafeadmin.viewmodels.AddVoucherViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.nt118.joliecafeadmin.util.Constants.Companion.listVoucherTypes
import com.nt118.joliecafeadmin.util.extenstions.setCustomBackground
import com.nt118.joliecafeadmin.util.extenstions.setIcon
import java.util.*

@AndroidEntryPoint
class AddVoucherActivity : AppCompatActivity() {

    private var _binding: ActivityAddVoucherBinding? = null
    private val binding get() = _binding!!

    private val addVoucherViewModel: AddVoucherViewModel by viewModels()
    private lateinit var networkListener: NetworkListener

    private val voucherFormState
        get() = addVoucherViewModel.voucherFormState

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
    private lateinit var btnAdd: MaterialButton
    private lateinit var btnCancel: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAddVoucherBinding.inflate(layoutInflater, FrameLayout(this), false)
        setContentView(binding.root)

        updateBackOnlineStatus()
        updateNetworkStatus()

        initViews()
        initListeners()
        initDropdownData()
        listenFieldChange()
        listenFormFieldError()
        observeResponse()
        observeNetworkMessage()

        setVoucherTypeDropdownMenu()
    }

    private fun observeNetworkMessage() {
        addVoucherViewModel.networkMessage.observe(this) { message ->
            if (!addVoucherViewModel.networkStatus) {
                showSnackBar(message = message, status = Constants.SNACK_BAR_STATUS_DISABLE, icon = R.drawable.ic_wifi_off)
            } else if (addVoucherViewModel.networkStatus) {
                if (addVoucherViewModel.backOnline) {
                    showSnackBar(message = message, status = SNACK_BAR_STATUS_SUCCESS, icon = R.drawable.ic_wifi)
                }
            }
        }
    }

    private fun updateBackOnlineStatus() {
        addVoucherViewModel.readBackOnline.asLiveData().observe(this) { status ->
            addVoucherViewModel.backOnline = status
        }
    }


    private fun updateNetworkStatus() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(this)
            .asLiveData().observe(this) { status ->
                addVoucherViewModel.networkStatus = status
                addVoucherViewModel.showNetworkStatus()
            }
    }

    private fun setVoucherTypeDropdownMenu() {
        val voucherTypesAdapter = ArrayAdapter(this, R.layout.drop_down_item, listVoucherTypes)
        etType.setAdapter(voucherTypesAdapter)
    }

    private fun observeResponse() {
        addVoucherViewModel.addVoucherResponse.asLiveData().observe(this) { response ->
            when (response) {
                is ApiResult.Loading -> {
                    progressIndicator.visibility = View.VISIBLE
                    btnAdd.isEnabled = false
                    btnCancel.isEnabled = false
                }
                is ApiResult.Success -> {
                    progressIndicator.visibility = View.GONE
                    showSnackBar("Voucher added successfully", status = SNACK_BAR_STATUS_SUCCESS, icon = R.drawable.ic_success)
                    btnAdd.isEnabled = true
                    btnCancel.isEnabled = true
                }
                is ApiResult.Error -> {
                    progressIndicator.visibility = View.GONE
                    showSnackBar("Add voucher failed: voucher code is already exist", status = SNACK_BAR_STATUS_ERROR, icon = R.drawable.ic_error)
                    btnAdd.isEnabled = true
                    btnCancel.isEnabled = true
                }
                else -> {
                    btnAdd.isEnabled = true
                    btnCancel.isEnabled = true
                }
            }
        }
    }


    private fun initDropdownData() {
        etType.setText(listVoucherTypes[0])
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
            addVoucherViewModel.onVoucherFormEvent(
                event = VoucherFormStateEvent.OnCodeChanged(
                    code = code.toString()
                )
            )
        }

        etDescription.addTextChangedListener { description ->
            addVoucherViewModel.onVoucherFormEvent(
                event = VoucherFormStateEvent.OnDescriptionChanged(
                    description = description.toString()
                )
            )
        }

        etStartDate.addTextChangedListener(DateTimeUtil.getTextChangeListener(etStartDate) {
            addVoucherViewModel.onVoucherFormEvent(
                event = VoucherFormStateEvent.OnStartDateChanged(
                    startDate = etStartDate.text.toString()
                )
            )
        })

        etEndDate.addTextChangedListener(DateTimeUtil.getTextChangeListener(etEndDate) {
            addVoucherViewModel.onVoucherFormEvent(
                event = VoucherFormStateEvent.OnEndDateChanged(
                    endDate = etEndDate.text.toString()
                )
            )
        })

        etType.addTextChangedListener { type ->
            addVoucherViewModel.onVoucherFormEvent(
                event = VoucherFormStateEvent.OnTypeChanged(
                    type = type.toString()
                )
            )
            if (type.toString() == listVoucherTypes[0]) {
                binding.tvCondition.visibility = View.VISIBLE
                etCondition.visibility = View.VISIBLE
            } else {
                binding.tvCondition.visibility = View.GONE
                etCondition.visibility = View.GONE
            }
        }

        etCondition.addTextChangedListener { condition ->
            addVoucherViewModel.onVoucherFormEvent(
                event = VoucherFormStateEvent.OnConditionChanged(
                    condition = condition.toString()
                )
            )
        }

        etDiscountPercent.addTextChangedListener { discountPercent ->
            addVoucherViewModel.onVoucherFormEvent(
                event = VoucherFormStateEvent.OnDiscountPercentChanged(
                    discountPercent = discountPercent.toString()
                )
            )
        }

        etQuantity.addTextChangedListener { quantity ->
            addVoucherViewModel.onVoucherFormEvent(
                event = VoucherFormStateEvent.OnQuantityChanged(
                    quantity = quantity.toString()
                )
            )
        }
    }

    private fun initListeners() {
        btnAdd.setOnClickListener {
            if (!addVoucherViewModel.networkStatus) {
                showSnackBar(
                    message = "No internet connection",
                    status = Constants.SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
                return@setOnClickListener
            }
            addVoucherViewModel.onVoucherFormEvent(
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
        btnAdd = binding.btnAdd
        btnCancel = binding.btnCancel
        progressIndicator = binding.progressIndicator
    }
}