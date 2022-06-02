package com.nt118.joliecafeadmin.ui.fragments.notification_detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.FragmentNotificationDetailBinding

class NotificationDetailFragment : Fragment() {

    private var _binding: FragmentNotificationDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}