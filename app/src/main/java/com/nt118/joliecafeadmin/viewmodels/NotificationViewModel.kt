package com.nt118.joliecafeadmin.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.nt118.joliecafeadmin.data.DataStoreRepository
import com.nt118.joliecafeadmin.data.Repository
import com.nt118.joliecafeadmin.models.*
import com.nt118.joliecafeadmin.use_cases.NotificationFormValidationUseCases
import com.nt118.joliecafeadmin.util.ApiResult
import com.nt118.joliecafeadmin.util.NotificationFormStateEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    dataStoreRepository: DataStoreRepository,
    private val provideNotificationFormValidationUseCases: NotificationFormValidationUseCases
) : BaseViewModel(application, dataStoreRepository){

    private val _addNewNotificationResponse =
        MutableStateFlow<ApiResult<Unit>>(ApiResult.Idle())
    val addNewNotificationResponse: StateFlow<ApiResult<Unit>> = _addNewNotificationResponse

    private val _updateNotificationResponse =
        MutableStateFlow<ApiResult<Notification>>(ApiResult.Idle())
    val updateNotificationResponse: StateFlow<ApiResult<Notification>> = _updateNotificationResponse

    private val _sendNotificationResponse =
        MutableStateFlow<ApiResult<Unit>>(ApiResult.Idle())
    val sendNotificationResponse: StateFlow<ApiResult<Unit>> = _sendNotificationResponse


    private val _getNotificationDetailResponse =
        MutableStateFlow<ApiResult<Notification>>(ApiResult.Idle())
    val getNotificationDetailResponse: StateFlow<ApiResult<Notification>> = _getNotificationDetailResponse

    private val validationEventChannel = Channel<ValidationEvent>()
    val validationEvents = validationEventChannel.receiveAsFlow()

    val notificationFormState = MutableStateFlow(NotificationFormState())

    fun addNewUserNotification(notificationData: Map<String, String>) {
        viewModelScope.launch {
            _addNewNotificationResponse.value = ApiResult.Loading()
            val result = repository.remote.addNewUserNotification(token = adminToken, notificationData = notificationData)
            _addNewNotificationResponse.value = handleApiNullDataSuccessResponse(response = result)
        }
    }

    fun addNewAdminNotification(notificationData: Map<String, String>) {
        viewModelScope.launch {
            _addNewNotificationResponse.value = ApiResult.Loading()
            val result = repository.remote.addNewAdminNotification(token = adminToken, notificationData = notificationData)
            _addNewNotificationResponse.value = handleApiNullDataSuccessResponse(response = result)
        }
    }

    fun updateNotification(notificationData: Map<String, String>) {
        viewModelScope.launch {
            _updateNotificationResponse.value = ApiResult.Loading()
            val result = repository.remote.updateNotification(token = adminToken, notificationData = notificationData)
            _updateNotificationResponse.value = handleApiResponse(response = result)
        }
    }


    fun sendCommonNotification(pushNotification: PushNotification) {
        viewModelScope.launch {
            println("sendCommonNotification")
            try {
                _sendNotificationResponse.value = ApiResult.Loading()
                val result = repository.remote.sendCommonNotification(notificationData = pushNotification)
                _sendNotificationResponse.value = handleFCMCommonApiResponse(response = result)
            } catch (e: Exception) {
                e.printStackTrace()
                _sendNotificationResponse.value = ApiResult.Error(e.message)
            }

        }
    }

    fun sendSingleNotification(pushNotification: PushNotification) {
        viewModelScope.launch {
            println("sendCommonNotification")
            try {
                _sendNotificationResponse.value = ApiResult.Loading()
                val result = repository.remote.sendSingleNotification(notificationData = pushNotification)
                _sendNotificationResponse.value = handleFCMSingleApiResponse(response = result)
            } catch (e: Exception) {
                e.printStackTrace()
                _sendNotificationResponse.value = ApiResult.Error(e.message)
            }

        }
    }

    fun getNotificationDetail(notificationId: String) {
        viewModelScope.launch {
            _getNotificationDetailResponse.value = ApiResult.Loading()
            try {
                val response =
                    repository.remote.getNotificationDetail(token = adminToken, notificationId = notificationId)
                _getNotificationDetailResponse.value = handleApiResponse(response = response)
            } catch (e: Exception) {
                e.printStackTrace()
                _getNotificationDetailResponse.value = ApiResult.Error(e.message)
            }
        }
    }

    fun onNotificationFormEvent(event: NotificationFormStateEvent) {
        when(event) {
            is NotificationFormStateEvent.OnTitleChanged -> {
                notificationFormState.value = notificationFormState.value.copy(title = event.title)
            }
            is NotificationFormStateEvent.OnMessageChanged -> {
                notificationFormState.value = notificationFormState.value.copy(message = event.message)
            }
            is NotificationFormStateEvent.OnImageChanged -> {
                notificationFormState.value = notificationFormState.value.copy(image = event.imageUri)
            }
            is NotificationFormStateEvent.OnTypeChanged -> {
                notificationFormState.value = notificationFormState.value.copy(type = event.type)
            }
            is NotificationFormStateEvent.OnProductIdChanged -> {
                notificationFormState.value = notificationFormState.value.copy(productId = event.productId)
            }
            is NotificationFormStateEvent.OnProductNameChanged -> {
                notificationFormState.value = notificationFormState.value.copy(productName = event.productName)
            }
            is NotificationFormStateEvent.OnVoucherIdChanged -> {
                notificationFormState.value = notificationFormState.value.copy(voucherId = event.voucherId)
            }
            is NotificationFormStateEvent.OnVoucherCodeChanged -> {
                notificationFormState.value = notificationFormState.value.copy(voucherCode = event.voucherCode)
            }
            is NotificationFormStateEvent.OnBillIdChanged -> {
                notificationFormState.value = notificationFormState.value.copy(billId = event.billId)
            }
            is NotificationFormStateEvent.OnUserIdChanged -> {
                notificationFormState.value = notificationFormState.value.copy(userId = event.userId)
            }
            is NotificationFormStateEvent.OnNotificationIdChanged -> {
                notificationFormState.value = notificationFormState.value.copy(notificationId = event.notificationId)
            }
            is NotificationFormStateEvent.Submit -> {
                submitNotificationFormData()
            }
        }
    }

    private fun submitNotificationFormData() {
        val titleResult = provideNotificationFormValidationUseCases.validateNotificationTitleUseCase.execute(notificationFormState.value.title)
        val messageResult = provideNotificationFormValidationUseCases.validateNotificationMessageUseCase.execute(notificationFormState.value.message)
        val imageResult = if(notificationFormState.value.image != Uri.EMPTY) provideNotificationFormValidationUseCases.validateNotificationImageUseCase.execute(notificationFormState.value.image) else ValidationResult(successful = true)

        val listResult = listOf(titleResult, messageResult, imageResult)

        val hasErrors = listResult.any { !it.successful }
        if (hasErrors) {
            notificationFormState.value = notificationFormState.value.copy(
                titleError = titleResult.errorMessage,
                messageError = messageResult.errorMessage,
                imageError = imageResult.errorMessage
            )
            return
        } else {
            cleanNotificationFormError()
        }

        viewModelScope.launch {
            validationEventChannel.send(ValidationEvent.Success)
        }
    }

    private fun cleanNotificationFormError() {
        notificationFormState.value = notificationFormState.value.copy(
            titleError = null,
            messageError = null,
            imageError = null
        )
    }

    sealed class ValidationEvent {
        object Success: ValidationEvent()
    }

}