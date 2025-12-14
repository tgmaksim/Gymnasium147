package ru.tgmaksim.gymnasium.fragment

import kotlin.math.abs
import java.util.Locale
import android.os.Bundle
import android.view.View
import java.time.LocalDate
import java.time.LocalTime
import android.text.Spanned
import android.view.ViewGroup
import android.graphics.Color
import android.text.TextPaint
import android.content.Intent
import java.time.ZonedDateTime
import android.widget.TextView
import android.view.MotionEvent
import kotlinx.coroutines.launch
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.view.GestureDetector
import android.text.SpannableString
import androidx.fragment.app.Fragment
import android.annotation.SuppressLint
import android.view.animation.Animation
import android.text.style.ClickableSpan
import androidx.lifecycle.lifecycleScope
import java.time.format.DateTimeFormatter
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import android.view.animation.AnimationUtils
import android.text.method.LinkMovementMethod
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.api.Hours
import ru.tgmaksim.gymnasium.api.Lesson
import ru.tgmaksim.gymnasium.api.Request
import ru.tgmaksim.gymnasium.api.Dnevnik
import ru.tgmaksim.gymnasium.api.ScheduleDay
import ru.tgmaksim.gymnasium.ui.MainActivity
import ru.tgmaksim.gymnasium.ui.LoginActivity
import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.utilities.CacheManager
import ru.tgmaksim.gymnasium.databinding.ItemDayBinding
import ru.tgmaksim.gymnasium.api.ExtracurricularActivity
import ru.tgmaksim.gymnasium.utilities.NotificationManager
import ru.tgmaksim.gymnasium.databinding.FragmentScheduleBinding

/**
 * Адаптер для списка уроков в расписании определенного дня
 * @param lessons уроки в виде списка уроков [Lesson]
 * @param hoursEA время внеурочек данного дня (если есть) в виде [Hours]
 * @param ea внеурочки данного дня (если есть) в виде списка внеурочек [ExtracurricularActivity]
 * @param mainActivity объект [MainActivity], в котором находится фрагмент
 * @author Максим Дрючин (tgmaksim)
 * @see ScheduleFragment
 * */
