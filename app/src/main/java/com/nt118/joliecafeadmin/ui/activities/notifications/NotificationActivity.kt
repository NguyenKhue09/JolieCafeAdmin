package com.nt118.joliecafeadmin.ui.activities.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.ActivityNotificationBinding
import com.nt118.joliecafeadmin.firebase.firebasefirestore.FirebaseStorage
import com.nt118.joliecafeadmin.models.*
import com.nt118.joliecafeadmin.util.*
import com.nt118.joliecafeadmin.util.Constants.Companion.ACTION_TYPE
import com.nt118.joliecafeadmin.util.Constants.Companion.ACTION_TYPE_ADD
import com.nt118.joliecafeadmin.util.Constants.Companion.ACTION_TYPE_EDIT
import com.nt118.joliecafeadmin.util.Constants.Companion.ACTION_TYPE_VIEW
import com.nt118.joliecafeadmin.util.Constants.Companion.COMMON_NOTIFICATION_TOPIC
import com.nt118.joliecafeadmin.util.Constants.Companion.NOTIFICATION_IMAGE
import com.nt118.joliecafeadmin.util.Constants.Companion.SNACK_BAR_STATUS_ERROR
import com.nt118.joliecafeadmin.util.Constants.Companion.SNACK_BAR_STATUS_SUCCESS
import com.nt118.joliecafeadmin.util.Constants.Companion.listNotificationType
import com.nt118.joliecafeadmin.util.extenstions.isValidUrl
import com.nt118.joliecafeadmin.util.extenstions.setCustomBackground
import com.nt118.joliecafeadmin.util.extenstions.setIcon
import com.nt118.joliecafeadmin.viewmodels.NotificationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException

@AndroidEntryPoint
class NotificationActivity : AppCompatActivity() {

    private var _binding: ActivityNotificationBinding? = null
    private val binding get() = _binding!!

    private val notificationViewModel: NotificationViewModel by viewModels()

    private lateinit var firebaseStorage: FirebaseStorage

    private lateinit var notificationFormState: MutableStateFlow<NotificationFormState>
    private lateinit var networkListener: NetworkListener
    private var notificationType: String? = null
    private var notificationId: String? = null
    private var actionType: Int? = null
    private var image: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notificationFormState = notificationViewModel.notificationFormState
        firebaseStorage = FirebaseStorage()

        setupBackButton()

        updateBackOnlineStatus()
        updateNetworkStatus()

        observerNetworkMessage()

        getValueFromIntent()

        disableNotificationType()

