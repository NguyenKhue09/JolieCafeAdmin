package com.nt118.joliecafeadmin.ui.activities.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nt118.joliecafeadmin.MainActivity
import com.nt118.joliecafeadmin.R
import com.nt118.joliecafeadmin.databinding.ActivityLoginBinding
import com.nt118.joliecafeadmin.ui.fragments.revenue.RevenueFragment
import com.nt118.joliecafeadmin.util.ApiResult
import com.nt118.joliecafeadmin.util.Constants
import com.nt118.joliecafeadmin.util.NetworkListener
import com.nt118.joliecafeadmin.util.extenstions.setCustomBackground
import com.nt118.joliecafeadmin.util.extenstions.setIcon
import com.nt118.joliecafeadmin.viewmodels.LoginViewModel
import com.nt118.joliecafeadmin.viewmodels.ProductsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val loginViewModel : LoginViewModel by viewModels()

    private var _binding: ActivityLoginBinding? = null
    private val  binding get() = _binding!!

    private lateinit var networkListener: NetworkListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        updateNetworkStatus()
        updateBackOnlineStatus()
        observerNetworkMessage()

        binding.btnLogin.setOnClickListener {
            if(loginViewModel.networkStatus) {
                loginUserWithUserPassword()
            } else {
                loginViewModel.showNetworkStatus()
            }
        }

        handleApiLoginResponse()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun updateBackOnlineStatus() {
        loginViewModel.readBackOnline.asLiveData().observe(this) { status ->
            loginViewModel.backOnline = status
        }
    }

    private fun observerNetworkMessage() {
        loginViewModel.networkMessage.observe(this) { message ->
            if (!loginViewModel.networkStatus) {
                showSnackBar(
                    message = message,
                    status = Constants.SNACK_BAR_STATUS_DISABLE,
                    icon = R.drawable.ic_wifi_off
                )
            } else if (loginViewModel.networkStatus) {
                if (loginViewModel.backOnline) {
                    showSnackBar(
                        message = message,
                        status = Constants.SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_wifi
                    )
                }
            }
        }
    }

    private fun updateNetworkStatus() {
        networkListener = NetworkListener()
        networkListener.checkNetworkAvailability(this)
            .asLiveData().observe(this) { status ->
                loginViewModel.networkStatus = status
                loginViewModel.showNetworkStatus()
                backOnlineRecallLoginAdmin()
            }
    }

    private fun backOnlineRecallLoginAdmin() {
        lifecycleScope.launchWhenStarted {
            if (loginViewModel.backOnline) {
                if(validateUserName() && validatePassword()) {
                    val username = binding.etUserName.text.toString().trim{it <= ' '}
                    val password = binding.etPassword.text.toString().trim{it <= ' '}

                    val loginInfo = mapOf(
                        "username" to username,
                        "password" to password,
                    )

                    loginViewModel.loginAdmin(loginInfo)
                }
            }

        }
    }

    private fun loginUserWithUserPassword() {
        if(validateUserName() && validatePassword()) {
            val username = binding.etUserName.text.toString().trim{it <= ' '}
            val password = binding.etPassword.text.toString().trim{it <= ' '}

            val loginInfo = mapOf(
                "username" to username,
                "password" to password,
            )

            loginViewModel.loginAdmin(loginInfo)
        }
    }

    private fun handleApiLoginResponse() {
        loginViewModel.adminLogin.observe(this) { response ->
            Log.d("Bottom Shit", "handleApiResponse: call")
            when (response) {
                is ApiResult.Loading -> {

                }
                is ApiResult.Success -> {
                    showSnackBar(
                        message = "Login successfully",
                        status = Constants.SNACK_BAR_STATUS_SUCCESS,
                        icon = R.drawable.ic_success
                    )
                    val data = response.data!!
                    data.let {
                        loginViewModel.saveAdminToken(it.token)
                    }
                    println(loginViewModel.adminToken)

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is ApiResult.Error -> {
                    showSnackBar(
                        message = "Login failed!",
                        status = Constants.SNACK_BAR_STATUS_ERROR,
                        icon = R.drawable.ic_error
                    )
                }
                else -> {}
            }
        }
    }

    private fun validateUserName(): Boolean {
        val email = binding.etUserName.text.toString().trim{it <= ' '}

        if (email.isEmpty()) {
            binding.etUserName.requestFocus()
            binding.etUserNameLayout.error = "You must enter your user name!"
            return false
        }
        return true
    }

    private fun validatePassword(): Boolean {
        val password = binding.etPassword.text.toString().trim{it <= ' '}

        if (password.isEmpty()) {
            binding.etPassword.requestFocus()
            binding.etPasswordLayout.error = "You must enter your password!"
            return false
        }
        return true
    }

    private fun showSnackBar(message: String, status: Int, icon: Int) {
        val drawable = this.getDrawable(icon)

        val snackBarContentColor = when (status) {
            Constants.SNACK_BAR_STATUS_SUCCESS -> R.color.text_color_2
            Constants.SNACK_BAR_STATUS_DISABLE -> R.color.dark_text_color
            Constants.SNACK_BAR_STATUS_ERROR -> R.color.error_color
            else -> R.color.text_color_2
        }


        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Ok") {
            }
            .setActionTextColor(ContextCompat.getColor(this, R.color.grey_primary))
            .setTextColor(ContextCompat.getColor(this, snackBarContentColor))
            .setIcon(
                drawable = drawable!!,
                colorTint = ContextCompat.getColor(this, snackBarContentColor),
                iconPadding = resources.getDimensionPixelOffset(R.dimen.small_margin)
            )
            .setCustomBackground(this.getDrawable(R.drawable.snackbar_normal_custom_bg)!!)

        snackBar.show()
    }
}