package ru.tgmaksim.gymnasium.ui

import android.os.Bundle
import android.view.View
import android.graphics.Color
import kotlinx.coroutines.launch
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import android.content.res.Configuration
import androidx.lifecycle.lifecycleScope
import ru.tgmaksim.gymnasium.BuildConfig
import androidx.appcompat.app.AppCompatDelegate

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.api.Constants
import ru.tgmaksim.gymnasium.api.VersionChecker
import ru.tgmaksim.gymnasium.utilities.Utilities
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

        // Инициализация обработчиков нажатий кнопок меню, смены темы и ухода назад
        setupMenuListener()
        setupBtnThemeListener()
        setupBackListener()

        // Проверка текущей версии приложения и при необходимости скачивание новой
        lifecycleScope.launch {
            checkVersion()
        }
    }

    /** Проверка текущей версии приложения и при необходимости скачивание новой */
    private suspend fun checkVersion() {
        val versionStatus = VersionChecker.checkVersion(BuildConfig.VERSION_CODE)

        CacheManager.versionStatus = versionStatus

        if (versionStatus.apiVersionDeprecated) {
            Utilities.showAlertDialog(
                this,
                "Срочно требуется обновление",
                "Похоже, данная версия приложения больше не обслуживается сервером. " +
                        "Посетите официальный сайт для обновления",
                "Обновить"
            ) { _, _ ->
                Utilities.openUrl(this, Constants.DOMAIN)
            }
        } else if (versionStatus.newApiVersion) {
            Utilities.showAlertDialog(
                this,
                "Требуется обновление",
                "Версия приложения устарела, поэтому некоторые функции могут не работать. " +
                        "Посетите настройки для обновления",
                "Обновить"
            ) { _, _ ->
                ui.bottomMenu.selectedItemId = R.id.it_settings
            }
        }

        if (versionStatus.newLatestVersion) {
            // Показ красной точки возле иконки настроек
            ui.bottomMenu.getOrCreateBadge(R.id.it_settings).apply {
                isVisible = true
                backgroundColor = Color.RED
                clearNumber()
            }

            val settings = newMenuPage(R.id.it_settings) as SettingsFragment
            settings.startLoading()

            // Загрузка новой версии
            CacheManager.loadedUpdate = VersionChecker.loadUpdate(
                this,settings::progressLoadUpdate
            ).let {
                if (!it)
                    Utilities.showText(this, "Не удалось скачать обновление...")
                else
                    Utilities.showText(this, "Обновление скачано, требуется установка")
                it
            }

            settings.finishLoading()
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
            true
        }
    }

    /** Настройка нажатия на кнопку смены темы */
    private fun setupBtnThemeListener() {
        ui.buttonTheme.setOnClickListener {
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

    /** Настройка нажатий на системную кнопку назад (или жестом) */
    private fun setupBackListener() {
        onBackPressedDispatcher.addCallback(this) {
            // На страницах кроме расписания происходит возврат на страницу расписания
            // В расписании свой обработчик
            if (ui.bottomMenu.selectedItemId != R.id.it_schedule)
                ui.bottomMenu.selectedItemId = R.id.it_schedule
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