class LessonsAdapter(private var lessons: List<Lesson>,
                     private var hoursEA: Hours?,
                     private var ea: List<ExtracurricularActivity>,
                     private var mainActivity: MainActivity) :
    RecyclerView.Adapter<LessonsAdapter.LessonViewHolder>() {

    /**
     * ViewHolder для элемента списка уроков
     * @param view элемент списка уроков
     * @property time [TextView] с временем урока
     * @property subject [TextView] с названием предмета
     * @property place [TextView] с кабинетом или другим местом проведения
     * @property homework [TextView] с домашним заданием (если есть)
     * @property homeworkGroup объект [LinearLayout] с элементами домашнего задания
     * @property filesContainer список файлов домашнего задания (если есть) в виде [LinearLayout]
     * @author Максим Дрючин (tgmaksim)
     * */
    class LessonViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        val time: TextView = view.findViewById(R.id.time)
        val subject: TextView = view.findViewById(R.id.subject)
        val place: TextView = view.findViewById(R.id.place)
        val homework: TextView = view.findViewById(R.id.homework)
        val homeworkGroup: LinearLayout = view.findViewById(R.id.homeworkGroup)
        val filesContainer: LinearLayout = view.findViewById(R.id.filesContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder =
        LessonViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lesson, parent, false))

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        // Для уроков объекты уже загружены
        val lesson = if (position < lessons.size) {
            // Возвращение цвета
            holder.view.background = ContextCompat.getDrawable(
                holder.view.context, R.drawable.bg_lesson)

            lessons[position]
        } else {
            // Выделение фона другим цветом
            holder.view.background = ContextCompat.getDrawable(
                holder.view.context, R.drawable.bg_lesson_extra)

            // Для всех внеурочек создается один объект
            val subjects = ea.joinToString("\n") { it.subject }
            val place = ea.joinToString("; ") { it.place }

            Lesson(position, subjects, place, hoursEA ?: Hours("00:00", "00:00"))
        }

        // Заполняется информация в элементе
        holder.time.text = lesson.hours.format()
        holder.subject.text = lesson.subject
        holder.place.text = lesson.place

        // Показывается или скрывается домашнее задание
        if (lesson.homework?.isEmpty() == false) {
            holder.homework.text = lesson.homework
            holder.homeworkGroup.visibility = View.VISIBLE
        }
        else {
            holder.homework.text = R.string.homework_not_found.toString()
            holder.homeworkGroup.visibility = View.GONE
        }

        // Строка с гиперссылкой на ресурс с файлами к домашнему заданию (если есть)
        val view = LayoutInflater.from(holder.view.context).inflate(
            R.layout.item_homework_file,
            holder.filesContainer,
            false
        ) as LinearLayout
        holder.filesContainer.removeAllViews()

        // Добавление прикрепленных файлов к уроку
        for (file in lesson.files) {
            val fileNameView: TextView = view.findViewById(R.id.homeworkDocumentName)
            val spannable = SpannableString(file.fileName)

            // Кликабельная ссылка
            spannable.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val url = file.downloadUrl

                    // Открытие либо WebView, либо браузера
                    if (CacheManager.openWebView) {
                        mainActivity.supportFragmentManager.beginTransaction().replace(
                            R.id.content_container,
                            WebViewFragment.newInstance(url)
                        ).addToBackStack(null).commit()
                    } else {
                        Utilities.openUrl(mainActivity, url)
                    }
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                }
            }, 0, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            fileNameView.text = spannable
            fileNameView.movementMethod = LinkMovementMethod.getInstance()
            fileNameView.highlightColor = Color.TRANSPARENT

            holder.filesContainer.addView(view)
        }
    }

    /**
     * Обновление расписания
     * @param newLessons новые уроки в виде списка уроков [Lesson]
     * @param newHoursEA время внеурочек данного дня (если есть) в виде [Hours]
     * @param newEA внеурочки данного дня
     * @author Максим Дрючин (tgmaksim)
     * */
    @SuppressLint("NotifyDataSetChanged")
    fun updateLessons(
        newLessons: List<Lesson>,
        newHoursEA: Hours?,
        newEA: List<ExtracurricularActivity>
    ) {
        lessons = newLessons
        hoursEA = newHoursEA
        ea = newEA
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int =
        lessons.size + if (ea.isEmpty()) 0 else 1  // Карточка внеурочки всегда одна, либо ее нет
}

/**
 * Fragment-страница с расписанием уроков пользователя
 * @author Максим Дрючин (tgmaksim)
 * @see ru.tgmaksim.gymnasium.ui.MainActivity
 * */
class ScheduleFragment : Fragment() {
    private lateinit var ui: FragmentScheduleBinding
    /** Текущий выбранный день в виде [ItemDayBinding.root] */
    private lateinit var lastSelected: LinearLayout
    /** Объект [MainActivity], в котором находится фрагмент */
    private lateinit var mainActivity: MainActivity
    /** Объект для обработки жестов */
    @Suppress("DEPRECATION") private lateinit var gestureDetector: GestureDetectorCompat

    companion object {
        private var schedule: List<ScheduleDay>? = null
        private val dateFormat = DateTimeFormatter.ofPattern(ScheduleDay.DATE_FORMAT)
        private const val SCHEDULE_LENGTH = 15
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initGestureDetectorCompat()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = FragmentScheduleBinding.inflate(inflater, container, false)
        mainActivity = requireActivity() as MainActivity

        // Синхронизация только при первой отрисовке или после входа по ссылке
        if (schedule == null || mainActivity.intent.data?.getQueryParameter("updateSchedule") == "true") {
            lifecycleScope.launch {
                mainActivity.showLoading()
                loadCloudSchedule()
                mainActivity.hideLoading()
            }
        }

        showScheduleCalendar()  // Отображение даты на две недели (15 дней)
        showCacheSchedule()  // Показ расписания из кеша

        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFlipping()  // Настройка перелистывания дней жестом
        createRemindEA()  // Создание напоминания на следующую внеурочку
    }

