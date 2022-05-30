package com.nt118.joliecafeadmin.ui.activities.add_product

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.PorterDuff
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.ActivityAddNewProductBinding
import com.nt118.joliecafeadmin.firebase.firebasefirestore.FirebaseStorage
import com.nt118.joliecafeadmin.models.ProductFormState
import com.nt118.joliecafeadmin.util.ApiResult
import com.nt118.joliecafeadmin.util.Constants
import com.nt118.joliecafeadmin.util.Constants.Companion.SNACK_BAR_STATUS_DISABLE
import com.nt118.joliecafeadmin.util.Constants.Companion.SNACK_BAR_STATUS_ERROR
import com.nt118.joliecafeadmin.util.Constants.Companion.SNACK_BAR_STATUS_SUCCESS
import com.nt118.joliecafeadmin.util.NetworkListener
import com.nt118.joliecafeadmin.util.ProductFormStateEvent
import com.nt118.joliecafeadmin.util.extenstions.setCustomBackground
import com.nt118.joliecafeadmin.util.extenstions.setIcon
import com.nt118.joliecafeadmin.viewmodels.AddNewProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AddNewProductActivity : AppCompatActivity() {

    private var _binding: ActivityAddNewProductBinding? = null
    private val binding get() = _binding!!

    private val addNewProductViewModel: AddNewProductViewModel by viewModels()

    private lateinit var datePicker: MaterialDatePicker<Long>

    private lateinit var networkListener: NetworkListener

    private lateinit var productFormState: MutableStateFlow<ProductFormState>

    private var productImageUri: Uri? = null
    private lateinit var firebaseStorage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAddNewProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productFormState = addNewProductViewModel.productFormState
        firebaseStorage = FirebaseStorage()

        setUpBackPress()
        setupDatePicker()

        setProductTypeDropDownMenu()
        setProductStatusDropDownMenu()

        updateBackOnlineStatus()
        updateNetworkStatus()

        observerAddNewProductDetailResponse()
        observerProductFormFieldChanged()
        observerProductFormFieldError()
        observerFooterActionClickEvent()
        observerProductFormValidateSubmitEvent()
        observerNetworkMessage()

        productStartDateDiscountOnclickListener()
        productEndDateDiscountOnclickListener()

        onTakeImageButtonClicked()

        handleDatePickerEvent()
    }

    private fun observerNetworkMessage() {
        addNewProductViewModel.networkMessage.observe(this) { message ->
            if (!addNewProductViewModel.networkStatus) {
              showSnackBar(message = message, status = SNACK_BAR_STATUS_DISABLE, icon = R.drawable.ic_wifi_off)
            } else if (addNewProductViewModel.networkStatus) {
                if (addNewProductViewModel.backOnline) {
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

    private fun onTakeImageButtonClicked() {
        binding.btnGetImage.setOnClickListener {
            if(checkPermissionGranted(permission = Manifest.permission.READ_EXTERNAL_STORAGE)) {
                firebaseStorage.chooseFile(getFile)
            } else {
                requestGetFilePermission()
            }
        }
    }

    private val getFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Uri? = result.data?.data
                if (data != null) {
                    try {
                        val fileName = getNameFile(data, this)

                        productImageUri = data

                        setProductImage(uri = data)
//                        firebaseStorage.uploadFile(
//                            file = data,
//                            fileName = fileName,
//                            root = getString(R.string.app_name),
//                            addNewProductActivity = this
//                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this,
                            "Get image failed!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

    private fun requestGetFilePermission() {
        getFilePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private var getFilePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(
                    this,
                    "Permission granted",
                    Toast.LENGTH_LONG
                ).show()
                firebaseStorage.chooseFile(getFile)
            } else {
                Toast.makeText(
                    this,
                    "Oops, you just denied the permission for storage, You can also allow it from settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    @SuppressLint("Range")
    fun getNameFile(uri: Uri, context: Context): String {
        var res: String? = null
        if (uri.scheme.equals("content")) {
            val cursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
            cursor.use { cur ->
                if (cur.moveToFirst()) {
                    res = cur.getString(cur.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (res == null) {
            res = uri.path!!.split('/').also {
                it[it.lastIndex]
            }.toString()
        }
        return res as String
    }

    private fun checkPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun observerProductFormValidateSubmitEvent() {
        lifecycleScope.launch {
            addNewProductViewModel.validationEvents.collect { event ->
                when (event) {
                    is AddNewProductViewModel.ValidationEvent.Success -> {

                        val newProductData = if (productFormState.value.isDiscount) {
                            mapOf(
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
                                "name" to productFormState.value.productName,
                                "status" to productFormState.value.productStatus,
                                "description" to productFormState.value.productDescription,
                                "originPrice" to productFormState.value.productPrice,
                                "type" to productFormState.value.productType,
                            )
                        }

                        // add new product call function
                    }
                }
            }
        }
    }

    private fun observerFooterActionClickEvent() {

        binding.footerActionButton.btnAddNewProduct.setOnClickListener {
            addNewProductViewModel.onProductFormEvent(event = ProductFormStateEvent.Submit)
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

    private fun observerProductFormFieldChanged() {
        binding.etProductName.addTextChangedListener { name ->
            println(name.toString())
            addNewProductViewModel.onProductFormEvent(
                event = ProductFormStateEvent.OnProductNameChanged(
                    productName = name.toString()
                )
            )
        }
        binding.etProductType.addTextChangedListener { type ->
            println(type.toString())
            addNewProductViewModel.onProductFormEvent(
                event = ProductFormStateEvent.OnProductTypeChanged(
                    productType = type.toString()
                )
            )
        }
        binding.etProductPrice.addTextChangedListener { price ->
            println(price.toString())
            addNewProductViewModel.onProductFormEvent(
                event = ProductFormStateEvent.OnProductPriceChanged(
                    productPrice = price.toString()
                )
            )
        }
        binding.etProductStatus.addTextChangedListener { status ->
            println(status.toString())
            addNewProductViewModel.onProductFormEvent(
                event = ProductFormStateEvent.OnProductStatusChanged(
                    productStatus = status.toString()
                )
            )
        }
        binding.etProductDescription.addTextChangedListener { description ->
            println(description.toString())
            addNewProductViewModel.onProductFormEvent(
                event = ProductFormStateEvent.OnProductDescriptionChanged(
                    productDescription = description.toString()
                )
            )
        }

        productDiscountSwitchButtonListener()

        binding.etProductStartDateDiscount.addTextChangedListener { start ->
            println(start.toString())
            addNewProductViewModel.onProductFormEvent(
                event = ProductFormStateEvent.OnProductStartDateDiscountChanged(
                    productStartDateDiscount = start.toString()
                )
            )
        }
        binding.etProductEndDateDiscount.addTextChangedListener { end ->
            println(end.toString())
            addNewProductViewModel.onProductFormEvent(
                event = ProductFormStateEvent.OnProductEndDateDiscountChanged(
                    productEndDateDiscount = end.toString()
                )
            )
        }
        binding.etProductDiscountPercent.addTextChangedListener { percent ->
            println(percent.toString())
            addNewProductViewModel.onProductFormEvent(
                event = ProductFormStateEvent.OnProductDiscountPercentChanged(
                    productDiscountPercent = percent.toString()
                )
            )
        }

    }

    private fun observerAddNewProductDetailResponse() {
        lifecycleScope.launch {
            addNewProductViewModel.addNewProductResponse.collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        binding.addNewProductCircularProgressIndicator.visibility = View.VISIBLE
                    }
                    is ApiResult.Error -> {
                        binding.addNewProductCircularProgressIndicator.visibility = View.GONE
                        Toast.makeText(
                            this@AddNewProductActivity,
                            result.message ?: "Unknown error!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is ApiResult.Success -> {
                        binding.addNewProductCircularProgressIndicator.visibility = View.GONE
                        Toast.makeText(
                            this@AddNewProductActivity,
                            "Add new product success",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                    }
                }
            }
        }
    }

    private fun setUpBackPress() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
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

    private fun setProductTypeDropDownMenu() {
        val listTypes = Constants.listProductTypes.subList(1, Constants.listProductTypes.size - 1)
        val productTypesAdapter = ArrayAdapter(this, R.layout.drop_down_item, listTypes)
        binding.etProductType.setAdapter(productTypesAdapter)
    }

    private fun setProductStatusDropDownMenu() {
        val productStatusAdapter =
            ArrayAdapter(this, R.layout.drop_down_item, Constants.listProductStatus)
        binding.etProductStatus.setAdapter(productStatusAdapter)
    }

    private fun setProductImage(uri: Uri) {
        binding.productImg.load(uri) {
            crossfade(600)
            error(R.drawable.image_logo)
            placeholder(R.drawable.image_logo)
        }
    }

    private fun updateBackOnlineStatus() {
        addNewProductViewModel.readBackOnline.asLiveData().observe(this) { status ->
            addNewProductViewModel.backOnline = status
        }
    }

    private fun updateNetworkStatus() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(this)
            .asLiveData().observe(this) { status ->
                addNewProductViewModel.networkStatus = status
                addNewProductViewModel.showNetworkStatus()
            }
    }

    private fun productDiscountSwitchButtonListener() {
        binding.switchProductDiscount.setOnCheckedChangeListener { _, isChecked ->
            addNewProductViewModel.onProductFormEvent(
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
                datePicker.show(supportFragmentManager, Constants.START_DATE_DISCOUNT_TAG)
            }
        }
        binding.etProductStartDateDiscount.setOnClickListener {
            datePicker.show(supportFragmentManager, Constants.START_DATE_DISCOUNT_TAG)
        }
    }

    private fun productEndDateDiscountOnclickListener() {
        binding.etProductEndDateDiscountLayout.setEndIconOnClickListener {
            binding.etProductEndDateDiscount.requestFocus()
            //datePicker.show(supportFragmentManager, END_DATE_DISCOUNT_TAG)
        }
        binding.etProductEndDateDiscount.setOnFocusChangeListener { _, isFocus ->
            if (isFocus) {
                datePicker.show(supportFragmentManager, Constants.END_DATE_DISCOUNT_TAG)
            }
        }
        binding.etProductEndDateDiscount.setOnClickListener {
            datePicker.show(supportFragmentManager, Constants.END_DATE_DISCOUNT_TAG)
        }
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
        if (tag == Constants.START_DATE_DISCOUNT_TAG) {
            binding.etProductStartDateDiscount.setText(date)
        } else if (tag == Constants.END_DATE_DISCOUNT_TAG) {
            binding.etProductEndDateDiscount.setText(date)
        }
    }

    private fun pairDateTime(time: Long): String {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.timeInMillis = time
        val format = SimpleDateFormat(Constants.LOCAL_TIME_FORMAT)
        return format.format(calendar.time)
    }

}