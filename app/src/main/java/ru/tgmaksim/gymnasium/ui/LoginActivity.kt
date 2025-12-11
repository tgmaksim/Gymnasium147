package ru.tgmaksim.gymnasium.ui

import android.view.View
import android.os.Bundle
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.api.Login
import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.databinding.LayoutLoginBinding

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

        ui.buttonLogin.setOnClickListener {
            lifecycleScope.launch {
                showLoading()

                try {
                    Login.login(this@LoginActivity)
                    finish()
                } catch (e: Exception) {
                    Utilities.log(e)
                    Utilities.showText(
                        this@LoginActivity,
                        R.string.error_login
                    )
                }

                hideLoading()
            }
        }

        // Заранее получается ссылка для входа
        lifecycleScope.launch {
            showLoading()

            try {
                Login.prepareLogin()
            } catch (e: Exception) {
                Utilities.log(e)
            }

            hideLoading()
        }
    }

    private fun showLoading() {
        ui.loadingOverlay.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        ui.loadingOverlay.visibility = View.GONE
    }
}