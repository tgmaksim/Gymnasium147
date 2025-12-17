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
    companion object {
        var loginUrl: String? = null
    }

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
                login()
                hideLoading()
            }
        }

        // Заранее получение ссылка для авторизации
        lifecycleScope.launch {
            showLoading()
            prepareLogin()
            hideLoading()
        }
    }

    private suspend fun login() {
        try {
            val response = Login.login()

            if (!response.status || response.answer == null) {
                response.error?.let { error ->
                    Utilities.log(error.type)

                    (error.errorMessage ?: "Произошла ошибка на сервере").let { errorMessage ->
                        Utilities.showText(this, errorMessage)
                    }
                }

                return
            }

            Utilities.openUrl(this, response.answer.loginUrl)
            finish()
        } catch (e: Exception) {
            Utilities.log(e)
            if (!Request.checkInternet())
                Utilities.showText(this, R.string.error_internet)
            else
                Utilities.showText(this, R.string.error_login)
        }
    }

    private suspend fun prepareLogin() {
        try {
            val response = Login.login()

            loginUrl = response.answer?.loginUrl
        } catch (e: Exception) {
            Utilities.log(e)
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