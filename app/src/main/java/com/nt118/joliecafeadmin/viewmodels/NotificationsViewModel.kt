package com.nt118.joliecafeadmin.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nt118.joliecafeadmin.data.DataStoreRepository
import com.nt118.joliecafeadmin.data.Repository
import com.nt118.joliecafeadmin.models.Notification
import com.nt118.joliecafeadmin.models.Product
import com.nt118.joliecafeadmin.models.ProductFormState
import com.nt118.joliecafeadmin.util.Constants.Companion.listTabNotificationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    dataStoreRepository: DataStoreRepository
) : BaseViewModel(application, dataStoreRepository) {

    private var _tabSelected = MutableLiveData<String>()
    val tabSelected: LiveData<String> = _tabSelected

    init {
        setTabSelected(tab = listTabNotificationType[0])
    }

    fun getNotifications(
        notificationQuery: Map<String, String>
    ): Flow<PagingData<Notification>> {
        return if (adminToken.isNotEmpty()) {
            try {
                repository.remote.getNotifications(
                    notificationQuery = notificationQuery,
                    token = adminToken
                ).cachedIn(viewModelScope)
            } catch (e: Exception) {
                e.printStackTrace()
                flowOf(PagingData.empty())
            }
        } else {
            println("Token empty")
            flowOf(PagingData.empty())
        }
    }

    fun setTabSelected(tab: String) {
        _tabSelected.value = tab
    }
}