package com.nt118.joliecafeadmin.ui.activities.add_product

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nt118.joliecafeadmin.databinding.ActivityAddNewProductBinding

class AddNewProductActivity : AppCompatActivity() {

    private var _binding: ActivityAddNewProductBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAddNewProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}