package ru.tgmaksim.gymnasium.ui

import android.view.View
import android.os.Bundle
import android.widget.Toast
import android.content.Context
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.api.Login
import ru.tgmaksim.gymnasium.databinding.LayoutLoginBinding
import ru.tgmaksim.gymnasium.utilities.Utilities

class LoginActivity : ParentActivity() {
    private lateinit var ui: LayoutLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Устанавливается сохраненная тема
        setActivityTheme()
        super.onCreate(savedInstanceState)

        ui = LayoutLoginBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // Настройка системных полей сверху и снизу
        setupSystemBars(ui.contentContainer)

        val context: Context = this
        ui.buttonLogin.setOnClickListener {
            lifecycleScope.launch {
                showLoading()

                try {
                    Login.login(context)
                    finish()
                } catch (e: Exception) {
                    Utilities.log(e)
                    Toast.makeText(
                        context,
                        R.string.error_login,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                hideLoading()
            }
        }

        // Заранее получается ссылка для входа
        showLoading()
        lifecycleScope.launch {
            try {
                Login.prepareLogin()
            } catch (e: Exception) {
                Utilities.log(e)
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