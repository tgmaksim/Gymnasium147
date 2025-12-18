package ru.tgmaksim.gymnasium.pages.schedule

import java.util.Locale
import android.os.Bundle
import android.view.View
import java.time.LocalDate
import java.time.LocalTime
import android.content.Intent
import android.view.ViewGroup
import java.time.ZonedDateTime
import kotlinx.coroutines.launch
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import java.time.format.DateTimeFormatter
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.CancellationException
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.LinearLayoutManager

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.api.Dnevnik
import ru.tgmaksim.gymnasium.api.Request
import ru.tgmaksim.gymnasium.api.ScheduleDay
import ru.tgmaksim.gymnasium.ui.LoginActivity
import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.utilities.CacheManager
import ru.tgmaksim.gymnasium.utilities.NotificationManager
import ru.tgmaksim.gymnasium.databinding.SchedulePageBinding
import ru.tgmaksim.gymnasium.databinding.ScheduleCalendarDayBinding

/**
 * Страница с расписанием уроков пользователя
 * @author Максим Дрючин (tgmaksim)
 * @see ru.tgmaksim.gymnasium.ui.MainActivity
 * */
class SchedulePage : Fragment() {
    private lateinit var ui: SchedulePageBinding
    /** Текущий выбранный день в виде [ScheduleCalendarDayBinding.root] */
    private lateinit var lastSelected: LinearLayout

    companion object {
        private var schedule: List<ScheduleDay>? = null
        private val dateFormat = DateTimeFormatter.ofPattern(ScheduleDay.DATE_FORMAT)
        private const val SCHEDULE_LENGTH = 15
        private var updateToken: String? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // При смене страниц ui сохраняется
        if (::ui.isInitialized)
            return ui.root

        ui = SchedulePageBinding.inflate(inflater, container, false)

        // Синхронизация только при первой отрисовке или после входа по ссылке
        val intentData = requireActivity().intent.data
        if (schedule == null || intentData?.getQueryParameter("updateScheduleToken") != updateToken) {
            updateToken = intentData?.getQueryParameter("updateScheduleToken")

            lifecycleScope.launch {
                ui.swipeRefresh.isRefreshing = true
                loadCloudSchedule()  // Получение актуальных данных
                ui.swipeRefresh.isRefreshing = false
            }
        }

        showScheduleCalendar()  // Отображение даты на 2 недели (15 дней)
        showCacheSchedule()  // Показ расписания из кеша
        createRemindEA()  // Создание напоминания на следующее внеурочное занятие

        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utilities.log("SchedulePage загружена")
    }

    /**
     * Инициализация адаптера расписания после ее загрузки
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun initDayPagerAdapter() {
        val layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        ui.dayPager.layoutManager = layoutManager

        // Создание адаптера с возможностью перелистывания
        ui.dayPager.adapter = DayPagerAdapter(requireActivity(), schedule!!)
        val snapHelper = PagerSnapHelper().apply {
            attachToRecyclerView(ui.dayPager)
        }

        // Выбор активного дня: до 15:00 - текущий, после - следующий
        if (LocalTime.now().hour > 15)
            ui.dayPager.scrollToPosition(1)  // Прокрутка без анимации

        // Инициализация обработчика для смены активного дня в календаре
        ui.dayPager.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // Прокрутка завершена, а не находится в промежуточном состоянии
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val snappedView = snapHelper.findSnapView(layoutManager)
                    val position = snappedView?.let {
                        layoutManager.getPosition(it)
                    } ?: return

                    selectItemCalendar(ui.calendar.getChildAt(position) as LinearLayout)
                }
            }
        })

        // Установка цвета в соответствии с темой
        ui.swipeRefresh.setColorSchemeResources(R.color.bg_gradient_center)
        ui.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.bg_gradient_start)

        // Обновление жестом доступно только в самом верху
        ui.swipeRefresh.setOnChildScrollUpCallback { _, _ ->
            (ui.dayPager.adapter as DayPagerAdapter).lessons?.canScrollVertically(-1) != false
        }

        // Обновление жестом
        ui.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                loadCloudSchedule()
                ui.swipeRefresh.isRefreshing = false
            }
        }
    }

    /**
     * Получение из кеша и показ сохраненного расписания на нужный день
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun showCacheSchedule() {
        schedule = Dnevnik.getCacheSchedule()

        initDayPagerAdapter()  // Инициализация адаптера и показ расписания
    }

    /**
     * Обработка нажатия на системную кнопку назад (или жестом) для перелистывания дней расписания
     * @return true, если действие выполнено, иначе false
     * @author Максим Дрючин (tgmaksim)
     * */
    fun onBackPressed(): Boolean {
        val defaultDate = getDefaultDate()
        if ((lastSelected.tag as LocalDate) != defaultDate) {
            openDay(ui.calendar.findViewWithTag(defaultDate))
            return true
        }

        return false
    }

