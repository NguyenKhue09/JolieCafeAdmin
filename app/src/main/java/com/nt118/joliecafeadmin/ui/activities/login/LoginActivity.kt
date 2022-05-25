package com.nt118.joliecafeadmin.ui.activities.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nt118.joliecafeadmin.MainActivity
import com.nt118.joliecafeadmin.databinding.ActivityLoginBinding
import com.nt118.joliecafeadmin.ui.fragments.revenue.RevenueFragment

class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val  binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}