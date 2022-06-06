package com.nt118.joliecafeadmin.ui.activities.notifications

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.asLiveData
import com.google.android.material.snackbar.Snackbar
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.ActivityNotificationsBinding
import com.nt118.joliecafeadmin.util.Constants
import com.nt118.joliecafeadmin.util.NetworkListener
import com.nt118.joliecafeadmin.util.extenstions.setCustomBackground
import com.nt118.joliecafeadmin.util.extenstions.setIcon
import com.nt118.joliecafeadmin.viewmodels.NotificationsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity() {

    private var _binding: ActivityNotificationsBinding? = null
    private val binding get() = _binding!!

    private val notificationsViewModel: NotificationsViewModel by viewModels()

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
    }

    private fun onAddNotificationButtonClicked() {
        binding.btnCreateNewNotification.setOnClickListener {
            val intend = Intent(this, NotificationActivity::class.java)
            intend.putExtra(Constants.ACTION_TYPE, Constants.ACTION_TYPE_ADD)
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