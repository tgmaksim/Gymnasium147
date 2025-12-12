package ru.tgmaksim.gymnasium

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

import ru.tgmaksim.gymnasium.ui.MainActivity
import ru.tgmaksim.gymnasium.ui.LoginActivity
import ru.tgmaksim.gymnasium.utilities.CacheManager

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Маршрутизация в зависимости от наличия сессии
        if (CacheManager.apiSession == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java).setData(intent.data))
        }

        finish()
    }
}