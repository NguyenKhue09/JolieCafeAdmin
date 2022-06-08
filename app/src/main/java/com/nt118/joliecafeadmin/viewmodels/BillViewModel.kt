package com.nt118.joliecafeadmin.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nt118.joliecafeadmin.data.DataStoreRepository
import com.nt118.joliecafeadmin.data.Repository
import com.nt118.joliecafeadmin.models.Bill
import com.nt118.joliecafeadmin.models.Product
import com.nt118.joliecafeadmin.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class BillViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
    dataStoreRepository: DataStoreRepository
) : BaseViewModel(application, dataStoreRepository) {

    private var _tabSelected = MutableLiveData<String>()
    val tabSelected: LiveData<String> = _tabSelected

    private val _billClickedList = MutableLiveData<MutableList<String>>(mutableListOf())
    val billClickedList: LiveData<MutableList<String>> = _billClickedList

    init {
        setTabSelected(Constants.listTabBillStatus[0])
    }

    fun getAdminBills(
        status: String,
    ): Flow<PagingData<Bill>> {
        return if (adminToken.isNotEmpty()) {
            try {
                repository.remote.getAdminBills(
                    status = status,
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

    fun addNewBillToClickedList(id: String) {
        println(id)
        if(!_billClickedList.value!!.contains(id)) {
            _billClickedList.value!!.add(id)
        } else {
            _billClickedList.value!!.remove(id)
        }
        _billClickedList.value = _billClickedList.value
    }

    fun setTabSelected(tab: String) {
        _tabSelected.value = tab
    }
}