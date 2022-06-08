package com.nt118.joliecafeadmin.ui.activities.bill

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.adapter.BillAdapter
import com.nt118.joliecafeadmin.databinding.ActivityBillsBinding
import com.nt118.joliecafeadmin.models.Bill
import com.nt118.joliecafeadmin.ui.activities.notifications.NotificationActivity
import com.nt118.joliecafeadmin.util.ApiResult
import com.nt118.joliecafeadmin.util.BillComparator
import com.nt118.joliecafeadmin.util.Constants
import com.nt118.joliecafeadmin.util.Constants.Companion.SNACK_BAR_STATUS_ERROR
import com.nt118.joliecafeadmin.util.Constants.Companion.SNACK_BAR_STATUS_SUCCESS
import com.nt118.joliecafeadmin.util.Constants.Companion.listTabBillStatus
import com.nt118.joliecafeadmin.util.NetworkListener
import com.nt118.joliecafeadmin.util.extenstions.setCustomBackground
import com.nt118.joliecafeadmin.util.extenstions.setIcon
import com.nt118.joliecafeadmin.viewmodels.BillViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
class BillsActivity : AppCompatActivity() {

    private var _binding: ActivityBillsBinding? = null
    private val binding get() = _binding!!

    private val billViewModel: BillViewModel by viewModels()

    private lateinit var billAdapter: BillAdapter
    private lateinit var selectedTab: String
    private lateinit var networkListener: NetworkListener

    lateinit var billClickedList: LiveData<MutableList<String>>

    private lateinit var customAlertDialogView : View
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder

    private val billStatusUpdateChecked = MutableLiveData("Pending")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityBillsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        billClickedList = billViewModel.billClickedList

        setupActionBar()

        updateNetworkStatus()
        updateBackOnlineStatus()
        observerNetworkMessage()

        observerUpdateBillResponse()

        initBillAdapter()
        configBillRecyclerView()
        addBillTabText()
        initBillAdapterData()
        setBillAdapterDataWhenTabChange()
        onBillTabSelected()
        handleProductPagingAdapterState()