    /**
     * Инициализация обработчика жестов для перелистывания дней и обновления расписания
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun initGestureDetectorCompat() {
        @Suppress("DEPRECATION")
        gestureDetector = GestureDetectorCompat(requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                private val SWIPE_THRESHOLD = 80     // минимальная дистанция
                private val SWIPE_VELOCITY = 80      // минимальная скорость

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (e1 == null)
                        return false
                    val diffX = e2.x - e1.x  // Расстояние по x
                    val diffY = e2.y - e1.y  // Расстояние по y

                    // Перелистывание жестом вправо/влево
                    if (abs(diffX) >= SWIPE_THRESHOLD && abs(velocityX) >= SWIPE_VELOCITY) {
                        val index = ui.dayContainer.indexOfChild(lastSelected)

                        return if (diffX < 0) {
                            if (index + 1 < SCHEDULE_LENGTH)
                                openDay(ui.dayContainer.getChildAt(index + 1) as LinearLayout)
                            true
                        } else if (diffX > 0) {
                            if (index - 1 >= 0)
                                openDay(ui.dayContainer.getChildAt(index - 1) as LinearLayout)
                            true
                        } else {
                            false
                        }
                    }

                    // Обновление расписания жестом вниз
                    else if (diffY >= SWIPE_THRESHOLD && abs(velocityY) >= SWIPE_VELOCITY) {
                        lifecycleScope.launch {
                            mainActivity.showLoading()
                            loadCloudSchedule()
                            mainActivity.hideLoading()
                        }
                        return true
                    }

                    return false
                }
            })
    }

    /**
     * Настройка перелистывания дней жестом с помощью обработчика
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun setupFlipping() {
        // Перелистывание доступно на списке уроков и фото выходных
        @SuppressLint("ClickableViewAccessibility")
        ui.lessons.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP)
                v.performClick()
            false
        }
        @SuppressLint("ClickableViewAccessibility")
        ui.weekendPhoto.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP)
                v.performClick()
            true
        }
    }

    /**
     * Получение из кеша и показ сохраненного расписания на нужный день
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun showCacheSchedule() {
        schedule = Dnevnik.getCacheSchedule()

        // Показ расписания на сегодня или завтра (после 15:00)
        showScheduleDay(getDefaultDate().format(dateFormat))
    }

    /**
     * Обработка нажатия на системную кнопку назад (или жестом) для перелистывания дней расписания
     * @return true, если действие выполнено, иначе false
     * @author Максим Дрючин (tgmaksim)
     * */
    fun onBackPressed(): Boolean {
        val defaultDate = getDefaultDate()
        if ((lastSelected.tag as LocalDate) != defaultDate) {
            openDay(ui.dayContainer.findViewWithTag(defaultDate))
            return true
        }

        return false
    }

