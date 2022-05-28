package com.nt118.joliecafeadmin.ui.activities.product_detail

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.navArgs
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.ActivityProductDetailBinding
import com.nt118.joliecafeadmin.util.Constants.Companion.END_DATE_DISCOUNT_TAG
import com.nt118.joliecafeadmin.util.Constants.Companion.START_DATE_DISCOUNT_TAG
import com.nt118.joliecafeadmin.util.Constants.Companion.listProductStatus
import com.nt118.joliecafeadmin.util.Constants.Companion.listProductTypes
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ProductDetailActivity : AppCompatActivity() {

    private var _binding: ActivityProductDetailBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<ProductDetailActivityArgs>()

    private lateinit var datePicker: MaterialDatePicker<Long>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpBackPress()

        if(args.isEdit) {
            enableViewToEditProduct()

            setProductTypeDropDownMenu()
            setProductStatusDropDownMenu()
            productStartDateDiscountOnclickListener()
            productEndDateDiscountOnclickListener()

            setupDatePicker()
            handleDatePickerEvent()
        }

        binding.switchProductDiscount.isChecked = false
        productDiscountSwitchButtonListener()
    }

    private fun enableViewToEditProduct() {
        binding.etProductNameLayout.isEnabled = true
        binding.etProductPriceLayout.isEnabled = true
        binding.etProductDescriptionLayout.isEnabled = true
        binding.etProductDiscountPercentLayout.isEnabled = true

        binding.etProductTypeLayout.isEnabled = true
        binding.etProductTypeLayout.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
        binding.etProductType.inputType = InputType.TYPE_NULL

        binding.etProductStatusLayout.isEnabled = true
        binding.etProductStatusLayout.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
        binding.etProductStatus.inputType = InputType.TYPE_NULL


        binding.etProductStartDateDiscountLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
        binding.etProductEndDateDiscountLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM

        binding.footerActionButtonContainer.visibility = View.VISIBLE
    }

    private fun productDiscountSwitchButtonListener() {
        binding.switchProductDiscount.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.productDiscountLayoutSession.visibility = View.VISIBLE
            } else {
                binding.productDiscountLayoutSession.visibility = View.GONE
            }
        }
    }

    private fun setUpBackPress() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setProductTypeDropDownMenu(){
        val productTypesAdapter = ArrayAdapter(this, R.layout.drop_down_item, listProductTypes)
        binding.etProductType.setAdapter(productTypesAdapter)
    }

    private fun setProductStatusDropDownMenu(){
        val productStatusAdapter = ArrayAdapter(this, R.layout.drop_down_item, listProductStatus)
        binding.etProductStatus.setAdapter(productStatusAdapter)
    }

    private fun setupDatePicker() {

        val today = MaterialDatePicker.todayInUtcMilliseconds()

        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .setStart(today)
                .build()

        datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select discount date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder)
                .build()

    }

    private fun handleDatePickerEvent() {
        datePicker.addOnPositiveButtonClickListener {
            val date = pairDateTime(it)
            val tag = datePicker.tag
            tag?.let {
                setDateDataToDateTimeInput(tag = tag, date = date)
            }

        }
        datePicker.addOnNegativeButtonClickListener {
            // Respond to negative button click.
        }
        datePicker.addOnCancelListener {
            // Respond to cancel button click.
        }
        datePicker.addOnDismissListener {
            // Respond to dismiss events.
        }
    }

    private fun setDateDataToDateTimeInput(tag: String, date: String) {
        if(tag == START_DATE_DISCOUNT_TAG) {
            binding.etProductStartDateDiscount.setText(date)
        } else if(tag == END_DATE_DISCOUNT_TAG) {
            binding.etProductEndDateDiscount.setText(date)
        }
    }

    private fun productStartDateDiscountOnclickListener() {
        binding.etProductStartDateDiscountLayout.setEndIconOnClickListener {
            datePicker.show(supportFragmentManager, START_DATE_DISCOUNT_TAG)
        }
    }

    private fun productEndDateDiscountOnclickListener() {
        binding.etProductEndDateDiscountLayout.setEndIconOnClickListener {
            datePicker.show(supportFragmentManager, END_DATE_DISCOUNT_TAG)
        }
    }

    private fun pairDateTime(time: Long): String {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.timeInMillis = time
        val format = SimpleDateFormat("dd/MM/yyyy")
        return format.format(calendar.time)
    }
}