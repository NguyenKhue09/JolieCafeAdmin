package com.nt118.joliecafeadmin.ui.activities.product_detail

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import coil.load
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.ActivityProductDetailBinding
import com.nt118.joliecafeadmin.models.Product
import com.nt118.joliecafeadmin.models.ProductFormState
import com.nt118.joliecafeadmin.util.ApiResult
import com.nt118.joliecafeadmin.util.Constants.Companion.END_DATE_DISCOUNT_TAG
import com.nt118.joliecafeadmin.util.Constants.Companion.LOCAL_TIME_FORMAT
import com.nt118.joliecafeadmin.util.Constants.Companion.SNACK_BAR_STATUS_DISABLE
import com.nt118.joliecafeadmin.util.Constants.Companion.SNACK_BAR_STATUS_ERROR
import com.nt118.joliecafeadmin.util.Constants.Companion.SNACK_BAR_STATUS_SUCCESS
import com.nt118.joliecafeadmin.util.Constants.Companion.START_DATE_DISCOUNT_TAG
import com.nt118.joliecafeadmin.util.Constants.Companion.listProductStatus
import com.nt118.joliecafeadmin.util.Constants.Companion.listProductTypes
import com.nt118.joliecafeadmin.util.NetworkListener
import com.nt118.joliecafeadmin.util.ProductFormStateEvent
import com.nt118.joliecafeadmin.util.extenstions.*
import com.nt118.joliecafeadmin.viewmodels.ProductDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ProductDetailActivity : AppCompatActivity() {

    private var _binding: ActivityProductDetailBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<ProductDetailActivityArgs>()

    private lateinit var datePicker: MaterialDatePicker<Long>

    private val productDetailActivityViewModel: ProductDetailViewModel by viewModels()

    private lateinit var networkListener: NetworkListener

    private lateinit var productFormState: MutableStateFlow<ProductFormState>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productFormState = productDetailActivityViewModel.productFormState

        setUpBackPress()

        updateBackOnlineStatus()
        updateNetworkStatus()

        getProductDataIfBackOnline()
        observerGetProductDetailResponse()
        observerNetworkMessage()

        if (args.isEdit) {
            enableViewToEditProduct()

            productStartDateDiscountOnclickListener()
            productEndDateDiscountOnclickListener()
            observerProductFormFieldChanged()
            observerProductFormFieldError()
            observerFooterActionClickEvent()
            observerProductFormValidateSubmitEvent()
            observerUpdateProductResponse()
            setupDatePicker()
            handleDatePickerEvent()
        }

    }

    private fun observerNetworkMessage() {
        productDetailActivityViewModel.networkMessage.observe(this) { message ->
            println(message)
            if (!productDetailActivityViewModel.networkStatus) {
                showSnackBar(message = message, status = SNACK_BAR_STATUS_DISABLE, icon = R.drawable.ic_wifi_off)
            } else if (productDetailActivityViewModel.networkStatus) {
                if (productDetailActivityViewModel.backOnline) {
                    showSnackBar(message = message, status = SNACK_BAR_STATUS_SUCCESS, icon = R.drawable.ic_wifi)
                }
            }
        }
    }

    private fun showSnackBar(message: String, status: Int, icon: Int) {
        val drawable = getDrawable(icon)

        val snackBarContentColor = when(status) {
            SNACK_BAR_STATUS_SUCCESS -> R.color.text_color_2
            SNACK_BAR_STATUS_DISABLE -> R.color.dark_text_color
            SNACK_BAR_STATUS_ERROR -> R.color.error_color
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

    private fun observerUpdateProductResponse() {
        productDetailActivityViewModel.productUpdateResponse.asLiveData().observe(this) { result ->
            when (result) {
                is ApiResult.Success -> {
                    binding.productDetailCircularProgressIndicator.visibility = View.GONE
                    showSnackBar(
                        message = "Update product date success",
                        status = SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_success
                    )
                }
                is ApiResult.Error -> {
                    binding.productDetailCircularProgressIndicator.visibility = View.GONE
                    showSnackBar(
                        message = "Update product data failed!",
                        status = SNACK_BAR_STATUS_ERROR,
                        icon = R.drawable.ic_error
                    )
                }
                is ApiResult.Loading -> {
                    binding.productDetailCircularProgressIndicator.visibility = View.VISIBLE
                }
                else -> {}
            }
        }
    }

    private fun observerProductFormValidateSubmitEvent() {
        lifecycleScope.launch {
            productDetailActivityViewModel.validationEvents.collect { event ->
                when (event) {
                    is ProductDetailViewModel.ValidationEvent.Success -> {

                        val newData = if (productFormState.value.isDiscount) {
                            mapOf(
                                "id" to args.productId,
                                "name" to productFormState.value.productName,
                                "status" to productFormState.value.productStatus,
                                "description" to productFormState.value.productDescription,
                                "originPrice" to productFormState.value.productPrice,
                                "type" to productFormState.value.productType,
                                "startDateDiscount" to productFormState.value.productStartDateDiscount,
                                "endDateDiscount" to productFormState.value.productEndDateDiscount,
                                "discountPercent" to productFormState.value.productDiscountPercent
                            )
                        } else {
                            mapOf(
                                "id" to args.productId,
                                "name" to productFormState.value.productName,
                                "status" to productFormState.value.productStatus,
                                "description" to productFormState.value.productDescription,
                                "originPrice" to productFormState.value.productPrice,
                                "type" to productFormState.value.productType,
                            )
                        }

                        productDetailActivityViewModel.updateProduct(
                            newData = newData
                        )
                    }
                }
            }
        }
    }

    private fun observerProductFormFieldChanged() {
        binding.etProductName.addTextChangedListener { name ->
            println(name.toString())
            productDetailActivityViewModel.onProductFormEvent(
                event = ProductFormStateEvent.OnProductNameChanged(
                    productName = name.toString()
                )
            )
        }
        binding.etProductType.addTextChangedListener { type ->
            println(type.toString())
            productDetailActivityViewModel.onProductFormEvent(
                event = ProductFormStateEvent.OnProductTypeChanged(
                    productType = type.toString()
                )
            )
        }
        binding.etProductPrice.addTextChangedListener { price ->
            println(price.toString())
            productDetailActivityViewModel.onProductFormEvent(
                event = ProductFormStateEvent.OnProductPriceChanged(
                    productPrice = price.toString()
                )
            )
        }
        binding.etProductStatus.addTextChangedListener { status ->
            println(status.toString())
            productDetailActivityViewModel.onProductFormEvent(
                event = ProductFormStateEvent.OnProductStatusChanged(
                    productStatus = status.toString()
                )
            )
        }
        binding.etProductDescription.addTextChangedListener { description ->
            println(description.toString())
            productDetailActivityViewModel.onProductFormEvent(
                event = ProductFormStateEvent.OnProductDescriptionChanged(
                    productDescription = description.toString()
                )
            )
        }

        productDiscountSwitchButtonListener()

        binding.etProductStartDateDiscount.addTextChangedListener { start ->
            println(start.toString())
            productDetailActivityViewModel.onProductFormEvent(
                event = ProductFormStateEvent.OnProductStartDateDiscountChanged(
                    productStartDateDiscount = start.toString()
                )
            )
        }
        binding.etProductEndDateDiscount.addTextChangedListener { end ->
            println(end.toString())
            productDetailActivityViewModel.onProductFormEvent(
                event = ProductFormStateEvent.OnProductEndDateDiscountChanged(
                    productEndDateDiscount = end.toString()
                )
            )
        }
        binding.etProductDiscountPercent.addTextChangedListener { percent ->
            println(percent.toString())
            productDetailActivityViewModel.onProductFormEvent(
                event = ProductFormStateEvent.OnProductDiscountPercentChanged(
                    productDiscountPercent = percent.toString()
                )
            )
        }

    }

    private fun observerProductFormFieldError() {
        productFormState.asLiveData().observe(this) { productFormState ->
            binding.etProductName.error = productFormState.productNameError
            binding.etProductPrice.error = productFormState.productPriceError
            binding.etProductDescription.error = productFormState.productDescriptionError
            binding.etProductEndDateDiscount.error = productFormState.productEndDateDiscountError
            binding.etProductDiscountPercent.error = productFormState.productDiscountPercentError
            if(productFormState.productEndDateDiscountError == null) {
                binding.etProductEndDateDiscountLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
            } else {
                binding.etProductEndDateDiscountLayout.endIconMode = TextInputLayout.GONE
            }
        }
    }

    private fun observerFooterActionClickEvent() {
        binding.footerActionButton.btnCancel.setOnClickListener {
            resetProductFormData()
        }
        binding.footerActionButton.btnSave.setOnClickListener {
            productDetailActivityViewModel.onProductFormEvent(event = ProductFormStateEvent.Submit)
        }
    }

    private fun observerGetProductDetailResponse() {
        lifecycleScope.launch {
            productDetailActivityViewModel.getProductDetailResponse.collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        binding.productDetailCircularProgressIndicator.visibility = View.VISIBLE
                        binding.productDetailSv.visibility = View.INVISIBLE
                    }
                    is ApiResult.Error -> {
                        binding.productDetailCircularProgressIndicator.visibility = View.GONE
                        Toast.makeText(
                            this@ProductDetailActivity,
                            result.message ?: "Unknown error!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is ApiResult.Success -> {
                        binding.productDetailCircularProgressIndicator.visibility = View.GONE
                        result.data?.let { product ->
                            binding.productDetailSv.visibility = View.VISIBLE
                            setProductData(product)
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }

    private fun resetProductFormData() {
        val product = productDetailActivityViewModel.getProductDetailResponse.value.data
        product?.let {
            setProductData(product = it)
        }
        productDetailActivityViewModel.cleanProductFormError()
    }

    private fun getProductDataIfBackOnline() {
        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(this@ProductDetailActivity)
                .collect { status ->
                    productDetailActivityViewModel.networkStatus = status
                    productDetailActivityViewModel.showNetworkStatus()
                    if (productDetailActivityViewModel.backOnline) {
                        productDetailActivityViewModel.getProductDetail(productId = args.productId)
                    }
                }
        }
    }

    private fun getProductData() {
        if (productDetailActivityViewModel.networkStatus) {
            lifecycleScope.launchWhenStarted {
                productDetailActivityViewModel.getProductDetail(productId = args.productId)
            }
        } else {
            productDetailActivityViewModel.showNetworkStatus()
        }
    }

    private fun setProductData(product: Product) {
        binding.productImg.load(product.thumbnail) {
            crossfade(600)
            error(R.drawable.image_logo)
            placeholder(R.drawable.image_logo)
        }
        binding.etProductName.setText(product.name)
        binding.etProductType.setText(product.type)
        binding.etProductPrice.setText(product.originPrice.toString())
        binding.etProductStatus.setText(product.status)
        binding.etProductDescription.setText(product.description)

        if (args.isEdit) {
            setProductTypeDropDownMenu()
            setProductStatusDropDownMenu()
        }

        if (product.startDateDiscount != null && product.endDateDiscount != null && product.discountPercent != null) {
            binding.switchProductDiscount.isChecked = true
            binding.productDiscountLayoutSession.visibility = View.VISIBLE

            val startDate = partUTCTimeToLocalTime(product.startDateDiscount)
            val endDate = partUTCTimeToLocalTime(product.endDateDiscount)

            binding.etProductStartDateDiscount.setText(startDate)
            binding.etProductEndDateDiscount.setText(endDate)
            binding.etProductDiscountPercent.setText(product.discountPercent.toString())
        } else {
            binding.switchProductDiscount.isChecked = false
            binding.productDiscountLayoutSession.visibility = View.GONE
        }
    }

    private fun setUpBackPress() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setProductTypeDropDownMenu() {
        val listTypes = listProductTypes.subList(1, listProductTypes.size - 1)
        val productTypesAdapter = ArrayAdapter(this, R.layout.drop_down_item, listTypes)
        binding.etProductType.setAdapter(productTypesAdapter)
    }

    private fun setProductStatusDropDownMenu() {
        val productStatusAdapter =
            ArrayAdapter(this, R.layout.drop_down_item, listProductStatus)
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

    private fun setDateDataToDateTimeInput(tag: String, date: String) {
        if (tag == START_DATE_DISCOUNT_TAG) {
            binding.etProductStartDateDiscount.setText(date)
        } else if (tag == END_DATE_DISCOUNT_TAG) {
            binding.etProductEndDateDiscount.setText(date)
        }
    }

    private fun updateBackOnlineStatus() {
        productDetailActivityViewModel.readBackOnline.asLiveData().observe(this) { status ->
            productDetailActivityViewModel.backOnline = status
        }
    }

    private fun updateNetworkStatus() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(this)
            .asLiveData().observeOnce(this) { status ->
                productDetailActivityViewModel.networkStatus = status
                getProductData()
            }
    }

    private fun enableViewToEditProduct() {
        binding.etProductNameLayout.isEnabled = true
        binding.etProductPriceLayout.isEnabled = true
        binding.etProductDescriptionLayout.isEnabled = true
        binding.etProductDiscountPercentLayout.isEnabled = true
        binding.etProductStartDateDiscountLayout.isEnabled = true
        binding.etProductEndDateDiscountLayout.isEnabled = true

        binding.etProductTypeLayout.isEnabled = true
        binding.etProductTypeLayout.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
        binding.etProductType.inputType = InputType.TYPE_NULL

        binding.etProductStatusLayout.isEnabled = true
        binding.etProductStatusLayout.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
        binding.etProductStatus.inputType = InputType.TYPE_NULL


        binding.etProductStartDateDiscountLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
        binding.etProductEndDateDiscountLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM

        binding.switchProductDiscount.isEnabled = true

        binding.footerActionButtonContainer.visibility = View.VISIBLE
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

    private fun productDiscountSwitchButtonListener() {
        binding.switchProductDiscount.setOnCheckedChangeListener { _, isChecked ->
            productDetailActivityViewModel.onProductFormEvent(
                event = ProductFormStateEvent.IsDiscountChanged(
                    isDiscount = isChecked
                )
            )
            if (isChecked) {
                binding.productDiscountLayoutSession.visibility = View.VISIBLE
            } else {
                binding.productDiscountLayoutSession.visibility = View.GONE
            }
        }
    }

    private fun productStartDateDiscountOnclickListener() {
        binding.etProductStartDateDiscountLayout.setEndIconOnClickListener {
            binding.etProductStartDateDiscount.requestFocus()
            //datePicker.show(supportFragmentManager, START_DATE_DISCOUNT_TAG)
        }
        binding.etProductStartDateDiscount.setOnFocusChangeListener { _, isFocus ->
            if (isFocus) {
                datePicker.show(supportFragmentManager, START_DATE_DISCOUNT_TAG)
            }
        }
        binding.etProductStartDateDiscount.setOnClickListener {
            datePicker.show(supportFragmentManager, START_DATE_DISCOUNT_TAG)
        }
    }

    private fun productEndDateDiscountOnclickListener() {
        binding.etProductEndDateDiscountLayout.setEndIconOnClickListener {
            binding.etProductEndDateDiscount.requestFocus()
            //datePicker.show(supportFragmentManager, END_DATE_DISCOUNT_TAG)
        }
        binding.etProductEndDateDiscount.setOnFocusChangeListener { _, isFocus ->
            if (isFocus) {
                datePicker.show(supportFragmentManager, END_DATE_DISCOUNT_TAG)
            }
        }
        binding.etProductEndDateDiscount.setOnClickListener {
            datePicker.show(supportFragmentManager, END_DATE_DISCOUNT_TAG)
        }
    }

    private fun pairDateTime(time: Long): String {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.timeInMillis = time
        val format = SimpleDateFormat(LOCAL_TIME_FORMAT)
        return format.format(calendar.time)
    }

    private fun partUTCTimeToLocalTime(time: String): String {
        return time.toDate()?.formatTo(LOCAL_TIME_FORMAT) ?: "None"
    }
}