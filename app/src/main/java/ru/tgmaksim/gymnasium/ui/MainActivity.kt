package ru.tgmaksim.gymnasium.ui

import android.os.Bundle
import android.view.View
import android.graphics.Color
import kotlinx.coroutines.launch
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.BuildConfig
import ru.tgmaksim.gymnasium.api.Status
import ru.tgmaksim.gymnasium.fragment.MarksPage
import ru.tgmaksim.gymnasium.fragment.SchoolPage
import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.fragment.SettingsPage
import ru.tgmaksim.gymnasium.utilities.CacheManager
import ru.tgmaksim.gymnasium.fragment.ScheduleFragment
import ru.tgmaksim.gymnasium.utilities.NotificationManager
import ru.tgmaksim.gymnasium.databinding.ActivityMainBinding

/**
 * Главная Activity приложения
 * @author Максим Дрючин (tgmaksim)
 * */
class MainActivity : ParentActivity() {
    private lateinit var ui: ActivityMainBinding
    /** Текущая открытая страница */
    private var currentTab = R.id.it_schedule

    companion object {
        var skipAnimation = false
        /** Фрагменты страниц */
        private val pages = mutableMapOf<Int, Fragment>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Устанавливается сохраненная тема
        setupActivityTheme()
        super.onCreate(savedInstanceState)

        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // Настройка системных полей сверху и снизу
        setupSystemBars(ui.contentContainer)

        // Запрос разрешений на уведомления и напоминаний
        NotificationManager.setupPostNotifications(this)

        // После перерисовки текущий fragment сам отрисуется
        if (savedInstanceState == null) {
            pages.clear()
            replaceFragment(newMenuPage(currentTab), animation = false)
        }

        // Текущий фрагмент расписания скрыт
        else if ((pages[R.id.it_schedule]?.id ?: 0) == 0) {
            val scheduleFragment = supportFragmentManager.fragments.find { it is ScheduleFragment }
            if (scheduleFragment == null)
                replaceFragment(newMenuPage(R.id.it_schedule), animation = false)
            else
                pages[R.id.it_schedule] = scheduleFragment
        }

        setupMenuListener()  // Настройка нажатий на пункты меню
        setupButtonThemeListener()  // Настройка кнопки смены темы
        setupBackListener()  // Настройка нажатий на системную кнопку назад (или жестом)

        // Проверка текущей версии приложения
        lifecycleScope.launch {
            checkVersion()
        }
    }

    /**
     * Проверка текущей версии приложения и при необходимости уведомление пользователя
     * @author Максим Дрючин (tgmaksim)
     * */
    private suspend fun checkVersion() {
        try {
            val response = Status.checkVersion()

            if (!response.status || response.answer == null) {
                response.error?.let { error ->
                    Utilities.log(error.type)
                    error.errorMessage?.let { errorMessage ->
                        Utilities.showText(this, errorMessage)
                    }
                }

                return
            }

            if (response.answer.latestVersionNumber > BuildConfig.VERSION_CODE) {
                CacheManager.versionStatus = response.answer

                // Показ красной точки возле иконки настроек
                ui.bottomMenu.getOrCreateBadge(R.id.it_settings).apply {
                    isVisible = true
                    backgroundColor = Color.RED
                    clearNumber()
                }
            }
        } catch (e: Exception) {
            Utilities.log(e)
        }
    }

    /**
     * Настройка нажатий на кнопки меню
     * @author Максим Дрючин (tgmaksim)
     * */
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

    /**
     * Настройка нажатия на кнопку смены темы
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun setupButtonThemeListener() {
        ui.buttonTheme.setOnClickListener {
            CacheManager.isDarkTheme = CacheManager.isDarkTheme.not()
            setupActivityTheme()
        }
    }

    /**
     * Настройка нажатий на системную кнопку назад (или жестом)
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun setupBackListener() {
        onBackPressedDispatcher.addCallback(this) {
            // На странице расписания свой обработчик,
            // но если действие не выполнено, окно сворачивается
            if (ui.bottomMenu.selectedItemId == R.id.it_schedule) {
                if (!(newMenuPage(R.id.it_schedule) as ScheduleFragment).onBackPressed())
                    moveTaskToBack(true)
            }

            // Если открытая страница - не расписание, переход на нее
            else {
                ui.bottomMenu.selectedItemId = R.id.it_schedule
            }
        }
    }

    /**
     * Смена страницы с анимацией перехода
     * @param fragment новая страница
     * @param toRight показывать ли анимацию перехода вправо, иначе влево
     * @param animation показывать ли анимацию
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun replaceFragment(
        fragment: Fragment,
        toRight: Boolean = true,
        animation: Boolean = true
    ) {
        supportFragmentManager.beginTransaction().apply {
            if (animation)
                setCustomAnimations(
                    if (toRight) R.anim.slide_in_right else R.anim.slide_in_left,
                    if (toRight) R.anim.slide_out_left else R.anim.slide_out_right
                )
        }.replace(R.id.content_container, fragment).commit()
    }

    /**
     * Получение идентификатора страницы
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun menuIndex(id: Int): Int =
        when(id) {
            R.id.it_schedule -> 0
            R.id.it_marks -> 1
            R.id.it_school -> 2
            R.id.it_settings -> 3
            else -> 0  // По умолчанию страница с расписанием
        }

    /**
     * Открытие страницы или создание нового экземпляра
     * @param itemId идентификатор страницы в виде идентификатора ресурса
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun newMenuPage(itemId: Int): Fragment =
        pages.getOrPut(itemId) {
            when(itemId) {
                R.id.it_schedule -> ScheduleFragment()
                R.id.it_marks -> MarksPage()
                R.id.it_school -> SchoolPage()
                R.id.it_settings -> SettingsPage()
                else -> ScheduleFragment()
            }
        }

    /**
     * Показ анимации загрузки в левом верхнем углу
     * @author Максим Дрючин (tgmaksim)
     * */
    fun showLoading() {
        ui.loadingOverlay.visibility = View.VISIBLE
    }

    /**
     * Скрытие анимации загрузки в левом верхнем углу
     * @author Максим Дрючин (tgmaksim)
     * */
    fun hideLoading() {
        ui.loadingOverlay.visibility = View.GONE
    }
}