    /**
     * Создание напоминания о внеурочном занятии в виде уведомления за несколько минут до начала
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun createRemindEA() {
        // Функция выключена в настройках
        if (!CacheManager.EANotifications)
            return

        // Следующее по времени внеурочное занятие (сегодня или в другой день)
        val scheduleDay = schedule?.find {
            val date = LocalDate.parse(it.date, dateFormat)
            val startTimeEA = it.hoursEA?.let { h -> LocalTime.parse(h.start) } ?: return@find false

            it.ea.any() && (date > LocalDate.now() || (date == LocalDate.now() && startTimeEA > LocalTime.now()))
        } ?: return  // Внеурочных занятий не найдено

        val startTime = LocalTime.parse(scheduleDay.hoursEA!!.start)
        val timestamp = LocalDate.parse(scheduleDay.date, dateFormat)
            .atStartOfDay()
            .plusHours(startTime.hour.toLong())
            .plusMinutes(startTime.minute.toLong())
            .minusMinutes(15)  // За 15 минут до начала
            .toInstant(ZonedDateTime.now().offset)
            .toEpochMilli()

        NotificationManager.addAlarmNotification(
            requireContext(),
            NotificationManager.CHANNEL_EA,
            "Внеурочное занятие",
            "Напоминаю! Через 15 минут начнется ${scheduleDay.ea.joinToString { it.subject }}",
            NotificationCompat.PRIORITY_HIGH,
            timestamp,
            NotificationManager.ALARM_REQUEST_CODE_EA
        )
    }

    /**
     * Загрузка актуального расписания API-запросом на сервер. Инициализация [schedule]
     * @author Максим Дрючин (tgmaksim)
     * */
    private suspend fun loadCloudSchedule() {
        val cacheSchedule = schedule

        try {
            val response = Dnevnik.getSchedule()

            // Если сессия не авторизована, то открывается Login
            // Если произошла ошибка, выводится ошибка
            if (!response.status || response.answer == null) {
                response.error?.let { Utilities.log(it.type) }

                if (response.error?.type == "UnauthorizedError") {
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                    // Без закрытия MainActivity по нажатию системной кнопки назад (или жестом)
                    // можно открыть локальное расписания
//                    mainActivity.finish()
                } else if (response.error?.errorMessage != null) {
                    Utilities.showText(requireContext(), response.error.errorMessage)
                } else if (response.error?.type in listOf("ValidationError", "ApiMethodNotFoundError")) {
                    Utilities.showText(requireContext(), R.string.error_incorrect_data)
                } else {
                    Utilities.showText(requireContext(), R.string.error_api)
                }
            } else {
                schedule = response.answer.schedule  // Сохранение расписания
            }
        } catch (_: CancellationException) {

        } catch (e: Exception) {
            Utilities.log(e)
            if (!Request.checkInternet())
                Utilities.showText(requireContext(), R.string.error_internet)
            else
                Utilities.showText(requireContext(), R.string.error_load_schedule)
            return
        }

        Utilities.log("Успешная загрузка расписания")

        // Есть изменения
        if (cacheSchedule != schedule)
            schedule?.let {
                (ui.dayPager.adapter as DayPagerAdapter).updateSchedule(it)
                createRemindEA()
            }
    }

    /**
     * Показ горизонтального мини-календаря с прокруткой для просмотра расписания
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun showScheduleCalendar() {
        val today = LocalDate.now()
        val time = LocalTime.now()

        repeat(SCHEDULE_LENGTH) { i ->  // Заполнение дней на 2 недели (15 дней)
            val item = ScheduleCalendarDayBinding.inflate(layoutInflater, ui.calendar, false)
            item.root.setBackgroundResource(R.drawable.bg_button_day_selected)

            // Число и день недели
            val date = today.plusDays(i.toLong())
            item.dayNumber.text = date.dayOfMonth.toString()
            item.root.tag = date // Идентификация по дате

            @Suppress("DEPRECATION")  // У Local нет России
            val dayOfWeek = date.format(DateTimeFormatter.ofPattern("EE", Locale("ru")))
            item.weekday.text = dayOfWeek.uppercase()

            // Выбор активного дня: до 15:00 - текущий, после - следующий
            if (i == 0 && time.hour < 15 || i == 1 && time.hour >= 15) {
                lastSelected = item.root
                item.root.isSelected = true
            }

            // Определение действия при нажатии
            item.root.setOnClickListener {
                openDay(item.root)
            }

            ui.calendar.addView(item.root)
        }
    }

    /**
     * Открытие определенного дня расписания по нажатии на кнопку, без анимации перехода
     * @param item Объект дня в мини-календаре
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun openDay(item: LinearLayout) {
        selectItemCalendar(item)

        val index = ui.calendar.indexOfChild(item)
        ui.dayPager.scrollToPosition(index)  // Без анимации
    }

    /**
     * Выбор активного дня в мини-календаре
     * @param item Объект дня в мини-календаре
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun selectItemCalendar(item: LinearLayout) {
        // Обновление выделения
        lastSelected.isSelected = false
        item.isSelected = true
        lastSelected = item

        // Центрирование кнопки текущего дня
        ui.calendarScroll.post {
            val scrollTo = item.left - (ui.calendarScroll.width - item.width) / 2
            ui.calendarScroll.smoothScrollTo(scrollTo, 0)
        }
    }

    /**
     * День расписания по умолчанию (сегодня или завтра после 15:00)
     * @return день в виде [LocalDate]
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun getDefaultDate(): LocalDate =
        if (LocalTime.now().hour >= 15) {
            LocalDate.now().plusDays(1)
        } else {
            LocalDate.now()
        }
}