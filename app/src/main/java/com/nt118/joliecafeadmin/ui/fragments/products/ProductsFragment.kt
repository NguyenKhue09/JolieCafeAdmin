package com.nt118.joliecafeadmin.ui.fragments.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.nt118.joliecafeadmin.adapter.ProductItemAdapter
import com.nt118.joliecafeadmin.databinding.FragmentProductsBinding
import com.nt118.joliecafeadmin.models.Product
import com.nt118.joliecafeadmin.util.Constants.Companion.listProductTypes
import com.nt118.joliecafeadmin.util.ProductComparator
import com.nt118.joliecafeadmin.viewmodels.ProductsViewModel

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!
    private val productsViewModel: ProductsViewModel by viewModels()

    private lateinit var productItemAdapter: ProductItemAdapter
    private lateinit var selectedTab: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)

        initProductAdapter()
        configProductRecyclerView()
        addProductTabText()
        onProductsTabSelected()

        return binding.root
    }

    private fun initProductAdapter() {
        val diffUtil = ProductComparator
        productItemAdapter = ProductItemAdapter(
            productFragment = this,
            diffUtil = diffUtil
        )
    }

    private fun configProductRecyclerView() {
        val productRV = binding.rvProductItem
        productRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        productRV.adapter = productItemAdapter
    }

    private suspend fun submitFavoriteData(data: PagingData<Product>) {
        productItemAdapter.submitData(data)
    }

    private fun onProductsTabSelected() {
        binding.productsTabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    //favoriteViewModel.setTabSelected(tab = tab.text.toString())
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