package ru.tgmaksim.gymnasium.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

import ru.tgmaksim.gymnasium.R
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
        // Устанавливается сохраненная тема
        setActivityTheme()
        super.onCreate(savedInstanceState)

        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // После перерисовки текущий fragment сам отрисуется
        if (savedInstanceState == null)
            replaceFragment(newMenuPage(currentTab), animation = false)

        // Настройка системных полей сверху и снизу
        setupSystemBars(ui.contentContainer)

        // Инициализация обработчиков нажатий кнопок меню и смены темы
        setupMenuListener()
        setupBtnThemeListener()
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