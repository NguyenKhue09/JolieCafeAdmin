package com.nt118.joliecafeadmin.ui.fragments.revenue

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.FontRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.adapter.BestSellerAdapter
import com.nt118.joliecafeadmin.databinding.FragmentRevenueBinding
import com.nt118.joliecafeadmin.util.ApiResult
import com.nt118.joliecafeadmin.util.Constants
import com.nt118.joliecafeadmin.util.NetworkListener
import com.nt118.joliecafeadmin.util.ScreenUtils.spToPx
import com.nt118.joliecafeadmin.util.extenstions.setCustomBackground
import com.nt118.joliecafeadmin.util.extenstions.setIcon
import com.nt118.joliecafeadmin.viewmodels.RevenueViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RevenueFragment : Fragment() {

    private var _binding: FragmentRevenueBinding? = null
    private val binding get() = _binding!!
    private val revenueViewModel: RevenueViewModel by viewModels()
    private lateinit var rvBestSeller: RecyclerView
    private lateinit var chart: LineChart
    private lateinit var revenueProgressIndicator: CircularProgressIndicator
    private lateinit var networkListener: NetworkListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRevenueBinding.inflate(inflater, container, false)

        updateNetworkStatus()
        observerNetworkMessage()
        initViews()
        initData(chart, requireContext())
        observe()
        buttonClickListener()

        return binding.root
    }

    private fun updateNetworkStatus() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(requireContext())
            .asLiveData().observe(viewLifecycleOwner) { status ->
                revenueViewModel.networkStatus = status
                revenueViewModel.showNetworkStatus()
                revenueViewModel.getRevenue()
            }
    }

    private fun observe() = revenueViewModel.apply {
        revenueResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ApiResult.Loading -> {
                    revenueProgressIndicator.visibility = View.VISIBLE
                }
                is ApiResult.Success -> {
                    revenueProgressIndicator.visibility = View.GONE
                }
                is ApiResult.Error -> {

                }
                else -> {}
            }
        }
    }

    private fun observerNetworkMessage() {
        revenueViewModel.networkMessage.observe(viewLifecycleOwner) { message ->
            if (!revenueViewModel.networkStatus) {
                showSnackBar(
                    message = message,
                    status = Constants.SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            } else if (revenueViewModel.networkStatus) {
                if (revenueViewModel.backOnline) {
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

    private fun buttonClickListener() {
        binding.btnNotifications.setOnClickListener {
            val action = RevenueFragmentDirections.actionNavigationRevenueToNotificationsFragment()
            findNavController().navigate(action)
        }
    }

    private fun initViews() {
        // Views
        rvBestSeller = binding.rvBestSeller
        chart = binding.chart
        revenueProgressIndicator = binding.revenueProgressIndicator

        rvBestSeller.adapter = BestSellerAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initData(chart: LineChart, context: Context) {
        val xAxisValue = arrayListOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val entries = arrayListOf(
            Entry(0f, 3f),
            Entry(1f, 4f),
            Entry(2f, 2f),
            Entry(3f, 4f),
            Entry(4f, 6f),
            Entry(5f, 2f),
            Entry(6f, 4f),
        )

        val dataSet = LineDataSet(entries, "Income")

        dataSet.color = ContextCompat.getColor(context, R.color.grey_primary_variant)
        dataSet.valueTextColor = ContextCompat.getColor(context, R.color.grey_primary_variant)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.lineWidth = 4f
        dataSet.circleRadius = 3f
        dataSet.setDrawValues(false)
        dataSet.circleHoleColor = ContextCompat.getColor(context, R.color.grey_secondary)
        dataSet.setCircleColor(ContextCompat.getColor(context, R.color.grey_secondary))

        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.description.text = "Millions"
        chart.description.typeface = setFontFace("montserrat_regular.ttf")
        chart.description.textSize = 12f
        chart.description.textColor = ContextCompat.getColor(context, R.color.grey_primary_variant)
        chart.description.textAlign = Paint.Align.LEFT
        chart.description.setPosition(30f, 50f)
        chart.legend.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setExtraOffsets(15f, 40f, 15f, 15f)

        // X Axis
        chart.xAxis.textColor = ContextCompat.getColor(context, R.color.grey_primary_variant)
        chart.xAxis.textSize = 12f
        chart.xAxis.typeface = setFontFace("montserrat_regular.ttf")
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.granularity = 1f
        chart.xAxis.setCenterAxisLabels(true)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValue)

        // Y Axis
        chart.axisLeft.textColor = ContextCompat.getColor(context, R.color.grey_primary_variant)
        chart.axisLeft.textSize = 12f
        chart.axisLeft.granularity = 1f
        chart.axisLeft.typeface = setFontFace("montserrat_regular.ttf")
        chart.axisLeft.textColor = ContextCompat.getColor(context, R.color.grey_primary_variant)
        chart.axisRight.isEnabled = false
        chart.axisLeft.setDrawGridLines(false)

        chart.invalidate()
    }

    private fun setFontFace(font: String): Typeface {
        return Typeface.createFromAsset(requireContext().assets, font)
    }
}