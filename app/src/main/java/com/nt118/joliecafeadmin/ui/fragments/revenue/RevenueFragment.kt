package com.nt118.joliecafeadmin.ui.fragments.revenue

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.tabs.TabLayout
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.adapter.BestSellerAdapter
import com.nt118.joliecafeadmin.databinding.FragmentRevenueBinding
import com.nt118.joliecafeadmin.models.MonthlyRevenue
import com.nt118.joliecafeadmin.models.WeeklyRevenue
import com.nt118.joliecafeadmin.ui.activities.notifications.NotificationsActivity
import com.nt118.joliecafeadmin.util.ApiResult
import com.nt118.joliecafeadmin.util.Constants
import com.nt118.joliecafeadmin.util.NetworkListener
import com.nt118.joliecafeadmin.util.extenstions.setCustomBackground
import com.nt118.joliecafeadmin.util.extenstions.setIcon
import com.nt118.joliecafeadmin.viewmodels.RevenueViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class RevenueFragment : Fragment() {

    private var _binding: FragmentRevenueBinding? = null
    private val binding get() = _binding!!
    private val revenueViewModel: RevenueViewModel by viewModels()
    private lateinit var revenueProgressIndicator: CircularProgressIndicator
    private lateinit var bestSellerProgressIndicator: CircularProgressIndicator
    private lateinit var networkListener: NetworkListener
    private lateinit var rvBestSeller: RecyclerView
    private lateinit var chart: LineChart
    private lateinit var tabLayout: TabLayout

    // backing field
    private var selectedTab
        get() = revenueViewModel.selectedTab.value ?: 0
        set(value) {
            revenueViewModel.selectedTab.value = value
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRevenueBinding.inflate(inflater, container, false)

        updateNetworkStatus()
        observerNetworkMessage()
        initViews()
        observe()
        buttonClickListener()
        tabChangeListener()
        initDefaultData()

        return binding.root
    }

    private fun initDefaultData() {
        when (selectedTab) {
            RevenueViewModel.WEEKLY_REVENUE -> revenueViewModel.getWeeklyRevenue()
            RevenueViewModel.MONTHLY_REVENUE -> revenueViewModel.getMonthlyRevenue()
        }
        revenueViewModel.getBestSeller()
    }

    private fun tabChangeListener() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                // do nothing
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // do nothing
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedTab = tab?.position ?: 0
            }
        })
    }

    private fun updateNetworkStatus() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(requireContext())
            .asLiveData().observe(viewLifecycleOwner) { status ->
                revenueViewModel.networkStatus = status
                revenueViewModel.showNetworkStatus()
                initDefaultData()
            }
    }

    private fun observe() = revenueViewModel.apply {
        monthlyRevenueResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ApiResult.Loading -> {
                    revenueProgressIndicator.visibility = View.VISIBLE
                }
                is ApiResult.Success -> {
                    revenueProgressIndicator.visibility = View.GONE
                    initMonthlyData(chart, requireContext(), response.data!!)
                }
                is ApiResult.Error -> {

                }
                else -> {}
            }
        }

        weeklyRevenueResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ApiResult.Loading -> {
                    revenueProgressIndicator.visibility = View.VISIBLE
                }
                is ApiResult.Success -> {
                    revenueProgressIndicator.visibility = View.GONE
                    initWeeklyData(chart, requireContext(), response.data!!)
                }
                is ApiResult.Error -> {

                }
                else -> {}
            }
        }

        bestSellerResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ApiResult.Loading -> {
                    bestSellerProgressIndicator.visibility = View.VISIBLE
                }
                is ApiResult.Success -> {
                    bestSellerProgressIndicator.visibility = View.GONE
                    rvBestSeller.adapter = BestSellerAdapter(response.data!!, requireContext())
                }
                is ApiResult.Error -> {}
                else -> {}
            }
        }

        selectedTab.observe(viewLifecycleOwner) { tab ->
            when (tab) {
                RevenueViewModel.WEEKLY_REVENUE -> {
                    revenueViewModel.getWeeklyRevenue()
                }
                RevenueViewModel.MONTHLY_REVENUE -> {
                    revenueViewModel.getMonthlyRevenue()
                }
                else -> {}
            }
        }
    }

    private fun initWeeklyData(chart: LineChart, context: Context, data: List<WeeklyRevenue>) {
        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)

        val xAxisValue = ArrayList<String>()
        for (i in currentWeek - 7 until currentWeek) {
            xAxisValue.add("$i")
        }

        val entries = ArrayList<Entry>()

        for (i in currentWeek - 7 until currentWeek) {
            if (data.none { it.week == i }) {
                entries.add(Entry((i - currentWeek + 7).toFloat(), 0f))
            } else {
                entries.add(Entry((i - currentWeek + 7).toFloat(), data.first { it.week == i }.totalCostOfWeek / 1000000f))
            }
        }

        setupChart(chart, context, entries, xAxisValue)
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
            startActivity(Intent(requireContext(), NotificationsActivity::class.java))
        }
    }

    private fun initViews() {
        // Views
        rvBestSeller = binding.rvBestSeller
        chart = binding.chart
        revenueProgressIndicator = binding.revenueProgressIndicator
        bestSellerProgressIndicator = binding.bestSellerProgressIndicator
        tabLayout = binding.tabLayout

        tabLayout.getTabAt(selectedTab)?.select()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initMonthlyData(chart: LineChart, context: Context, data: List<MonthlyRevenue>) {
        val months = arrayListOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")
        val entries = mutableListOf<Entry>()

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)

        // init x axis
        val xAxisValue = ArrayList(months.subList(currentMonth, months.size))
        xAxisValue.addAll(months.subList(0, currentMonth))

        for (i in 0..11) {
            if (data.none { it.month == (i + currentMonth) % 12 }) {
                entries.add(Entry(i.toFloat(), 0f))
            } else {
                entries.add(Entry(i.toFloat(), data.first { it.month == (i + currentMonth) % 12 }.totalCostOfMonth / 1000000f))
            }
        }

        setupChart(chart, context, entries, xAxisValue)
    }

    private fun setupChart(chart: LineChart, context: Context, entries: MutableList<Entry>, xAxisValue: ArrayList<String>) {
        val dataSet = LineDataSet(entries, "Income")

        println(xAxisValue.toString())

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
//        chart.xAxis.setCenterAxisLabels(true)
        chart.xAxis.setLabelCount(xAxisValue.size, true)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValue)
        chart.xAxis.axisLineColor = ContextCompat.getColor(context, R.color.grey_primary_variant)
        chart.xAxis.axisLineWidth = 2f

        // Y Axis
        chart.axisLeft.textColor = ContextCompat.getColor(context, R.color.grey_primary_variant)
        chart.axisLeft.textSize = 12f
        chart.axisLeft.granularity = 0.2f
        chart.axisLeft.typeface = setFontFace("montserrat_regular.ttf")
        chart.axisLeft.textColor = ContextCompat.getColor(context, R.color.grey_primary_variant)
        chart.axisRight.isEnabled = false
//        chart.axisLeft.setDrawGridLines(false)
        chart.axisLeft.axisLineWidth = 2f
        chart.axisLeft.axisLineColor = ContextCompat.getColor(context, R.color.grey_primary_variant)

        chart.invalidate()
    }

    private fun setFontFace(font: String): Typeface {
        return Typeface.createFromAsset(requireContext().assets, font)
    }
}