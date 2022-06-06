package com.nt118.joliecafeadmin.ui.activities.notifications

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nt118.joliecafeadmin.databinding.ActivityNotificationBinding
import com.nt118.joliecafeadmin.util.Constants.Companion.ACTION_TYPE
import com.nt118.joliecafeadmin.util.Constants.Companion.ACTION_TYPE_ADD
import com.nt118.joliecafeadmin.util.Constants.Companion.ACTION_TYPE_EDIT
import com.nt118.joliecafeadmin.util.Constants.Companion.ACTION_TYPE_VIEW

class NotificationActivity : AppCompatActivity() {

    private var _binding: ActivityNotificationBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackButton()

        getValueFromIntent()
    }

    private fun getValueFromIntent() {
        val actionType = intent.extras?.getInt(ACTION_TYPE)

        actionType?.let {
            when (it) {
                ACTION_TYPE_ADD -> {
                    println("ACTION_TYPE_ADD")
                }
                ACTION_TYPE_EDIT -> {
                    println("ACTION_TYPE_EDIT")
                }
                ACTION_TYPE_VIEW -> {
                    println("ACTION_TYPE_VIEW")
                }
            }
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }
}