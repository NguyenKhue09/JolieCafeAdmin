package com.nt118.joliecafeadmin.ui.fragments.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.nt118.joliecafeadmin.adapter.ProductItemAdapter
import com.nt118.joliecafeadmin.databinding.FragmentProductsBinding
import com.nt118.joliecafeadmin.models.Product
import com.nt118.joliecafeadmin.util.Constants.Companion.listProductTypes
import com.nt118.joliecafeadmin.util.NetworkListener
import com.nt118.joliecafeadmin.util.ProductComparator
import com.nt118.joliecafeadmin.util.extenstions.observeOnce
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



        initProductAdapter()
        setProductAdapterData()
        updateBackOnlineStatus()
        configProductRecyclerView()
        addProductTabText()
        onProductsTabSelected()

        initProductAdapterData()
        handleProductPagingAdapterState()
        return binding.root
    }

    private fun initProductAdapterData() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(requireContext())
            .asLiveData().observeOnce(viewLifecycleOwner) { status ->
                productsViewModel.networkStatus = status
                if (productsViewModel.networkStatus) {
                    lifecycleScope.launchWhenStarted {
                        println("call init daa")
                        productsViewModel.getProducts(
                            productQuery = mapOf(
                                "type" to listProductTypes[0]
                            ),
                        ).collectLatest { data ->
                            selectedTab = listProductTypes[0]
                            submitProductAdapterData(data = data)
                        }
                    }
                } else {
                    productsViewModel.showNetworkStatus()
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
        if(productsViewModel.networkStatus) {
            val action = ProductsFragmentDirections.actionNavigationProductsToProductDetailActivity(
                productId = productId,
                isEdit = isEdit
            )
            findNavController().navigate(action)
        }
    }

    private fun setProductAdapterData() {
        lifecycleScope.launchWhenStarted {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(requireContext())
                .collect { status ->
                    productsViewModel.networkStatus = status
                    productsViewModel.showNetworkStatus()
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
            } else {
                productsViewModel.showNetworkStatus()
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
            if (loadState.refresh is LoadState.Loading){
                binding.productCircularProgressIndicator.visibility = View.VISIBLE
            }
            else{
                binding.productCircularProgressIndicator.visibility = View.GONE
                // getting the error
                val error = when {
                    loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                    loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                    loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                    else -> null
                }
                error?.let {
                    Toast.makeText(requireContext(), it.error.message, Toast.LENGTH_LONG).show()
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