        onTakeImageButtonClicked()
    }

    private fun onTakeImageButtonClicked() {
        binding.btnGetImage.setOnClickListener {
            if (checkPermissionGranted(permission = Manifest.permission.READ_EXTERNAL_STORAGE)) {
                firebaseStorage.chooseFile(getFile)
            } else {
                requestGetFilePermission()
            }
        }
    }

    private fun checkPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val getFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Uri? = result.data?.data
                if (data != null) {
                    try {
                        setImage(image = data)
                        setProductImage(uri = data)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        showSnackBar(
                            message = "Get image failed!",
                            status = SNACK_BAR_STATUS_ERROR,
                            icon = R.drawable.ic_error
                        )
                    }
                }
            }
        }

    private fun setProductImage(uri: Uri) {
        binding.notificationImg.load(uri) {
            crossfade(600)
            error(R.drawable.image_logo)
            placeholder(R.drawable.image_logo)
        }
    }

    private fun requestGetFilePermission() {
        getFilePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private var getFilePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                showSnackBar(
                    message = "Permission granted",
                    status = SNACK_BAR_STATUS_SUCCESS,
                    icon = R.drawable.ic_success
                )
                firebaseStorage.chooseFile(getFile)
            } else {
                showSnackBar(
                    message = "Oops, you just denied the permission for get image!",
                    status = Constants.SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_sad
                )
            }
        }


    private fun getValueFromIntent() {
        actionType = intent.extras?.getInt(ACTION_TYPE)
        image = intent.extras?.getString(NOTIFICATION_IMAGE)

        actionType?.let {
            when (it) {
                ACTION_TYPE_ADD -> {
                    println("ACTION_TYPE_ADD")
                    enableViewToADDOrEdit()

                    observerFormFieldChanged()
                    observerFormFieldError()
                    observerAddNewNotification()
                    observerSendNotification()
                    observerFooterActionClickEvent()
                    observerNotificationFormValidateSubmitEvent()
                    observerUploadImageToFirebase()

                    getNotificationType()
                }
                ACTION_TYPE_EDIT -> {
                    println("ACTION_TYPE_EDIT")

                    binding.footerActionButton.btnAddNewNotification.text = "Update notification"

                    enableViewToADDOrEdit()

                    observerFormFieldChanged()
                    observerFormFieldError()

                    observerSendNotification()
                    observerFooterActionClickEvent()
                    observerNotificationFormValidateSubmitEvent()
                    observerUploadImageToFirebase()
                    observerUpdateNotification()
                    observerGetDetailNotification()
                    notificationId = intent.extras?.getString(Constants.NOTIFICATION_ID)
                    notificationId?.let { id ->
                        notificationViewModel.getNotificationDetail(id)
                    }
                }
                ACTION_TYPE_VIEW -> {
                    println("ACTION_TYPE_VIEW")
                    binding.footerActionButtonContainer.visibility = View.GONE
                    binding.btnGetImage.visibility = View.GONE

                    observerGetDetailNotification()
                    notificationId = intent.extras?.getString(Constants.NOTIFICATION_ID)
                    notificationId?.let { id ->
                        notificationViewModel.getNotificationDetail(id)
                    }
                }
                else -> {}
            }
        }

        image?.let {
            setImage(Uri.parse(it))
        }

    }

    // Action edit
    private fun updateNotification() {
        notificationId?.let { id ->
            val notificationData = mutableMapOf(
                "notificationId" to id ,
                "title" to notificationFormState.value.title,
                "message" to notificationFormState.value.message,
                "type" to notificationFormState.value.type,
                "image" to notificationFormState.value.image.toString(),
            )
            notificationViewModel.updateNotification(notificationData)
        }
    }
    private fun observerUpdateNotification() {
        lifecycleScope.launch {
            notificationViewModel.updateNotificationResponse.collectLatest {
                when (it) {
                    is ApiResult.Loading -> {
                        binding.notificationDetailCircularProgressIndicator.visibility =
                            View.VISIBLE
                    }
                    is ApiResult.Success -> {
                        binding.notificationDetailCircularProgressIndicator.visibility =
                            View.INVISIBLE
                        showSnackBar(
                            message = "Update notification success!",
                            status = SNACK_BAR_STATUS_SUCCESS,
                            icon = R.drawable.ic_success
                        )
                    }
                    is ApiResult.Error -> {
                        binding.notificationDetailCircularProgressIndicator.visibility =
                            View.INVISIBLE
                        showSnackBar(
                            message = it.message!!,
                            status = SNACK_BAR_STATUS_ERROR,
                            icon = R.drawable.ic_error
                        )
                    }
                    else -> {}
                }
            }
        }

    }

    //Action View
    private fun setDataToView(notification: Notification) {
        binding.etNotificationTitle.setText(notification.title)
        binding.etNotificationMessage.setText(notification.message)
        binding.etNotificationType.setText(notification.type)
        notification.image?.let {
            setImage(Uri.parse(it))
            binding.notificationImg.load(it) {
                crossfade(300)
                error(R.drawable.image_logo)
                placeholder(R.drawable.image_logo)
            }
        }
    }
    private fun observerGetDetailNotification() {
        lifecycleScope.launch {
            notificationViewModel.getNotificationDetailResponse.collectLatest {
                when (it) {
                    is ApiResult.Loading -> {
                        binding.notificationDetailCircularProgressIndicator.visibility =
                            View.VISIBLE
                    }
                    is ApiResult.Success -> {
                        binding.notificationDetailCircularProgressIndicator.visibility =
                            View.INVISIBLE
                        setDataToView(it.data!!)
                    }
                    is ApiResult.Error -> {
                        binding.notificationDetailCircularProgressIndicator.visibility =
                            View.INVISIBLE
                        if (notificationViewModel.networkStatus) showSnackBar(
                            message = it.message!!,
                            status = SNACK_BAR_STATUS_ERROR,
                            icon = R.drawable.ic_error
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    // Action Add
    private fun observerSendNotification() {
        notificationViewModel.sendNotificationResponse.asLiveData().observe(this) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    println("Loading")
                }
                is ApiResult.NullDataSuccess -> {
                    println("Success")
                    showSnackBar(
                        message = "Send new notification success",
                        status = SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_success
                    )
                }
                is ApiResult.Error -> {
                    println("Error")
                    showSnackBar(
                        message = result.message!!,
                        status = SNACK_BAR_STATUS_ERROR,
                        icon = R.drawable.ic_error
                    )
                }
                else -> {}
            }
        }
    }

    private fun observerAddNewNotification() {
        notificationViewModel.addNewNotificationResponse.asLiveData().observe(this) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    println("Loading")
                    if (!binding.notificationDetailCircularProgressIndicator.isVisible) {
                        binding.notificationDetailCircularProgressIndicator.visibility =
                            View.VISIBLE
                    }
                }
                is ApiResult.NullDataSuccess -> {
                    println("Success")
                    if (binding.notificationDetailCircularProgressIndicator.isVisible) {
                        binding.notificationDetailCircularProgressIndicator.visibility = View.GONE
                    }
                    showSnackBar(
                        message = "Create new notification success",
                        status = SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_success
                    )
                }
                is ApiResult.Error -> {
                    if (binding.notificationDetailCircularProgressIndicator.isVisible) {
                        binding.notificationDetailCircularProgressIndicator.visibility = View.GONE
                    }
                    println("Error")
                    showSnackBar(
                        message = result.message!!,
                        status = SNACK_BAR_STATUS_ERROR,
                        icon = R.drawable.ic_error
                    )
                }
                else -> {}
            }
        }
    }

    private fun observerFooterActionClickEvent() {
        binding.footerActionButton.btnAddNewNotification.setOnClickListener {
            notificationViewModel.onNotificationFormEvent(event = NotificationFormStateEvent.Submit)
        }
    }

    private fun observerNotificationFormValidateSubmitEvent() {
        lifecycleScope.launch {
            notificationViewModel.validationEvents.collect { event ->
                when (event) {
                    is NotificationViewModel.ValidationEvent.Success -> {
                        println(notificationFormState.value.image)
                        println(notificationFormState.value.image != Uri.EMPTY)
                        println(notificationFormState.value.image.toString()
                            .isValidUrl())
                        if (notificationFormState.value.image != Uri.EMPTY && notificationFormState.value.image.toString()
                                .isValidUrl()
                        ) {
                            apiAction()
                        } else {
                            val fileName = getNameFile(
                                notificationFormState.value.image,
                                this@NotificationActivity
                            )

                            firebaseStorage.uploadFile(
                                file = notificationFormState.value.image,
                                fileName = fileName,
                                root = getString(R.string.app_name),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun apiAction () {
        if(actionType == ACTION_TYPE_ADD) {
            addNewNotification()
        } else if(actionType == ACTION_TYPE_EDIT) {
            updateNotification()
        }
    }

    private fun observerUploadImageToFirebase() {
        firebaseStorage.uploadImageResult.asLiveData().observe(this) { uploadResult ->
            when (uploadResult) {
                is UploadFileToFirebaseResult.Error -> {
                    binding.notificationDetailCircularProgressIndicator.visibility = View.GONE
                    showSnackBar(
                        message = uploadResult.errorMessage!!,
                        status = SNACK_BAR_STATUS_ERROR,
                        icon = R.drawable.ic_error
                    )
                }
                is UploadFileToFirebaseResult.Success -> {
                    println(uploadResult.downloadUri.toString())
                    uploadResult.downloadUri?.let {
                        setImage(image = Uri.parse(it))
                        apiAction()
                    }
                }
                is UploadFileToFirebaseResult.Loading -> {
                    binding.notificationDetailCircularProgressIndicator.visibility = View.VISIBLE
                }
                else -> {}
            }
        }
    }

    private fun addNewNotification() {
        val notificationData = mutableMapOf(
            "title" to notificationFormState.value.title,
            "message" to notificationFormState.value.message,
            "type" to notificationFormState.value.type,
            "image" to notificationFormState.value.image.toString(),
        )

        var pushNotification = PushNotification(
            data = NotificationData(
                title = notificationFormState.value.title,
                message = notificationFormState.value.message
            ),
            to = "/topics/$COMMON_NOTIFICATION_TOPIC",
        )

        notificationType?.let {
            when (it) {
                listNotificationType[0] -> {
                    notificationViewModel.addNewAdminNotification(notificationData)
                    notificationViewModel.sendCommonNotification(pushNotification)
                }
                listNotificationType[1] -> {
                    notificationData["productId"] = notificationFormState.value.productId
                    notificationViewModel.addNewAdminNotification(notificationData)
                    notificationViewModel.sendCommonNotification(pushNotification)
                }
                listNotificationType[2] -> {
                    notificationData["voucherId"] = notificationFormState.value.productId
                    notificationViewModel.addNewAdminNotification(notificationData)
                    notificationViewModel.sendCommonNotification(pushNotification)
                }
                listNotificationType[3] -> {
                    notificationData["billId"] = notificationFormState.value.billId
                    notificationData["userId"] = notificationFormState.value.userId
                    val userNoticeToken = intent.extras?.getString(Constants.USER_NOTICE_TOKEN)
                    pushNotification = pushNotification.copy(
                        to = userNoticeToken
                    )
                    notificationViewModel.addNewUserNotification(notificationData)
                    notificationViewModel.sendSingleNotification(pushNotification)
                }
            }
        }

    }

    private fun getNotificationType() {
        notificationType = intent.extras?.getString(Constants.NOTIFICATION_TYPE)
        notificationType?.let {
            initDropDownData(it)
            when (it) {
                listNotificationType[0] -> {}
                listNotificationType[1] -> {
                    val productId = intent.extras?.getString(Constants.PRODUCT_ID)
                    val productName = intent.extras?.getString(Constants.PRODUCT_NAME)

                    println("$productId $productName")

                    if (productId.isNullOrEmpty() || productName.isNullOrEmpty()) {
                        binding.footerActionButton.btnAddNewNotification.isEnabled = false
                        showSnackBar(
                            message = "Notification type is product, but product id  or product name is empty",
                            status = SNACK_BAR_STATUS_ERROR,
                            icon = R.drawable.ic_error
                        )
                    } else {
                        setProductId(productId)
                        setProductName(productName = productName)
                        setPredictProductNotificationContent(productName = productName, image = image)
                    }
                }
                listNotificationType[2] -> {
                    val voucherId = intent.extras?.getString(Constants.VOUCHER_ID)
                    val voucherCode = intent.extras?.getString(Constants.VOUCHER_CODE)
                    if (voucherId.isNullOrEmpty() || voucherCode.isNullOrEmpty()) {
                        binding.footerActionButton.btnAddNewNotification.isEnabled = false
                        showSnackBar(
                            message = "Notification type is voucher, but voucher id or voucher code is empty",
                            status = SNACK_BAR_STATUS_ERROR,
                            icon = R.drawable.ic_error
                        )
                    } else {
                        setVoucherId(voucherId)
                        setVoucherCode(voucherCode)
                    }
                }
                listNotificationType[3] -> {
                    val notificationId = intent.extras?.getString(Constants.NOTIFICATION_ID)
                    val userId = intent.extras?.getString(Constants.USER_ID)
                    notificationId?.let { id ->
                        setNotificationId(id)
                    }
                    if (notificationId.isNullOrEmpty() || userId.isNullOrEmpty()) {
                        binding.footerActionButton.btnAddNewNotification.isEnabled = false
                        showSnackBar(
                            message = "Notification type is bill, but notification id or user id is empty",
                            status = SNACK_BAR_STATUS_ERROR,
                            icon = R.drawable.ic_error
                        )
                    } else {
                        setUserId(userId)
                        setNotificationId(notificationId)
                    }
                }
            }
        }
    }

    private fun setPredictProductNotificationContent(productName: String, image: String?) {
        binding.etNotificationTitle.setText(productName)
        binding.etNotificationMessage.setText(productName)
        image?.let {
            binding.notificationImg.load(
                Uri.parse(image)
            ) {
                crossfade(300)
                placeholder(R.drawable.image_logo)
                error(R.drawable.image_logo)
            }
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

    private fun observerFormFieldError() {
        notificationFormState.asLiveData().observe(this) { notificationFormState ->
            binding.etNotificationTitle.error = notificationFormState.titleError
            binding.etNotificationMessage.error = notificationFormState.messageError
            if (notificationFormState.imageError != null) {
                showSnackBar(
                    message = notificationFormState.imageError,
                    status = SNACK_BAR_STATUS_ERROR,
                    icon = R.drawable.ic_image_error
                )
            }
        }
    }

    private fun disableNotificationType() {
        binding.etNotificationTypeLayout.endIconMode = TextInputLayout.END_ICON_NONE
    }

    private fun enableViewToADDOrEdit() {
        binding.btnGetImage.isEnabled = true
        binding.etNotificationTitleLayout.isEnabled = true
        binding.etNotificationMessageLayout.isEnabled = true
        binding.footerActionButton.btnAddNewNotification.isEnabled = true
    }

    private fun observerFormFieldChanged() {
        binding.etNotificationTitle.addTextChangedListener { title ->
            println(title.toString())
            notificationViewModel.onNotificationFormEvent(
                event = NotificationFormStateEvent.OnTitleChanged(title.toString())
            )
        }
        binding.etNotificationMessage.addTextChangedListener { message ->
            println(message.toString())
            notificationViewModel.onNotificationFormEvent(
                event = NotificationFormStateEvent.OnMessageChanged(message.toString())
            )
        }
        binding.etNotificationType.addTextChangedListener { type ->
            println(type.toString())
            notificationViewModel.onNotificationFormEvent(
                event = NotificationFormStateEvent.OnTypeChanged(type.toString())
            )
        }
    }


    private fun setVoucherCode(voucherCode: String) {
        notificationFormState.value = notificationFormState.value.copy(
            voucherCode = voucherCode
        )
    }

    private fun setUserId(userId: String) {
        notificationViewModel.onNotificationFormEvent(
            event = NotificationFormStateEvent.OnUserIdChanged(userId)
        )
    }

    private fun setNotificationId(id: String) {
        notificationViewModel.onNotificationFormEvent(
            event = NotificationFormStateEvent.OnNotificationIdChanged(id)
        )
    }

    private fun setProductId(productId: String) {
        notificationViewModel.onNotificationFormEvent(
            event = NotificationFormStateEvent.OnProductIdChanged(productId)
        )
    }

    private fun setProductName(productName: String) {
        notificationViewModel.onNotificationFormEvent(
            event = NotificationFormStateEvent.OnProductNameChanged(productName)
        )
    }

    private fun setImage(image: Uri) {
        notificationViewModel.onNotificationFormEvent(
            event = NotificationFormStateEvent.OnImageChanged(image)
        )
    }

    private fun setVoucherId(voucherId: String) {
        notificationViewModel.onNotificationFormEvent(
            event = NotificationFormStateEvent.OnVoucherIdChanged(voucherId)
        )
    }

    private fun updateBackOnlineStatus() {
        notificationViewModel.readBackOnline.asLiveData().observe(this) { status ->
            notificationViewModel.backOnline = status
        }
    }

    private fun initDropDownData(type: String) {
        binding.etNotificationType.setText(type)
    }

    private fun updateNetworkStatus() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(this)
            .asLiveData().observe(this) { status ->
                notificationViewModel.networkStatus = status
                notificationViewModel.showNetworkStatus()
                recallDataIfBackOnline()
            }
    }

    private fun recallDataIfBackOnline() {
        if (actionType == ACTION_TYPE_EDIT || actionType == ACTION_TYPE_VIEW) {
            notificationId?.let { id ->
                notificationViewModel.getNotificationDetail(id)
            }
        }
    }

    private fun observerNetworkMessage() {
        notificationViewModel.networkMessage.observe(this) { message ->
            if (!notificationViewModel.networkStatus) {
                showSnackBar(
                    message = message,
                    status = Constants.SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            } else if (notificationViewModel.networkStatus) {
                if (notificationViewModel.backOnline) {
                    showSnackBar(
                        message = message,
                        status = Constants.SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_wifi
                    )
                }
            }
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun showSnackBar(message: String, status: Int, icon: Int) {
        val drawable = getDrawable(icon)

        val snackBarContentColor = when (status) {
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
}