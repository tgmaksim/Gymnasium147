package ru.tgmaksim.gymnasium.fragment

import kotlin.math.abs
import java.util.Locale
import android.os.Bundle
import android.view.View
import java.time.LocalDate
import java.time.LocalTime
import android.widget.Toast
import android.text.Spanned
import android.view.ViewGroup
import android.graphics.Color
import android.text.TextPaint
import android.content.Intent
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
import ru.tgmaksim.gymnasium.ui.MainActivity
import android.view.animation.AnimationUtils
import android.text.method.LinkMovementMethod
import ru.tgmaksim.gymnasium.ui.LoginActivity
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.api.Lesson
import ru.tgmaksim.gymnasium.api.Schedule
import ru.tgmaksim.gymnasium.api.Schedules
import ru.tgmaksim.gymnasium.api.ScheduleDay
import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.api.HomeworkDocument
import ru.tgmaksim.gymnasium.utilities.CacheManager
import ru.tgmaksim.gymnasium.databinding.ItemDayBinding
import ru.tgmaksim.gymnasium.api.ExtracurricularActivity
import ru.tgmaksim.gymnasium.databinding.FragmentScheduleBinding

class LessonsAdapter(private var lessons: List<Lesson>,
                     private var hoursExtracurricularActivities: String?,
                     private var extracurricularActivities: List<ExtracurricularActivity>,
                     private var mainActivity: MainActivity) :
    RecyclerView.Adapter<LessonsAdapter.LessonViewHolder>() {

    class LessonViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        val time: TextView = view.findViewById(R.id.time)
        val subject: TextView = view.findViewById(R.id.subject)
        val room: TextView = view.findViewById(R.id.room)
        val homework: TextView = view.findViewById(R.id.homework)
        val homeworkGroup: LinearLayout = view.findViewById(R.id.homeworkGroup)
        val filesContainer: LinearLayout = view.findViewById(R.id.filesContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lesson, parent, false)
        return LessonViewHolder(view)
    }

    /** Создает элемент-карточку урока или внеурочки */
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
            val subjects = extracurricularActivities.joinToString("\n") { it.subject }
            val place = extracurricularActivities.joinToString("; ") { it.place }
            Lesson(
                position,
                subjects,
                place,
                hoursExtracurricularActivities!!
            )
        }

        // Заполняется информация в элементе
        holder.time.text = lesson.hours
        holder.subject.text = lesson.subject
        holder.room.text = lesson.place

        // Показывается или скрывается домашнее задание
        if (lesson.homework?.isEmpty() == false) {
            holder.homework.text = lesson.homework
            holder.homeworkGroup.visibility = View.VISIBLE
        }
        else {
            holder.homework.text = R.string.homework_not_found.toString()
            holder.homeworkGroup.visibility = View.GONE
        }

        // Строка с гиперсылкой на ресурс (если есть)
        val view = LayoutInflater.from(holder.view.context).inflate(
            R.layout.item_homework_file,
            holder.filesContainer,
            false
        ) as LinearLayout
        holder.filesContainer.removeAllViews()

        // Добавление прикрепленных файлов к уроку
        for (file: HomeworkDocument in lesson.files) {
            val fileNameView: TextView = view.findViewById(R.id.homeworkDocumentName)
            val spannable = SpannableString(file.fileName)

            // Кликабельная ссылка
            spannable.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val url = file.downloadUrl

                    // Открытие либо WebView, либо браузера
                    if (CacheManager.openWebView) {
                        mainActivity.supportFragmentManager.beginTransaction()
                            .replace(
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

    /** Обновляет расписание и показывает его */
    @SuppressLint("NotifyDataSetChanged")
    fun updateLessons(
        newLessons: List<Lesson>,
        newHoursExtracurricularActivities: String?,
        newExtracurricularActivity: List<ExtracurricularActivity>
    ) {
        lessons = newLessons
        hoursExtracurricularActivities = newHoursExtracurricularActivities
        extracurricularActivities = newExtracurricularActivity
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        // Карточка внеурочки всегда одна, либо ее нет
        return lessons.size + if (extracurricularActivities.isEmpty()) 0 else 1
    }
}

class ScheduleFragment : Fragment() {
    private lateinit var ui: FragmentScheduleBinding
    private lateinit var lastSelected: LinearLayout
    private lateinit var mainActivity: MainActivity
    @Suppress("DEPRECATION")
    private lateinit var gestureDetector: GestureDetectorCompat
    private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Расписание не уничтожается даже после перерисовки
    companion object {
        private var schedule: List<ScheduleDay>? = null
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
        mainActivity = activity as MainActivity

        // Синхронизация только при первой отрисовке
        if (schedule == null) {
            lifecycleScope.launch {
                // Показывается анимация загрузки
                mainActivity.showLoading()

                loadCloudSchedule()

                // Завершение загрузки расписания
                mainActivity.hideLoading()
            }
        }

        initSchedule()
        initTouchListener()

        return ui.root
    }

    /** Инициализация обработчика жестов для перелистывания дней */
    private fun initGestureDetectorCompat() {
        // Обработчик пролистывания дней
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
                    val diffX = e2.x - e1.x

                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY) {
                        val index = ui.dayContainer.indexOfChild(lastSelected)

                        return if (diffX < 0 && index + 1 < 15) {
                            openDay(ui.dayContainer.getChildAt(index + 1) as LinearLayout)
                            true
                        } else if (diffX > 0 && index - 1 >= 0) {
                            openDay(ui.dayContainer.getChildAt(index - 1) as LinearLayout)
                            true
                        } else {
                            false
                        }
                    }

                    return false
                }
            })
    }

    /** Привязка обработчиков жестов для перелистывания */
    private fun initTouchListener() {
        // Пролистывание доступно на списке уроков и фото выходных
        @SuppressLint("ClickableViewAccessibility")
        ui.lessons.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP) {
                v.performClick()
            }
            false
        }
        @SuppressLint("ClickableViewAccessibility")
        ui.weekendPhoto.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP) {
                v.performClick()
            }
            true
        }
    }

    /** Инициализация страницы расписания с данными из кеша */
    private fun initSchedule() {
        schedule = Schedule.getCacheSchedule()

        // Показ расписания на сегодня или завтра
        val date = if (LocalTime.now().hour >= 15) {
            LocalDate.now().plusDays(1)
        } else {
            LocalDate.now()
        }
        fillDay(date.format(dateFormat))

        // Отображение даты на две недели (15 дней)
        fillDays()
    }

    /** Загрузка расписания с сервера */
    private suspend fun loadCloudSchedule() {
        val cacheSchedule = schedule

        try {
            val schedules: Schedules = Schedule.getSchedule()

            // Если сессия не авторизована, то открывается Login
            // Если произошла ошибка, выводится ошибка
            if (!schedules.status) {
                if (schedules.unauthorized) {
                    val intent = Intent(context, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        context,
                        R.string.error_api,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                schedule = schedules.schedule  // Сохранение расписания
            }
        } catch (e: Exception) {
            Utilities.log(e)
            Toast.makeText(
                context,
                R.string.error_load_schedule,
                Toast.LENGTH_SHORT
            ).show()
        }

        // Есть изменения
        if (cacheSchedule != schedule)
            openDay(lastSelected, anim = false)
    }

    /** Загрузка списка дней с датами и сокращением дня недели */
    private fun fillDays() {
        val container = ui.dayContainer

        val today = LocalDate.now()
        val time = LocalTime.now()

        repeat(15) { i ->  // Заполнение дней на две недели (15 дней)
            val item = ItemDayBinding.inflate(layoutInflater, container, false)
            item.root.setBackgroundResource(R.drawable.bg_button_day_selected)

            // Число и день недели
            val date = today.plusDays(i.toLong())
            item.dayNumber.text = date.dayOfMonth.toString()

            @Suppress("DEPRECATION")  // У Local нет России
            val dayOfWeek = date.format(
                DateTimeFormatter.ofPattern("EE", Locale("ru")))
            item.weekday.text = dayOfWeek.uppercase()

            // Выбор активного дня: до 15:00 - текущий, после - следующий
            if (i == 0 && time.hour < 15 || i == 1 && time.hour >= 15) {
                item.root.isSelected = true
                lastSelected = item.root
            }

            // Определение действия при нажатии
            item.root.setOnClickListener { openDay(item.root) }

            container.addView(item.root)
        }
    }

    /** Показ расписания на данный день */
    private fun fillDay(date: String) {
        // Первый раз инициализация адаптера
        if (ui.lessons.adapter == null) {
            ui.lessons.adapter = LessonsAdapter(
                emptyList(),
                null,
                emptyList(),
                mainActivity
            )
            ui.lessons.layoutManager = LinearLayoutManager(context)
        }

        if (schedule == null)
            return

        var lessons: List<Lesson> = emptyList()
        var hoursExtracurricularActivities: String? = null
        var extracurricularActivities: List<ExtracurricularActivity> = emptyList()

        // Поиск нужного дня в загруженном расписании
        val scheduleDay: ScheduleDay? = searchScheduleDay(date)
        if (scheduleDay != null) {
            lessons = scheduleDay.lessons
            hoursExtracurricularActivities = scheduleDay.hoursExtracurricularActivities
            extracurricularActivities = scheduleDay.extracurricularActivities
        }

        if (lessons.isEmpty() && scheduleDay != null) {
            // Обновление страницы с новыми данными
            (ui.lessons.adapter as LessonsAdapter).updateLessons(
                lessons,
                hoursExtracurricularActivities,
                extracurricularActivities
            )

            ui.lessons.visibility = View.GONE
            ui.weekendPhoto.visibility = View.VISIBLE
        }
        else {
            ui.lessons.visibility = View.VISIBLE
            ui.weekendPhoto.visibility = View.GONE

            // Обновление страницу с новыми данными
            (ui.lessons.adapter as LessonsAdapter).updateLessons(
                lessons,
                hoursExtracurricularActivities,
                extracurricularActivities
            )
        }
    }

    /** Ищет день в расписании с нужной датой формата [dateFormat] */
    private fun searchScheduleDay(date: String): ScheduleDay? {
        if (schedule == null)
            return null

        for (scheduleDay: ScheduleDay in schedule) {
            if (scheduleDay.date == date) {
                return scheduleDay
            }
        }

        return null
    }

    /** Открывает определенный день по нажатии на кнопку, показывая анимацию перехода */
    private fun openDay(item: LinearLayout, anim: Boolean = true) {
        val lastIndex: Int = ui.dayContainer.indexOfChild(lastSelected)
        val index: Int = ui.dayContainer.indexOfChild(item)

        // Обновление выделения
        lastSelected.isSelected = false
        item.isSelected = true
        lastSelected = item

        // Центрирование кнопки текущего дня
        ui.dayScroll.post {
            val scrollTo = item.left - (ui.dayScroll.width - item.width) / 2
            ui.dayScroll.smoothScrollTo(scrollTo, 0)
        }

        val date = LocalDate.now().plusDays(index.toLong())
        val lastDate = LocalDate.now().plusDays(lastIndex.toLong())
        val format: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        // Выбор стороны анимации
        val toRight = index > lastIndex
        val inAnim = AnimationUtils.loadAnimation(
            context,
            if (toRight) R.anim.slide_in_right else R.anim.slide_in_left
        )
        val outAnim = AnimationUtils.loadAnimation(
            context,
            if (toRight) R.anim.slide_out_left else R.anim.slide_out_right
        )

        val lessons = searchScheduleDay(date.format(format))?.lessons
        val lastLessons = searchScheduleDay(lastDate.format(format))?.lessons

        outAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Заполнение расписания
                fillDay(date.format(format))
                if (lessons?.isEmpty() != true)
                    ui.lessons.startAnimation(inAnim)
            }
        })

        if (!anim || lastLessons?.isEmpty() == true && lessons?.isEmpty() == true)
            fillDay(date.format(format))
        else
            // Показ анимации и смена расписания
            ui.lessons.startAnimation(outAnim)
    }
}