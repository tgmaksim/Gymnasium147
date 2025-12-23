package ru.tgmaksim.gymnasium.ui

import android.os.Bundle
import android.graphics.Rect
import android.graphics.Color
import android.widget.TextView
import android.view.MotionEvent
import kotlinx.coroutines.launch
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.api.Status
import ru.tgmaksim.gymnasium.BuildConfig
import ru.tgmaksim.gymnasium.pages.MarksPage
import ru.tgmaksim.gymnasium.pages.SchoolPage
import ru.tgmaksim.gymnasium.pages.SettingsPage
import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.utilities.CacheManager
import ru.tgmaksim.gymnasium.pages.schedule.SchedulePage
import ru.tgmaksim.gymnasium.databinding.ActivityMainBinding

/**
 * Главная Activity приложения
 * @author Максим Дрючин (tgmaksim)
 * */
class MainActivity : ParentActivity() {
    private lateinit var ui: ActivityMainBinding
    private var currentTab = R.id.it_schedule

    companion object {
        private var skipAnimation = false
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

        // После перерисовки текущий fragment сам отрисуется
        if (savedInstanceState == null) {
            pages.clear()
            replaceFragment(newMenuPage(currentTab), animation = false)
        }

        // Текущий фрагмент расписания скрыт или не существует
        else if ((pages[R.id.it_schedule]?.id ?: 0) == 0) {
            val scheduleFragment = supportFragmentManager.fragments.find { it is SchedulePage }

            if (scheduleFragment != null)
                pages[R.id.it_schedule] = scheduleFragment
        }

        setupMenuListener()  // Настройка нажатий на пункты меню
        setupBackListener()  // Настройка нажатий на системную кнопку назад (или жестом)

        // Проверка текущей версии приложения
        lifecycleScope.launch {
            checkVersion()
        }

        Utilities.log("MainActivity запущен", tag="load") {
            param("place", "MainActivity")
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN)
            return super.dispatchTouchEvent(event)

        // При нажатии на любую область вне TextView с возможным выделением текста,
        // фокус сбрасывается
        val el = currentFocus
        if (el is TextView) {
            val outRect = Rect()
            el.getGlobalVisibleRect(outRect)

            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt()))
                el.clearFocus()
        }

        return super.dispatchTouchEvent(event)
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
                    Utilities.log("API error(${error.type}) at checkVersion: ${error.errorMessage}}")
                    error.errorMessage?.let {
                        Utilities.showText(this, it)
                    }
                }

                return
            }

            if (response.answer.latestVersionNumber <= BuildConfig.VERSION_CODE)
                return

            Utilities.log("Обнаружена новая версия: ${response.answer.latestVersionString} " +
                    "(${response.answer.latestVersionNumber})", tag="version") {
                param("now", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                param("found", "${response.answer.latestVersionString} (${response.answer.latestVersionNumber})")
            }

            CacheManager.versionStatus = response.answer

            // Показ красной точки возле иконки настроек
            ui.bottomMenu.getOrCreateBadge(R.id.it_settings).apply {
                isVisible = true
                backgroundColor = Color.RED
                clearNumber()
            }

            if (response.answer.versionStatus == "Требуется обновление") {
                Utilities.showAlertDialog(
                    this,
                    response.answer.versionStatus,
                    "Вышло обновление, которое требуется установить для корректной работы приложения",
                    "Настройки",
                    true
                ) { _, _ ->
                    ui.bottomMenu.selectedItemId = R.id.it_settings
                }
            } else if (response.answer.versionStatus == "Новая функция") {
                Utilities.showAlertDialog(
                    this,
                    response.answer.versionStatus,
                    "В обновлении приложения появилась новая функция. Уже можно пользоваться",
                    "Настройки",
                    true
                ) { _, _ ->
                    ui.bottomMenu.selectedItemId = R.id.it_settings
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
     * Настройка нажатий на системную кнопку назад (или жестом)
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun setupBackListener() {
        onBackPressedDispatcher.addCallback(this) {
            // На странице расписания свой обработчик,
            // но если действие не выполнено, окно сворачивается
            if (ui.bottomMenu.selectedItemId == R.id.it_schedule) {
                if (!(newMenuPage(R.id.it_schedule) as SchedulePage).onBackPressed())
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
                R.id.it_schedule -> SchedulePage()
                R.id.it_marks -> MarksPage()
                R.id.it_school -> SchoolPage()
                R.id.it_settings -> SettingsPage()
                else -> SchedulePage()
            }
        }
}