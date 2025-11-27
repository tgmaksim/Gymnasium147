package ru.tgmaksim.gymnasium

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.content.Context
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

import ru.tgmaksim.gymnasium.api.Login
import ru.tgmaksim.gymnasium.utilities.CacheManager
import ru.tgmaksim.gymnasium.databinding.LayoutLoginBinding

class LoginActivity : ParentActivity() {

    private lateinit var ui: LayoutLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        CacheManager.init(this)

        // Устанавливаем сохраненную тему
        setActivityTheme()
        super.onCreate(savedInstanceState)

        ui = LayoutLoginBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // Настройка системных полей сверху и снизу
        setupSystemBars(ui.contentContainer)

        val context: Context = this
        ui.btnLogin.setOnClickListener {
            lifecycleScope.launch {
                try {
                    Login.login(context)
                    finish()
                } catch (e: Exception) {
                    Log.e("api-error", null, e)
                    Toast.makeText(
                        context,
                        "Произошла ошибка при попытке открыть авторизоваться",
                        Toast.LENGTH_SHORT
                    )
                }
            }
        }
    }
}
