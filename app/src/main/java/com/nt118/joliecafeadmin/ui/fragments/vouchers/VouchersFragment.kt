package com.nt118.joliecafeadmin.ui.fragments.vouchers

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.adapter.VoucherAdapter
import com.nt118.joliecafeadmin.databinding.FragmentVouchersBinding
import com.nt118.joliecafeadmin.models.Voucher
import com.nt118.joliecafeadmin.ui.activities.add_voucher.AddVoucherActivity
import com.nt118.joliecafeadmin.util.ApiResult
import com.nt118.joliecafeadmin.util.Constants
import com.nt118.joliecafeadmin.util.Constants.Companion.SNACK_BAR_STATUS_ERROR
import com.nt118.joliecafeadmin.util.Constants.Companion.SNACK_BAR_STATUS_SUCCESS
import com.nt118.joliecafeadmin.util.Constants.Companion.listVoucherTypes
import com.nt118.joliecafeadmin.util.MarginItemDecoration
import com.nt118.joliecafeadmin.util.NetworkListener
import com.nt118.joliecafeadmin.util.extenstions.setCustomBackground
import com.nt118.joliecafeadmin.util.extenstions.setIcon
import com.nt118.joliecafeadmin.viewmodels.VouchersViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VouchersFragment : Fragment() {

    private var _binding: FragmentVouchersBinding? = null
    private val binding get() = _binding!!
    private val vouchersViewModel: VouchersViewModel by viewModels()
    private lateinit var discountAdapter: VoucherAdapter
    private lateinit var shipAdapter: VoucherAdapter
    private lateinit var networkListener: NetworkListener

    private var selectedTab
        get() = vouchersViewModel.selectedTab.value?: 0
        set(value) { vouchersViewModel.selectedTab.value = value }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentVouchersBinding.inflate(inflater, container, false)
        initListeners()
//        recyclerViewVoucher()

        updateNetworkStatus()
        updateBackOnlineStatus()
        handleSearchView()
        observeNetworkMessage()
        observeGetVoucherResponse()
        observeDeleteVoucherResponse()
        observeVoucherList()
        observeTabSelection()
        initTabSelection()
        observeSelectedTab()
        setMarginForRvItem()

        return binding.root
    }

    private fun handleSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    val adapter = binding.recycleViewVoucher.adapter as VoucherAdapter
                    val filteredDataset = vouchersViewModel.voucherList.value?.filter {
                        it.code.lowercase().contains(newText.toString().lowercase()) && it.type == listVoucherTypes[selectedTab]
                    } ?: emptyList()
                    println(filteredDataset.toString())
                    adapter.fetchData(filteredDataset)
                } else {
                    val adapter = binding.recycleViewVoucher.adapter as VoucherAdapter
                    adapter.fetchData(vouchersViewModel.voucherList.value?.filter{
                        it.type == listVoucherTypes[selectedTab]
                    } ?: emptyList())
                }
                return false
            }
        })
    }

    private fun observeDeleteVoucherResponse() {
        vouchersViewModel.deleteVoucherResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ApiResult.Loading -> binding.progressIndicator.visibility = View.VISIBLE
                is ApiResult.NullDataSuccess -> {
                    (binding.recycleViewVoucher.adapter as VoucherAdapter).deleteItem()
                    showSnackBar("Voucher deleted successfully", SNACK_BAR_STATUS_SUCCESS, R.drawable.ic_success)
                    binding.progressIndicator.visibility = View.GONE
                }
                is ApiResult.Error -> {
                    showSnackBar("Failed to delete voucher", SNACK_BAR_STATUS_ERROR, R.drawable.ic_error)
                }
                else -> {}
            }
        }
    }

    private fun setMarginForRvItem() {
        binding.recycleViewVoucher.addItemDecoration(MarginItemDecoration(resources.getDimensionPixelSize(R.dimen.medium_margin)))
    }

    private fun observeSelectedTab() {
        vouchersViewModel.selectedTab.observe(viewLifecycleOwner) { tab ->
            when (tab) {
                VouchersViewModel.DISCOUNT_TAB -> binding.recycleViewVoucher.adapter = discountAdapter
                VouchersViewModel.SHIPPING_TAB -> binding.recycleViewVoucher.adapter = shipAdapter
            }
        }
    }

    private fun observeTabSelection() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                //
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedTab = tab?.position?: 0
            }
        })
    }

    private fun observeVoucherList() {
        vouchersViewModel.voucherList.observe(viewLifecycleOwner) { data ->
            discountAdapter = VoucherAdapter(data.filter { it.type == listVoucherTypes[0] } as MutableList<Voucher>, requireContext(), vouchersViewModel)
            shipAdapter = VoucherAdapter(data.filter { it.type == listVoucherTypes[1] } as MutableList<Voucher>, requireContext(), vouchersViewModel)
            when (selectedTab) {
                VouchersViewModel.DISCOUNT_TAB -> binding.recycleViewVoucher.adapter = discountAdapter
                VouchersViewModel.SHIPPING_TAB -> binding.recycleViewVoucher.adapter = shipAdapter
            }
        }
    }

    private fun observeGetVoucherResponse() {
        vouchersViewModel.getAllVouchersResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ApiResult.Loading -> {
                    binding.progressIndicator.visibility = View.VISIBLE
                }
                is ApiResult.Success -> {
                    binding.progressIndicator.visibility = View.GONE
                    vouchersViewModel.voucherList.value = response.data
                }
                is ApiResult.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    showSnackBar("Failed to get vouchers", SNACK_BAR_STATUS_ERROR, R.drawable.ic_error)
                }
                else -> {}
            }
        }
    }

    private fun initTabSelection() {
        binding.tabLayout.getTabAt(selectedTab)?.select()
    }

    private fun observeNetworkMessage() {
        vouchersViewModel.networkMessage.observe(viewLifecycleOwner) { message ->
            if (!vouchersViewModel.networkStatus) {
                showSnackBar(
                    message = message,
                    status = Constants.SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            } else if (vouchersViewModel.networkStatus) {
                if (vouchersViewModel.backOnline) {
                    showSnackBar(
                        message = message,
                        status = Constants.SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_wifi
                    )
                }
            }
        }
    }

    private fun showSnackBar(message: String, status: Int, icon: Int) {
        val drawable = requireContext().getDrawable(icon)

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
            .setCustomBackground(requireContext().getDrawable(R.drawable.snackbar_normal_custom_bg)!!)

        snackBar.show()
    }

    private fun updateBackOnlineStatus() {
        vouchersViewModel.readBackOnline.asLiveData().observe(viewLifecycleOwner) { status ->
            vouchersViewModel.backOnline = status
        }
    }

    private fun updateNetworkStatus() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(requireContext())
            .asLiveData().observe(viewLifecycleOwner) { status ->
                vouchersViewModel.networkStatus = status
                vouchersViewModel.showNetworkStatus()
            }
    }

    private fun initListeners() {
        binding.btnAddVoucher.setOnClickListener {
            startActivity(Intent(context, AddVoucherActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetDataBestSeller() : ArrayList<String> {
        val item = ArrayList<String>()
        for (i in 0 until 15) {
            item.add("$i")
        }
        return item
    }

    override fun onResume() {
        super.onResume()
        if (vouchersViewModel.networkStatus) {
            vouchersViewModel.getAllVouchers()
        }
    }
}