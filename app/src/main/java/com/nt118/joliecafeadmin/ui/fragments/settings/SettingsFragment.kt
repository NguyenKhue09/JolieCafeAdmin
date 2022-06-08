package com.nt118.joliecafeadmin.ui.fragments.settings

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.nt118.joliecafeadmin.databinding.FragmentSettingsBinding
import com.nt118.joliecafeadmin.ui.activities.bill.BillsActivity
import com.nt118.joliecafeadmin.ui.activities.login.LoginActivity
import com.nt118.joliecafeadmin.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.btnLogout.setOnClickListener {
            settingsViewModel.saveAdminToken(adminToken = "")
            if(settingsViewModel.adminToken.isEmpty()) {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            }

        }

        binding.btnBill.setOnClickListener {
            startActivity(Intent(requireContext(), BillsActivity::class.java))
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}