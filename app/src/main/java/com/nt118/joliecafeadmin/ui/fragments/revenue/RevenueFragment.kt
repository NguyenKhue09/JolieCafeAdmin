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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.adapter.BestSellerAdapter
import com.nt118.joliecafeadmin.databinding.FragmentRevenueBinding
import com.nt118.joliecafeadmin.util.ScreenUtils.spToPx
import com.nt118.joliecafeadmin.viewmodels.RevenueViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RevenueFragment : Fragment() {

    private var _binding: FragmentRevenueBinding? = null
    private val binding get() = _binding!!
    private val revenueViewModel: RevenueViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRevenueBinding.inflate(inflater, container, false)

        // Views
        val rvBestSeller = binding.rvBestSeller
        val chart = binding.chart

        initData(chart, requireContext())

        rvBestSeller.adapter = BestSellerAdapter()

        println(revenueViewModel.adminToken)

        return binding.root
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