    /**
     * Создание напоминания о внеурочке в виде уведомления за несколько минут до начала
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun createRemindEA() {
        // Функция выключена в настройках
        if (!CacheManager.EANotifications)
            return

        // Следующая по времени внеурочка (сегодня или в другой день)
        val scheduleDay = schedule?.find {
            val date = LocalDate.parse(it.date, dateFormat)
            val startTimeEA = it.hoursEA?.let { h -> LocalTime.parse(h.start) } ?: return@find false

            it.ea.any() && (date > LocalDate.now() || (date == LocalDate.now() && startTimeEA > LocalTime.now()))
        } ?: return  // Внеурочек не найдено

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
            "Напоминаю! Через 15 минут начнется внеурочка (${scheduleDay.ea.joinToString { it.subject }})",
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
            val newSchedule = Dnevnik.getSchedule()

            // Если сессия не авторизована, то открывается Login
            // Если произошла ошибка, выводится ошибка
            if (!newSchedule.status) {
                if (newSchedule.unauthorized) {
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                    // Без закрытия MainActivity по нажатию системной кнопки назад (или жестом)
                    // можно открыть локальное расписания
//                    mainActivity.finish()
                } else {
                    Utilities.showText(requireContext(), R.string.error_api)
                }
            } else {
                schedule = newSchedule.schedule  // Сохранение расписания
            }
        } catch (e: Exception) {
            Utilities.log(e)
            if (!Request.checkInternet())
                Utilities.showText(requireContext(), R.string.error_internet)
            else
                Utilities.showText(requireContext(), R.string.error_load_schedule)
            return
        }

        // Есть изменения
        if (cacheSchedule != schedule) {
            openDay(lastSelected, anim = false)
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
            val item = ItemDayBinding.inflate(layoutInflater, ui.dayContainer, false)
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
                item.root.isSelected = true
                lastSelected = item.root
            }

            // Определение действия при нажатии
            item.root.setOnClickListener { openDay(item.root) }

            ui.dayContainer.addView(item.root)
        }
    }

    /**
     * Показ расписания на данный день
     * @param date дата в формате [ScheduleDay.DATE_FORMAT]
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun showScheduleDay(date: String) {
        // Первый раз инициализация адаптера
        if (ui.lessons.adapter == null) {
            ui.lessons.adapter = LessonsAdapter(
                emptyList(),
                null,
                emptyList(),
                mainActivity
            )
            ui.lessons.layoutManager = LinearLayoutManager(requireContext())
        }

        if (schedule == null)
            return

        // Поиск нужного дня в загруженном расписании
        val scheduleDay = searchScheduleDay(date)

        val lessons = scheduleDay?.lessons ?: emptyList()
        val hoursEA = scheduleDay?.hoursEA
        val ea = scheduleDay?.ea ?: emptyList()

        if (lessons.isEmpty() && scheduleDay != null) {
            // Обновление страницы с новыми данными
            (ui.lessons.adapter as LessonsAdapter).updateLessons(
                lessons,
                hoursEA,
                ea
            )

            ui.lessons.visibility = View.GONE
            ui.weekendPhoto.visibility = View.VISIBLE
        } else {
            ui.lessons.visibility = View.VISIBLE
            ui.weekendPhoto.visibility = View.GONE

            // Обновление страницы с новыми данными
            (ui.lessons.adapter as LessonsAdapter).updateLessons(
                lessons,
                hoursEA,
                ea
            )
        }
    }

    /**
     * Поиск дня в расписании с данной датой
     * @param date дата в формате [ScheduleDay.DATE_FORMAT]
     * @return найденный день в виде [ScheduleDay]
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun searchScheduleDay(date: String): ScheduleDay? = schedule?.find { it.date == date }

    /**
     * Открытие определенного дня расписания по нажатии на кнопку, показывая анимацию перехода
     * @param item объект дня в мини-календаре
     * @param anim показывать ли анимацию перехода
     * @author Максим Дрючин (tgmaksim)
     * */
    private fun openDay(item: LinearLayout, anim: Boolean = true) {
        val lastIndex = ui.dayContainer.indexOfChild(lastSelected)
        val index = ui.dayContainer.indexOfChild(item)

        // Обновление выделения
        lastSelected.isSelected = false
        item.isSelected = true
        lastSelected = item

        // Центрирование кнопки текущего дня
        ui.dayScroll.post {
            val scrollTo = item.left - (ui.dayScroll.width - item.width) / 2
            ui.dayScroll.smoothScrollTo(scrollTo, 0)
        }

        val lastDate = LocalDate.now().plusDays(lastIndex.toLong())
        val date = LocalDate.now().plusDays(index.toLong())

        // Выбор стороны анимации
        val toRight = index > lastIndex
        val inAnim = AnimationUtils.loadAnimation(
            requireContext(),
            if (toRight) R.anim.slide_in_right else R.anim.slide_in_left
        )
        val outAnim = AnimationUtils.loadAnimation(
            requireContext(),
            if (toRight) R.anim.slide_out_left else R.anim.slide_out_right
        )

        val lessons = searchScheduleDay(date.format(dateFormat))?.lessons
        val lastLessons = searchScheduleDay(lastDate.format(dateFormat))?.lessons

        outAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Заполнение расписания
                showScheduleDay(date.format(dateFormat))
                if (lessons?.isEmpty() == false)
                    ui.lessons.startAnimation(inAnim)
            }
        })

        // Если анимация не нужна или перелистывания осуществляется между выходными
        if (!anim || lastLessons?.isEmpty() != false && lessons?.isEmpty() != false)
            showScheduleDay(date.format(dateFormat))
        // Показ анимации и смена расписания
        else
            ui.lessons.startAnimation(outAnim)
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