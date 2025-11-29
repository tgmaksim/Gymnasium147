package ru.tgmaksim.gymnasium.ui

import android.util.Log
import android.view.View
import android.os.Bundle
import android.widget.Toast
import android.content.Context
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.api.Login
import ru.tgmaksim.gymnasium.utilities.CacheManager
import ru.tgmaksim.gymnasium.databinding.LayoutLoginBinding

class LoginActivity : ParentActivity() {
    private lateinit var ui: LayoutLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        CacheManager.init(this)

        // Устанавливается сохраненная тема
        setActivityTheme()
        super.onCreate(savedInstanceState)

        ui = LayoutLoginBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // Настройка системных полей сверху и снизу
        setupSystemBars(ui.contentContainer)

        val context: Context = this
        ui.btnLogin.setOnClickListener {
            showLoading()
            lifecycleScope.launch {
                try {
                    Login.login(context)
                    finish()
                } catch (e: Exception) {
                    Log.e("api-error", null, e)
                    Toast.makeText(
                        context,
                        R.string.error_login,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            hideLoading()
        }

        // Заранее получается ссылка для входа
        showLoading()
        lifecycleScope.launch {
            try {
                Login.prepareLogin()
            } catch (e: Exception) {
                Log.e("api-error", null, e)
            }
        }
        hideLoading()
    }

    private fun showLoading() {
        ui.loadingOverlay.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        ui.loadingOverlay.visibility = View.GONE
    }
}