        initMaterialAlertDialogBuilder()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarBills)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

        binding.toolbarBills.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun initMaterialAlertDialogBuilder() {
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Background)
    }

    private fun initUpdateDialogView() {
        customAlertDialogView = layoutInflater.inflate(R.layout.update_bill_status_dialog_layout, null, false)
    }
    private fun launchUpdateAlertDialog(status: String, id: String, paid: Boolean) {
        val cbPending = customAlertDialogView.findViewById<CheckBox>(R.id.cb_pending)
        val cbDelivering = customAlertDialogView.findViewById<CheckBox>(R.id.cb_delivering)
        val cbReceived = customAlertDialogView.findViewById<CheckBox>(R.id.cb_received)
        val cbCancelled = customAlertDialogView.findViewById<CheckBox>(R.id.cb_cancelled)
        val cbPaid = customAlertDialogView.findViewById<CheckBox>(R.id.cb_paid)

        cbPaid.isChecked = paid

        billStatusUpdateChecked.value = status

        cbPending.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                billStatusUpdateChecked.value = listTabBillStatus[0]
            }
        }
        cbDelivering.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                billStatusUpdateChecked.value = listTabBillStatus[1]
            }
        }
        cbReceived.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                billStatusUpdateChecked.value = listTabBillStatus[2]
            }
        }
        cbCancelled.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                billStatusUpdateChecked.value = listTabBillStatus[3]
            }
        }

        billStatusUpdateChecked.observe(this) { selectedStatus ->
            cbPending.isChecked = listTabBillStatus[0] == selectedStatus
            cbPending.isEnabled = listTabBillStatus[0] != selectedStatus

            cbDelivering.isChecked = listTabBillStatus[1] == selectedStatus
            cbDelivering.isEnabled = listTabBillStatus[1] != selectedStatus

            cbReceived.isChecked = listTabBillStatus[2] == selectedStatus
            cbReceived.isEnabled = listTabBillStatus[2] != selectedStatus

            cbCancelled.isChecked = listTabBillStatus[3] == selectedStatus
            cbCancelled.isEnabled = listTabBillStatus[3] != selectedStatus
        }



        materialAlertDialogBuilder.setView(customAlertDialogView)
            .setPositiveButton("Update") { dialog, _ ->
                billStatusUpdateChecked.value?.let {
                    if(it != status || cbPaid.isChecked != paid) {
                        billViewModel.updateBill(
                            billData = mapOf(
                                "status" to it,
                                "billId" to id,
                                "paid" to cbPaid.isChecked.toString()
                            )
                        )
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private  fun observerUpdateBillResponse() {
        billViewModel.updateBillResponse.asLiveData().observe(this) { result ->
            when(result) {
                is ApiResult.Loading -> {
                    binding.billCircularProgressIndicator.visibility = View.VISIBLE
                }
                is ApiResult.NullDataSuccess -> {
                    binding.billCircularProgressIndicator.visibility = View.GONE
                    showSnackBar(message = "Bill updated successfully", status = SNACK_BAR_STATUS_SUCCESS, icon = R.drawable.ic_success)
                    getAdminBills()
                }
                is ApiResult.Error -> {
                    binding.billCircularProgressIndicator.visibility = View.GONE
                    showSnackBar(message = "Bill updated failed", status = SNACK_BAR_STATUS_ERROR, icon = R.drawable.ic_error)
                }
                else -> {}
            }

        }
    }


    fun addNewBillToClickList(id: String) {
        billViewModel.addNewBillToClickedList(id)
    }

    private fun handleProductPagingAdapterState() {
        billAdapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading) {
                binding.billCircularProgressIndicator.visibility = View.VISIBLE
            } else {
                binding.billCircularProgressIndicator.visibility = View.GONE
                // getting the error
                val error = when {
                    loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                    loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                    loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                    else -> null
                }
                error?.let {
                    println(it)
                    if (billViewModel.networkStatus)
                        showSnackBar(
                            message = "Can't load product data!",
                            status = Constants.SNACK_BAR_STATUS_ERROR,
                            icon = R.drawable.ic_error
                        )
                }
            }
        }
    }

    private fun onBillTabSelected() {
        binding.billsTabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    billViewModel.setTabSelected(tab = tab.text.toString())
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun setBillAdapterDataWhenTabChange() {
        billViewModel.tabSelected.observe(this) { tab ->
            selectedTab = tab
            if (billViewModel.networkStatus) {
                getAdminBills()
            }
        }
    }

    private fun getAdminBills() {
        lifecycleScope.launchWhenStarted {
            billViewModel.getAdminBills(
                status = selectedTab
            ).collectLatest { data ->
                submitProductAdapterData(data = data)
            }
        }
    }

    private suspend fun submitProductAdapterData(data: PagingData<Bill>) {
        billAdapter.submitData(data)
    }

    private fun initBillAdapterData() {
        val tabIndex = Constants.listTabBillStatus.indexOf(billViewModel.tabSelected.value)
        binding.billsTabLayout.selectTab(binding.billsTabLayout.getTabAt(tabIndex), true)
    }

    private fun addBillTabText() {
        Constants.listTabBillStatus.forEach {
            binding.billsTabLayout.addTab(binding.billsTabLayout.newTab().apply {
                tag = it
                text = it
            })
        }
    }

    private fun configBillRecyclerView() {
        val billRV = binding.billRv
        billRV.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        billRV.adapter = billAdapter
    }

    private fun initBillAdapter() {
        val diffUtil = BillComparator
        billAdapter = BillAdapter(
            this,
            onNotificationClicked = { id, userId, token ->
                val intend = Intent(this, NotificationActivity::class.java)
                intend.putExtra(Constants.ACTION_TYPE, Constants.ACTION_TYPE_ADD)
                intend.putExtra(Constants.USER_ID, userId)
                intend.putExtra(Constants.BILL_ID, id)
                intend.putExtra(Constants.USER_NOTICE_TOKEN, token)
                intend.putExtra(Constants.NOTIFICATION_TYPE, Constants.listNotificationType[3])
                startActivity(intend)
            },
            onBillUpdateClicked = { id, status, paid ->
                initUpdateDialogView()
                launchUpdateAlertDialog(status, id, paid)
            },
            diffUtil,
        )
    }

    private fun observerNetworkMessage() {
        billViewModel.networkMessage.observe(this) { message ->
            if (!billViewModel.networkStatus) {
                showSnackBar(
                    message = message,
                    status = Constants.SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            } else if (billViewModel.networkStatus) {
                if (billViewModel.backOnline) {
                    showSnackBar(
                        message = message,
                        status = Constants.SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_wifi
                    )
                }
            }
        }
    }

    private fun updateNetworkStatus() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(this)
            .asLiveData().observe(this) { status ->
                billViewModel.networkStatus = status
                billViewModel.showNetworkStatus()
            }
    }

    private fun updateBackOnlineStatus() {
        billViewModel.readBackOnline.asLiveData().observe(this) { status ->
            billViewModel.backOnline = status
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        billAdapter.removeOrderListIdObserver()
    }
}