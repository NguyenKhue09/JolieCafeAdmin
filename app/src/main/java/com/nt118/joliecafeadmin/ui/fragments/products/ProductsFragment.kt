package com.nt118.joliecafeadmin.ui.fragments.products

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.adapter.ProductItemAdapter
import com.nt118.joliecafeadmin.databinding.FragmentProductsBinding
import com.nt118.joliecafeadmin.models.Product
import com.nt118.joliecafeadmin.ui.activities.add_product.AddNewProductActivity
import com.nt118.joliecafeadmin.util.Constants
import com.nt118.joliecafeadmin.util.Constants.Companion.SNACK_BAR_STATUS_ERROR
import com.nt118.joliecafeadmin.util.Constants.Companion.listProductTypes
import com.nt118.joliecafeadmin.util.NetworkListener
import com.nt118.joliecafeadmin.util.ProductComparator
import com.nt118.joliecafeadmin.util.extenstions.observeOnce
import com.nt118.joliecafeadmin.util.extenstions.setCustomBackground
import com.nt118.joliecafeadmin.util.extenstions.setIcon
import com.nt118.joliecafeadmin.viewmodels.ProductsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!
    private val productsViewModel: ProductsViewModel by viewModels()

    private lateinit var productItemAdapter: ProductItemAdapter
    private lateinit var selectedTab: String
    private lateinit var networkListener: NetworkListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // check user

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)

        updateNetworkStatus()
        updateBackOnlineStatus()
        observerNetworkMessage()

        initProductAdapter()
        initProductAdapterData()
        configProductRecyclerView()
        addProductTabText()
        setProductAdapterDataWhenTabChange()
        onProductsTabSelected()
        handleProductPagingAdapterState()
        navigateToAddNewProductScreen()

        return binding.root
    }

    private fun observerNetworkMessage() {
        productsViewModel.networkMessage.observe(viewLifecycleOwner) { message ->
            if (!productsViewModel.networkStatus) {
                showSnackBar(
                    message = message,
                    status = Constants.SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            } else if (productsViewModel.networkStatus) {
                if (productsViewModel.backOnline) {
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
        networkListener.checkNetworkAvailability(requireContext())
            .asLiveData().observe(viewLifecycleOwner) { status ->
                productsViewModel.networkStatus = status
                productsViewModel.showNetworkStatus()
                backOnlineRecallFavoriteProducts()
            }
    }

    private fun showSnackBar(message: String, status: Int, icon: Int) {
        val drawable = requireContext().getDrawable(icon)

        val snackBarContentColor = when (status) {
            Constants.SNACK_BAR_STATUS_SUCCESS -> R.color.text_color_2
            Constants.SNACK_BAR_STATUS_DISABLE -> R.color.dark_text_color
            SNACK_BAR_STATUS_ERROR -> R.color.error_color
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

    private fun navigateToAddNewProductScreen() {
        binding.btnAddNewProduct.setOnClickListener {
            startActivity(Intent(context, AddNewProductActivity::class.java))
        }
    }

    private fun initProductAdapterData() {
        networkListener.checkNetworkAvailability(requireContext())
            .asLiveData().observeOnce(viewLifecycleOwner) { status ->
                productsViewModel.networkStatus = status
                if (productsViewModel.networkStatus) {
                    lifecycleScope.launchWhenStarted {
                        println("call init daaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                        productsViewModel.getProducts(
                            productQuery = mapOf(
                                "type" to listProductTypes[0]
                            ),
                        ).collectLatest { data ->
                            selectedTab = listProductTypes[0]
                            submitProductAdapterData(data = data)
                        }
                    }
                }
            }
    }

    private fun updateBackOnlineStatus() {
        productsViewModel.readBackOnline.asLiveData().observe(viewLifecycleOwner) { status ->
            productsViewModel.backOnline = status
        }
    }

    private fun initProductAdapter() {
        val diffUtil = ProductComparator
        productItemAdapter = ProductItemAdapter(
            productFragment = this,
            diffUtil = diffUtil,
            onProductClicked = { productId ->
                onProductClicked(productId = productId, isEdit = false)
            },
            onEditProductClicked = { productId ->
                onProductClicked(productId = productId, isEdit = true)
            }
        )
    }

    private fun onProductClicked(productId: String, isEdit: Boolean) {
        if (productsViewModel.networkStatus) {
            val action = ProductsFragmentDirections.actionNavigationProductsToProductDetailActivity(
                productId = productId,
                isEdit = isEdit
            )
            findNavController().navigate(action)
        }
    }

    private fun backOnlineRecallFavoriteProducts() {
        lifecycleScope.launchWhenStarted {
            if (productsViewModel.backOnline) {
                productsViewModel.getProducts(
                    productQuery = mapOf(
                        "type" to listProductTypes[binding.productsTabLayout.selectedTabPosition]
                    )
                ).collectLatest { data ->
                    selectedTab = listProductTypes[binding.productsTabLayout.selectedTabPosition]
                    submitProductAdapterData(data = data)
                }
            }

        }
    }

    private fun setProductAdapterDataWhenTabChange() {
        productsViewModel.tabSelected.observe(viewLifecycleOwner) { tab ->
            if (productsViewModel.networkStatus) {
                lifecycleScope.launchWhenStarted {
                    productsViewModel.getProducts(
                        productQuery = mapOf(
                            "type" to tab
                        )
                    ).collectLatest { data ->
                        selectedTab = tab
                        submitProductAdapterData(data = data)
                    }
                }
            }
        }
    }

    private fun configProductRecyclerView() {
        val productRV = binding.rvProductItem
        productRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        productRV.adapter = productItemAdapter
    }

    private suspend fun submitProductAdapterData(data: PagingData<Product>) {
        productItemAdapter.submitData(data)
    }

    private fun handleProductPagingAdapterState() {
        productItemAdapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading) {
                binding.productCircularProgressIndicator.visibility = View.VISIBLE
            } else {
                binding.productCircularProgressIndicator.visibility = View.GONE
                // getting the error
                val error = when {
                    loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                    loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                    loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                    else -> null
                }
                error?.let {
                    if (productsViewModel.networkStatus)
                        showSnackBar(
                            message = "Can't load product data!",
                            status = SNACK_BAR_STATUS_ERROR,
                            icon = R.drawable.ic_error
                        )
                }
            }
        }
    }

    private fun onProductsTabSelected() {
        binding.productsTabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    productsViewModel.setTabSelected(tab = tab.text.toString())
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun addProductTabText() {
        listProductTypes.forEach {
            binding.productsTabLayout.addTab(binding.productsTabLayout.newTab().apply {
                tag = it
                text = it
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}