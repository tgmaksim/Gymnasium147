package ru.tgmaksim.gymnasium

import android.util.Log
import android.os.Bundle
import java.lang.Exception
import android.widget.Toast
import android.content.Intent
import android.content.Context
import kotlinx.coroutines.launch
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AppCompatDelegate

import ru.tgmaksim.gymnasium.api.Login
import ru.tgmaksim.gymnasium.api.SessionStatus
import ru.tgmaksim.gymnasium.utilities.CacheManager
import ru.tgmaksim.gymnasium.fragment.ActionsFragment
import ru.tgmaksim.gymnasium.fragment.ProfileFragment
import ru.tgmaksim.gymnasium.fragment.ScheduleFragment
import ru.tgmaksim.gymnasium.fragment.SettingsFragment
import ru.tgmaksim.gymnasium.databinding.ActivityMainBinding

class MainActivity : ParentActivity() {

    private lateinit var ui: ActivityMainBinding
    private var currentTab = R.id.it_schedule
    private val fragments = mutableMapOf<Int, Fragment>()

    companion object {
        var skipAnimation = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        CacheManager.init(this)

        // Устанавливаем сохраненную тему
        setActivityTheme()
        super.onCreate(savedInstanceState)

        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // Настройка системных полей сверху и снизу
        setupSystemBars(ui.contentContainer)

        // Проверяем сессию и, если надо, перенаправляем на авторизацию
        // Только после проверки сессии продолжается отрисовка
        val context: Context = this
        lifecycleScope.launch {
            var processed = true
            try {
                val sessionStatus: SessionStatus = Login.checkSession()

                if (!(sessionStatus.auth && sessionStatus.exists)) {
                    processed = false
                    CacheManager.apiSession = null
                    val intent = Intent(context, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                Log.e("api-error", null, e)
                Toast.makeText(
                    context,
                    "Произошла ошибка при загрузке приложения",
                    Toast.LENGTH_SHORT
                ).show()
            }

            if (processed) {
                // Смена страницы меню на ранее открытую
                currentTab = CacheManager.openedActivity
                replaceFragment(newMenuPage(currentTab), animation = false)
                ui.bottomMenu.selectedItemId = currentTab

                // Инициализация обработчиков нажатий кнопок меню и смены темы
                setupMenuListener()
                setupBtnThemeListener()
            }
        }
    }

    private fun setupMenuListener() {
        ui.bottomMenu.setOnItemSelectedListener { item ->
            val newFragment = newMenuPage(item.itemId)

            if (skipAnimation) {
                skipAnimation = false
                replaceFragment(newFragment, animation = false)
                currentTab = item.itemId
                return@setOnItemSelectedListener true
            }

            val oldIndex = menuIndex(currentTab)
            val newIndex = menuIndex(item.itemId)

            if (newIndex > oldIndex) {
                replaceFragment(newFragment, toRight = true)
            } else {
                replaceFragment(newFragment, toRight = false)
            }

            currentTab = item.itemId
            CacheManager.openedActivity = item.itemId
            true
        }
    }

    private fun setupBtnThemeListener() {
        ui.btnTheme.setOnClickListener {
            val isDark = (resources.configuration.uiMode
                    and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES

            if (isDark) {
                CacheManager.isDarkTheme = false
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                CacheManager.isDarkTheme = true
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, toRight: Boolean = true, animation: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
        if (animation) {
            transaction.setCustomAnimations(
                if (toRight) R.anim.slide_in_right else R.anim.slide_in_left,
                if (toRight) R.anim.slide_out_left else R.anim.slide_out_right
            )
        }
        transaction.replace(R.id.content_container, fragment).commit()
    }

    private fun menuIndex(id: Int): Int =
        when(id) {
            R.id.it_schedule -> 0
            R.id.it_actions -> 1
            R.id.it_profile -> 2
            R.id.it_settings -> 3
            else -> 0
        }

    private fun newMenuPage(itemId: Int): Fragment =
        fragments.getOrPut(itemId) {
            when(itemId) {
                R.id.it_schedule -> ScheduleFragment()
                R.id.it_actions -> ActionsFragment()
                R.id.it_profile -> ProfileFragment()
                R.id.it_settings -> SettingsFragment()
                else -> ScheduleFragment()
            }
        }
}
