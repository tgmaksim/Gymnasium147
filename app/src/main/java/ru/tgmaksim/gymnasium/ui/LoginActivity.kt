package ru.tgmaksim.gymnasium.ui

import android.view.View
import android.os.Bundle
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.api.Login
import ru.tgmaksim.gymnasium.api.Request
import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.databinding.LayoutLoginBinding

/**
 * Activity для авторизации пользователя
 * @author Максим Дрючин (tgmaksim)
 * */
class LoginActivity : ParentActivity() {
    private lateinit var ui: LayoutLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Устанавливается сохраненная тема
        setupActivityTheme()
        super.onCreate(savedInstanceState)

        ui = LayoutLoginBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // Настройка системных полей сверху и снизу
        setupSystemBars(ui.contentContainer)

        // Настройка кнопки входа
        ui.buttonLogin.setOnClickListener {
            lifecycleScope.launch {
                showLoading()

                try {
                    Login.login(this@LoginActivity)
                    finish()
                } catch (e: Exception) {
                    Utilities.log(e)
                    if (!Request.checkInternet())
                        Utilities.showText(this@LoginActivity, R.string.error_internet)
                    else
                        Utilities.showText(this@LoginActivity, R.string.error_login)
                }

                hideLoading()
            }
        }

        // Заранее получение ссылка для авторизации
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

    /**
     * Показ анимации загрузки у кнопки входа
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun showLoading() {
        ui.loadingOverlay.visibility = View.VISIBLE
    }

    /**
     * Скрытие анимации загрузки у кнопки входа
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun hideLoading() {
        ui.loadingOverlay.visibility = View.GONE
    }
}