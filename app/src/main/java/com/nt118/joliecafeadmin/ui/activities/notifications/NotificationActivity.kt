package com.nt118.joliecafeadmin.ui.activities.notifications

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.ActivityNotificationBinding
import com.nt118.joliecafeadmin.models.NotificationData
import com.nt118.joliecafeadmin.models.NotificationFormState
import com.nt118.joliecafeadmin.models.PushNotification
import com.nt118.joliecafeadmin.util.*
import com.nt118.joliecafeadmin.util.Constants.Companion.ACTION_TYPE
import com.nt118.joliecafeadmin.util.Constants.Companion.ACTION_TYPE_ADD
import com.nt118.joliecafeadmin.util.Constants.Companion.ACTION_TYPE_EDIT
import com.nt118.joliecafeadmin.util.Constants.Companion.ACTION_TYPE_VIEW
import com.nt118.joliecafeadmin.util.Constants.Companion.COMMON_NOTIFICATION_TOPIC
import com.nt118.joliecafeadmin.util.Constants.Companion.SNACK_BAR_STATUS_ERROR
import com.nt118.joliecafeadmin.util.Constants.Companion.SNACK_BAR_STATUS_SUCCESS
import com.nt118.joliecafeadmin.util.Constants.Companion.listNotificationType
import com.nt118.joliecafeadmin.util.extenstions.setCustomBackground
import com.nt118.joliecafeadmin.util.extenstions.setIcon
import com.nt118.joliecafeadmin.viewmodels.AddNewProductViewModel
import com.nt118.joliecafeadmin.viewmodels.NotificationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationActivity : AppCompatActivity() {

    private var _binding: ActivityNotificationBinding? = null
    private val binding get() = _binding!!

    private val notificationViewModel: NotificationViewModel by viewModels()

    private lateinit var notificationFormState : MutableStateFlow<NotificationFormState>
    private lateinit var networkListener: NetworkListener
    private var notificationType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notificationFormState = notificationViewModel.notificationFormState

        setupBackButton()

        updateBackOnlineStatus()
        updateNetworkStatus()

        observerNetworkMessage()

        getValueFromIntent()

        disableNotificationType()
    }

    private fun getValueFromIntent() {
        val actionType = intent.extras?.getInt(ACTION_TYPE)

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

                    getNotificationType()
                }
                ACTION_TYPE_EDIT -> {
                    println("ACTION_TYPE_EDIT")
                    enableViewToADDOrEdit()

                    observerFormFieldChanged()
                    observerFormFieldError()
                    observerAddNewNotification()
                    observerSendNotification()
                    observerFooterActionClickEvent()
                    observerNotificationFormValidateSubmitEvent()
                }
                ACTION_TYPE_VIEW -> {
                    println("ACTION_TYPE_VIEW")
                }
            }
        }
    }

    private fun observerSendNotification() {
        notificationViewModel.sendNotificationResponse.asLiveData().observe(this) { result ->
            when(result) {
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
            when(result) {
                is ApiResult.Loading -> {
                    println("Loading")
                }
                is ApiResult.NullDataSuccess -> {
                    println("Success")
                    showSnackBar(
                        message = "Create new notification success",
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

    private fun observerFooterActionClickEvent() {
        binding.footerActionButton.btnAddNewNotification.setOnClickListener {
            notificationViewModel.onNotificationFormEvent(event = NotificationFormStateEvent.Submit)
            //observerProductImageError()
        }
    }

    private fun observerNotificationFormValidateSubmitEvent() {
        lifecycleScope.launch {
            notificationViewModel.validationEvents.collect { event ->
                when (event) {
                    is NotificationViewModel.ValidationEvent.Success -> {
                        addNewNotification()
                    }
                }
            }
        }
    }

    private fun addNewNotification() {
        val notificationData = mutableMapOf(
            "title" to notificationFormState.value.title,
            "message" to notificationFormState.value.message,
            "type" to notificationFormState.value.type,
        )

        var pushNotification = PushNotification(
            data =  NotificationData(
                title = notificationFormState.value.title,
                message = notificationFormState.value.message
            ),
            to = "/topics/$COMMON_NOTIFICATION_TOPIC",
        )

        notificationType?.let {
            when(it) {
                listNotificationType[0] -> {
                    notificationViewModel.addNewAdminNotification(notificationData)
                    notificationViewModel.sendNotification(pushNotification)
                }
                listNotificationType[1] -> {
                    notificationData["productId"] = notificationFormState.value.productId
                    notificationViewModel.addNewAdminNotification(notificationData)
                    notificationViewModel.sendNotification(pushNotification)
                }
                listNotificationType[2] -> {
                    notificationData["voucherId"] = notificationFormState.value.productId
                    notificationViewModel.addNewAdminNotification(notificationData)
                    notificationViewModel.sendNotification(pushNotification)
                }
                listNotificationType[3] -> {
                    notificationData["billId"] = notificationFormState.value.billId
                    notificationData["userId"] = notificationFormState.value.userId
                    val userNoticeToken = intent.extras?.getString(Constants.USER_NOTICE_TOKEN)
                    pushNotification = pushNotification.copy(
                        to = userNoticeToken
                    )
                    notificationViewModel.addNewUserNotification(notificationData)
                    notificationViewModel.sendNotification(pushNotification)
                }
            }
        }

    }

    private fun getNotificationType() {
        notificationType = intent.extras?.getString(Constants.NOTIFICATION_TYPE)
        notificationType?.let {
            initDropDownData(it)
            when(it) {
                listNotificationType[0] -> {}
                listNotificationType[1] -> {
                    val productId = intent.extras?.getString(Constants.PRODUCT_ID)
                    if (productId.isNullOrEmpty()) {
                        binding.footerActionButton.btnAddNewNotification.isEnabled = false
                        showSnackBar(
                            message = "Notification type is product, but product id is empty",
                            status = SNACK_BAR_STATUS_ERROR,
                            icon = R.drawable.ic_error
                        )
                    } else {
                        setProductId(productId)
                    }
                }
                listNotificationType[2] -> {
                    val voucherId = intent.extras?.getString(Constants.VOUCHER_ID)
                    if(voucherId.isNullOrEmpty()) {
                        binding.footerActionButton.btnAddNewNotification.isEnabled = false
                        showSnackBar(
                            message = "Notification type is voucher, but voucher id is empty",
                            status = SNACK_BAR_STATUS_ERROR,
                            icon = R.drawable.ic_error
                        )
                    } else {
                        setVoucherId(voucherId)
                    }
                }
                listNotificationType[3] -> {
                    val notificationId = intent.extras?.getString(Constants.NOTIFICATION_ID)
                    val userId = intent.extras?.getString(Constants.USER_ID)
                    notificationId?.let { id ->
                        setNotificationId(id)
                    }
                    if(notificationId.isNullOrEmpty() || userId.isNullOrEmpty()) {
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

    private fun observerFormFieldError() {
        notificationFormState.asLiveData().observe(this) { notificationFormState ->
            binding.etNotificationTitle.error = notificationFormState.titleError
            binding.etNotificationMessage.error = notificationFormState.messageError
            if(notificationFormState.imageError != null) {
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
            }
    }

    private fun observerNetworkMessage() {
        notificationViewModel.networkMessage.observe(this) { message ->
            if (!notificationViewModel.networkStatus) {
                showSnackBar(message = message, status = Constants.SNACK_BAR_STATUS_DISABLE, icon = R.drawable.ic_wifi_off)
            } else if (notificationViewModel.networkStatus) {
                if (notificationViewModel.backOnline) {
                    showSnackBar(message = message, status = Constants.SNACK_BAR_STATUS_SUCCESS, icon = R.drawable.ic_wifi)
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
}