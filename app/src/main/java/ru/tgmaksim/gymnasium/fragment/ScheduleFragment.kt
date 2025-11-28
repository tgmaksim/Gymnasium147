package ru.tgmaksim.gymnasium.fragment

import java.util.Locale
import android.util.Log
import android.os.Bundle
import android.view.View
import java.time.LocalDate
import android.widget.Toast
import android.view.ViewGroup
import android.content.Intent
import android.content.Context
import android.widget.TextView
import kotlinx.coroutines.launch
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import android.view.animation.Animation
import androidx.lifecycle.lifecycleScope
import java.time.format.DateTimeFormatter
import ru.tgmaksim.gymnasium.MainActivity
import ru.tgmaksim.gymnasium.LoginActivity
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.api.Lesson
import ru.tgmaksim.gymnasium.api.Schedule
import ru.tgmaksim.gymnasium.api.Schedules
import ru.tgmaksim.gymnasium.api.ScheduleDay
import ru.tgmaksim.gymnasium.utilities.CacheManager
import ru.tgmaksim.gymnasium.databinding.ItemDayBinding
import ru.tgmaksim.gymnasium.api.ExtracurricularActivity
import ru.tgmaksim.gymnasium.databinding.FragmentScheduleBinding

fun Int.dp(context: Context): Int =
    (this * context.resources.displayMetrics.density).toInt()

class LessonsAdapter(private var lessons: List<Lesson>,
                     private var hoursExtracurricularActivities: String?,
                     private var extracurricularActivities: List<ExtracurricularActivity>) :
    RecyclerView.Adapter<LessonsAdapter.LessonViewHolder>() {

    class LessonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvSubject: TextView = view.findViewById(R.id.tvSubject)
        val tvRoom: TextView = view.findViewById(R.id.tvRoom)
        val tvHomework: TextView = view.findViewById(R.id.tvHomework)
        val tvHomeworkGroup: LinearLayout = view.findViewById(R.id.tvHomeworkGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lesson, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val lesson = if (position < lessons.size) {
            lessons[position]
        } else {
            val subjects = extracurricularActivities.joinToString("\n") { it.subject }
            val place = extracurricularActivities.joinToString("; ") { it.place }
            Lesson(position, subjects, place, hoursExtracurricularActivities!!, null)
        }
        holder.tvTime.text = lesson.hours
        holder.tvSubject.text = lesson.subject
        holder.tvRoom.text = lesson.place

        if (lesson.homework?.isEmpty() == false) {
            holder.tvHomework.text = lesson.homework
            holder.tvHomeworkGroup.visibility = View.VISIBLE
        }
        else {
            holder.tvHomework.text = "Нет домашнего задания"
            holder.tvHomeworkGroup.visibility = View.GONE
        }
    }

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
        return lessons.size + if (extracurricularActivities.isEmpty()) 0 else 1
    }
}

class ScheduleFragment : Fragment() {
    private lateinit var ui: FragmentScheduleBinding
    private lateinit var lastSelected: LinearLayout
    private var schedule: List<ScheduleDay>? = null
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = FragmentScheduleBinding.inflate(inflater, container, false)
        mainActivity = activity as MainActivity

        lifecycleScope.launch {
            init()
        }

        return ui.root
    }

    private suspend fun init() {
        // Если расписание еще не загружено, делаем это
        var processed = true
        if (schedule == null) {
            // Показываем пользователю, что расписание загружается
            mainActivity.showLoading()

            try {
                // Получаем расписание от сервера
                val schedules: Schedules = Schedule.getSchedule()

                // Если сессия не авторизована, то открываем Login
                // Если произошла ошибка, выводим ее
                if (!schedules.status) {
                    if (schedules.unauthorized) {
                        processed = false
                        CacheManager.clear()
                        val intent = Intent(context, LoginActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            context,
                            "Произошла ошибка на сервере...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    schedule = schedules.schedule
                }
            } catch (e: Exception) {
                Log.e("api-error", null, e)
                Toast.makeText(
                    context,
                    "Произошла ошибка при загрузке расписания",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Завершаем загрузку расписания
            mainActivity.hideLoading()
        }

        // Если не получилось загрузить расписание, используем кеш
        if (processed) {
            if (schedule == null)
                schedule = Schedule.getCacheSchedule()

            // Отображаем даты на две недели (15 дней)
            fillDays()

            // Показываем расписание на сегодня
            val format: DateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd")
            val date: String = LocalDate.now().format(format)
            fillDay(date)
        }
    }

    private fun fillDays() {
        val container = ui.dayContainer

        val today = LocalDate.now()

        repeat(15) { i ->  // Заполнение дней на две недели (15 дней)
            val item = ItemDayBinding.inflate(layoutInflater, container, false)
            item.root.setBackgroundResource(R.drawable.bg_day_selector)

            // Указываем число и день недели
            val date = today.plusDays(i.toLong())
            item.tvDayNumber.text = date.dayOfMonth.toString()
            @Suppress("DEPRECATION")  // У Local нет России
            val dayOfWeek = date.format(DateTimeFormatter.ofPattern("EE", Locale("ru")))
            item.tvDayWeek.text = dayOfWeek.uppercase()

            // Устанавливаем расстояние между элементами
            val lp = item.root.layoutParams as LinearLayout.LayoutParams
            lp.marginEnd = 14.dp(requireContext())
            item.root.layoutParams = lp

            // Выбираем активный день
            if (i == 0) {
                item.root.isSelected = true
                lastSelected = item.root
            }

            // Определяем действие при нажатии
            item.root.setOnClickListener { openDay(item) }

            container.addView(item.root)
        }
    }

    private fun fillDay(date: String) {
        // Первый раз инициализируем адаптер
        if (ui.rvLessons.adapter == null) {
            ui.rvLessons.adapter = LessonsAdapter(
                emptyList(),
                null,
                emptyList()
            )
            ui.rvLessons.layoutManager = LinearLayoutManager(context)
        }

        if (schedule == null) {
            return
        }

        // Показываем расписание на сегодня
        var lessons: List<Lesson> = emptyList()
        var hoursExtracurricularActivities: String? = null
        var extracurricularActivities: List<ExtracurricularActivity> = emptyList()
        for (scheduleDay: ScheduleDay in schedule) {
            if (scheduleDay.date == date) {
                lessons = scheduleDay.lessons
                hoursExtracurricularActivities = scheduleDay.hoursExtracurricularActivities
                extracurricularActivities = scheduleDay.extracurricularActivities
            }
        }

        (ui.rvLessons.adapter as LessonsAdapter).updateLessons(
            lessons,
            hoursExtracurricularActivities,
            extracurricularActivities
        )
    }

    private fun openDay(item: ItemDayBinding) {
        val lastIndex: Int = ui.dayContainer.indexOfChild(lastSelected)
        val index: Int = ui.dayContainer.indexOfChild(item.root)

        // Обновляем выделение
        lastSelected.isSelected = false
        item.root.isSelected = true
        lastSelected = item.root

        val date = LocalDate.now().plusDays(index.toLong())
        val format: DateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd")

        val toRight = index > lastIndex
        val inAnim = AnimationUtils.loadAnimation(
            context,
            if (toRight) R.anim.slide_in_right else R.anim.slide_in_left
        )
        val outAnim = AnimationUtils.loadAnimation(
            context,
            if (toRight) R.anim.slide_out_left else R.anim.slide_out_right
        )

        outAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                fillDay(date.format(format))
                ui.rvLessons.startAnimation(inAnim)
            }
        })

        ui.rvLessons.startAnimation(outAnim)
    }
}
