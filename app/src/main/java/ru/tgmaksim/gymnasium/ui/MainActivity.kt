package ru.tgmaksim.gymnasium.ui

import android.util.Log
import android.view.View
import android.os.Bundle
import java.lang.Exception
import android.widget.Toast
import android.content.Intent
import android.content.Context
import kotlinx.coroutines.launch
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.api.Login
import ru.tgmaksim.gymnasium.api.SessionStatus
import ru.tgmaksim.gymnasium.utilities.CacheManager
import ru.tgmaksim.gymnasium.fragment.MarksFragment
import ru.tgmaksim.gymnasium.fragment.SchoolFragment
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

        // Устанавливается сохраненная тема
        setActivityTheme()
        super.onCreate(savedInstanceState)

        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // Настройка системных полей сверху и снизу
        setupSystemBars(ui.contentContainer)

        // Показ анимации загрузки
        showLoading()

        // Проверяется сессия и, если надо, перенаправляется на авторизацию
        // Только после проверки сессии продолжается отрисовка
        val context: Context = this
        lifecycleScope.launch {
            var processed = true
            try {
                val sessionStatus: SessionStatus = Login.checkSession()

                if (!(sessionStatus.auth && sessionStatus.exists)) {
                    processed = false
                    val intent = Intent(context, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                Log.e("api-error", null, e)
                Toast.makeText(
                    context,
                    R.string.error_start,
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Завершается анимация загрузки
            hideLoading()

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

    /** Настройка нажатий на кнопки меню */
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

            if (newIndex > oldIndex)
                replaceFragment(newFragment, toRight = true)
            else
                replaceFragment(newFragment, toRight = false)

            // Сохраняется открытая страница
            currentTab = item.itemId
            CacheManager.openedActivity = item.itemId
            true
        }
    }

    /** Настройка нажатия на кнопку смены темы */
    private fun setupBtnThemeListener() {
        ui.btnTheme.setOnClickListener {
            val isDark = (resources.configuration.uiMode
                    and Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES

            // Смена темы и сохранение в кеше
            if (isDark) {
                CacheManager.isDarkTheme = false
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                CacheManager.isDarkTheme = true
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    /** Смена страницы с анимацией перехода */
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

    /** Получение идентификатора страницы */
    private fun menuIndex(id: Int): Int =
        when(id) {
            R.id.it_schedule -> 0
            R.id.it_marks -> 1
            R.id.it_school -> 2
            R.id.it_settings -> 3
            else -> 0  // По умолчанию страница с расписанием
        }

    /** Открытие страницы или создание новой */
    private fun newMenuPage(itemId: Int): Fragment =
        fragments.getOrPut(itemId) {
            when(itemId) {
                R.id.it_schedule -> ScheduleFragment()
                R.id.it_marks -> MarksFragment()
                R.id.it_school -> SchoolFragment()
                R.id.it_settings -> SettingsFragment()
                else -> ScheduleFragment()
            }
        }

    /** Показ анимации загрузки */
    fun showLoading() {
        ui.loadingOverlay.visibility = View.VISIBLE
    }

    /** Скрытие анимации загрузки */
    fun hideLoading() {
        ui.loadingOverlay.visibility = View.GONE
    }
}