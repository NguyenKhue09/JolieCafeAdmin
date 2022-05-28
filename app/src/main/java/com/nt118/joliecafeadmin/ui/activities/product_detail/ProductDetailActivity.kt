package com.nt118.joliecafeadmin.ui.activities.product_detail

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
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
    private lateinit var datePicker: MaterialDatePicker<Long>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setProductTypeDropDownMenu()
        setProductStatusDropDownMenu()
        setUpBackPress()
        productStartDateDiscountOnclickListener()
        productEndDateDiscountOnclickListener()

        setupDatePicker()
        handleDatePickerEvent()
    }

    private fun setUpBackPress() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setProductTypeDropDownMenu(){
        val productTypesAdapter = ArrayAdapter(this, R.layout.drop_down_item, listProductTypes)
        binding.etProductType.setAdapter(productTypesAdapter)

//        binding?.typesFilterSpinner.onItemClickListener =
//            AdapterView.OnItemClickListener { parent, view, position, id->
//                // do something with the available information
//            }

        //binding.etProductTypeLayout.endIconMode = TextInputLayout.END_ICON_NONE
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