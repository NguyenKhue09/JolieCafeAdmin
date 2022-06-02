package com.nt118.joliecafeadmin.ui.fragments.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import com.google.android.material.snackbar.Snackbar
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.FragmentNotificationsBinding
import com.nt118.joliecafeadmin.util.Constants
import com.nt118.joliecafeadmin.util.NetworkListener
import com.nt118.joliecafeadmin.util.extenstions.setCustomBackground
import com.nt118.joliecafeadmin.util.extenstions.setIcon
import com.nt118.joliecafeadmin.viewmodels.NotificationsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val notificationsViewModel: NotificationsViewModel by viewModels()

    private lateinit var networkListener: NetworkListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(layoutInflater, container, false)

        setupActionBar()

        updateNetworkStatus()
        updateBackOnlineStatus()
        observerNetworkMessage()

        return binding.root
    }

    private fun onAddNotificationButtonClicked() {
        binding.btnCreateNewNotification.setOnClickListener {
            
        }
    }

    private fun observerNetworkMessage() {
        notificationsViewModel.networkMessage.observe(viewLifecycleOwner) { message ->
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
        notificationsViewModel.readBackOnline.asLiveData().observe(viewLifecycleOwner) { status ->
            notificationsViewModel.backOnline = status
        }
    }

    private fun updateNetworkStatus() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(requireContext())
            .asLiveData().observe(viewLifecycleOwner) { status ->
                notificationsViewModel.networkStatus = status
                notificationsViewModel.showNetworkStatus()
            }
    }

    private fun setupActionBar() {
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbarNotificationsFragment)

        val actionBar =  (activity as AppCompatActivity?)!!.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

        binding.toolbarNotificationsFragment.setNavigationOnClickListener { (activity as AppCompatActivity?)!!.onBackPressed() }
    }

    private fun showSnackBar(message: String, status: Int, icon: Int) {
        val drawable = ResourcesCompat.getDrawable(requireActivity().resources, icon, null)

        val snackBarContentColor = when (status) {
            Constants.SNACK_BAR_STATUS_SUCCESS -> R.color.text_color_2
            Constants.SNACK_BAR_STATUS_DISABLE -> R.color.dark_text_color
            Constants.SNACK_BAR_STATUS_ERROR -> R.color.error_color
            else -> R.color.text_color_2
        }


        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Ok") {
            }
            .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.grey_primary))
            .setTextColor(ContextCompat.getColor(requireContext(), snackBarContentColor))
            .setIcon(
                drawable = drawable!!,
                colorTint = ContextCompat.getColor(requireContext(), snackBarContentColor),
                iconPadding = resources.getDimensionPixelOffset(R.dimen.small_margin)
            )
            .setCustomBackground(ResourcesCompat.getDrawable(requireActivity().resources, R.drawable.snackbar_normal_custom_bg, null)!!)

        snackBar.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}