package com.nt118.joliecafeadmin.ui.activities.notifications

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.adapter.NotificationItemAdapter
import com.nt118.joliecafeadmin.adapter.ProductItemAdapter
import com.nt118.joliecafeadmin.databinding.ActivityNotificationsBinding
import com.nt118.joliecafeadmin.models.Notification
import com.nt118.joliecafeadmin.util.Constants
import com.nt118.joliecafeadmin.util.Constants.Companion.NOTIFICATION_ID
import com.nt118.joliecafeadmin.util.Constants.Companion.NOTIFICATION_TYPE
import com.nt118.joliecafeadmin.util.Constants.Companion.listNotificationType
import com.nt118.joliecafeadmin.util.NetworkListener
import com.nt118.joliecafeadmin.util.NotificationComparator
import com.nt118.joliecafeadmin.util.ProductComparator
import com.nt118.joliecafeadmin.util.extenstions.setCustomBackground
import com.nt118.joliecafeadmin.util.extenstions.setIcon
import com.nt118.joliecafeadmin.viewmodels.NotificationsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.*

@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity() {

    private var _binding: ActivityNotificationsBinding? = null
    private val binding get() = _binding!!

    private val notificationsViewModel: NotificationsViewModel by viewModels()

    private lateinit var notificationItemAdapter: NotificationItemAdapter
    private lateinit var selectedTab: String
    private lateinit var networkListener: NetworkListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        updateNetworkStatus()
        updateBackOnlineStatus()
        observerNetworkMessage()

        onAddNotificationButtonClicked()

        initProductAdapter()
        configProductRecyclerView()
        addProductTabText()
        initProductAdapterData()
        setNotificationAdapterDataWhenTabChange()
        onNotificationTabSelected()
        handleNotificationPagingAdapterState()
    }

    private fun handleNotificationPagingAdapterState() {
        notificationItemAdapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading) {
                binding.notificationCircularProgressIndicator.visibility = View.VISIBLE
            } else {
                binding.notificationCircularProgressIndicator.visibility = View.GONE
                // getting the error
                val error = when {
                    loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                    loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                    loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                    else -> null
                }
                error?.let {
                    if (notificationsViewModel.networkStatus)
                        showSnackBar(
                            message = "Can't load product data!",
                            status = Constants.SNACK_BAR_STATUS_ERROR,
                            icon = R.drawable.ic_error
                        )
                }
            }
        }
    }

    private fun onNotificationTabSelected() {
        binding.notificationsTabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    notificationsViewModel.setTabSelected(tab = tab.text.toString())
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun setNotificationAdapterDataWhenTabChange() {
        notificationsViewModel.tabSelected.observe(this) { tab ->
            println(tab)
            if (notificationsViewModel.networkStatus) {
                lifecycleScope.launchWhenStarted {
                    notificationsViewModel.getNotifications(
                        notificationQuery = mapOf(
                            "type" to tab.uppercase(Locale.getDefault())
                        )
                    ).collectLatest { data ->
                        selectedTab = tab
                        submitProductAdapterData(data = data)
                    }
                }
            }
        }
    }

    private suspend fun submitProductAdapterData(data: PagingData<Notification>) {
        notificationItemAdapter.submitData(data)
    }

    private fun initProductAdapterData() {
        val tabIndex = Constants.listProductTypes.indexOf(notificationsViewModel.tabSelected.value)
        binding.notificationsTabLayout.selectTab(binding.notificationsTabLayout.getTabAt(tabIndex), true)
    }

    private fun addProductTabText() {
        Constants.listTabNotificationType.forEach {
            binding.notificationsTabLayout.addTab(binding.notificationsTabLayout.newTab().apply {
                tag = it
                text = it
            })
        }
    }

    private fun configProductRecyclerView() {
        val notificationRv = binding.notificationRv
        notificationRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        notificationRv.adapter = notificationItemAdapter
    }

    private fun initProductAdapter() {
        val diffUtil = NotificationComparator
        notificationItemAdapter = NotificationItemAdapter(
            notificationActivity = this,
            diffUtil = diffUtil,
            onNotificationClicked = { notificationId ->
                val intend = Intent(this, NotificationActivity::class.java)
                intend.putExtra(Constants.ACTION_TYPE, Constants.ACTION_TYPE_VIEW)
                intend.putExtra(NOTIFICATION_ID, notificationId)
                startActivity(intend)
            },
            onEditNotificationClicked = { notificationId ->
                val intend = Intent(this, NotificationActivity::class.java)
                intend.putExtra(Constants.ACTION_TYPE, Constants.ACTION_TYPE_EDIT)
                intend.putExtra(NOTIFICATION_ID, notificationId)
                startActivity(intend)
            },
        )
    }

    private fun onAddNotificationButtonClicked() {
        binding.btnCreateNewNotification.setOnClickListener {
            val intend = Intent(this, NotificationActivity::class.java)
            intend.putExtra(Constants.ACTION_TYPE, Constants.ACTION_TYPE_ADD)
            intend.putExtra(NOTIFICATION_TYPE, listNotificationType[0])
            startActivity(intend)
        }
    }

    private fun observerNetworkMessage() {
        notificationsViewModel.networkMessage.observe(this) { message ->
            if (!notificationsViewModel.networkStatus) {
                showSnackBar(
                    message = message,
                    status = Constants.SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            } else if (notificationsViewModel.networkStatus) {
                if (notificationsViewModel.backOnline) {
                    showSnackBar(
                        message = message,
                        status = Constants.SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_wifi
                    )
                }
            }
        }
    }

    private fun updateBackOnlineStatus() {
        notificationsViewModel.readBackOnline.asLiveData().observe(this) { status ->
            notificationsViewModel.backOnline = status
        }
    }

    private fun updateNetworkStatus() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(this)
            .asLiveData().observe(this) { status ->
                notificationsViewModel.networkStatus = status
                notificationsViewModel.showNetworkStatus()
                backOnlineRecallNotifications()
            }
    }

    private fun backOnlineRecallNotifications() {
        lifecycleScope.launchWhenStarted {
            if (notificationsViewModel.backOnline) {
                notificationsViewModel.getNotifications(
                    notificationQuery = mapOf(
                        "type" to Constants.listProductTypes[binding.notificationsTabLayout.selectedTabPosition].uppercase(
                            Locale.getDefault()
                        )
                    )
                ).collectLatest { data ->
                    selectedTab = Constants.listProductTypes[binding.notificationsTabLayout.selectedTabPosition]
                    submitProductAdapterData(data = data)
                }
            }

        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarNotificationsFragment)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

        binding.toolbarNotificationsFragment.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun showSnackBar(message: String, status: Int, icon: Int) {
        val drawable = ResourcesCompat.getDrawable(resources, icon, null)

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
            .setCustomBackground(ResourcesCompat.getDrawable(resources, R.drawable.snackbar_normal_custom_bg, null)!!)

        snackBar.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}