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
import androidx.lifecycle.lifecycleScope
import java.time.format.DateTimeFormatter
import ru.tgmaksim.gymnasium.LoginActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.api.Lesson
import ru.tgmaksim.gymnasium.api.Schedule
import ru.tgmaksim.gymnasium.api.Schedules
import ru.tgmaksim.gymnasium.api.ScheduleDay
import ru.tgmaksim.gymnasium.utilities.CacheManager
import ru.tgmaksim.gymnasium.databinding.ItemDayBinding
import ru.tgmaksim.gymnasium.databinding.FragmentScheduleBinding

fun Int.dp(context: Context): Int =
    (this * context.resources.displayMetrics.density).toInt()

class LessonsAdapter(private var lessons: List<Lesson>) :
    RecyclerView.Adapter<LessonsAdapter.LessonViewHolder>() {

    class LessonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvSubject: TextView = view.findViewById(R.id.tvSubject)
        val tvRoom: TextView = view.findViewById(R.id.tvRoom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lesson, parent, false)
        return LessonViewHolder(view)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val lesson = lessons[position]
        holder.tvTime.text = lesson.hours
        holder.tvSubject.text = lesson.subject
        holder.tvRoom.text = lesson.place
    }

    fun updateLessons(newLessons: List<Lesson>) {
        lessons = newLessons
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = lessons.size
}

class ScheduleFragment : Fragment() {
    private lateinit var ui: FragmentScheduleBinding
    private lateinit var lastSelected: LinearLayout
    private var schedule: List<ScheduleDay>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = FragmentScheduleBinding.inflate(inflater, container, false)

        lifecycleScope.launch {
            var processed = true
            if (schedule == null) {
                try {
                    val schedules: Schedules = Schedule.getSchedule()

                    if (!schedules.status) {
                        if (schedules.unauthorized) {
                            processed = false
                            CacheManager.apiSession = null
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
            }

            if (processed) {
                if (schedule == null)
                    schedule = Schedule.getCacheSchedule()

                fillDays()

                val format: DateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd")
                val date: String = LocalDate.now().format(format)
                fillDay(date)
            }
        }

        return ui.root
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
        if (ui.rvLessons.adapter == null) {
            val adapter = LessonsAdapter(emptyList())
            ui.rvLessons.adapter = adapter
            ui.rvLessons.layoutManager = LinearLayoutManager(context)
        }

        if (schedule == null) {
            (ui.rvLessons.adapter as LessonsAdapter).updateLessons(emptyList())
            return
        }

        var lessons: List<Lesson>? = null
        for (scheduleDay: ScheduleDay in schedule) {
            if (scheduleDay.date == date) {
                lessons = scheduleDay.lessons
            }
        }
        if (lessons == null) {
            (ui.rvLessons.adapter as LessonsAdapter).updateLessons(emptyList())
            return
        }

        (ui.rvLessons.adapter as LessonsAdapter).updateLessons(lessons)
    }

    private fun openDay(item: ItemDayBinding) {
        // Обновляем выделение
        lastSelected.isSelected = false
        item.root.isSelected = true
        lastSelected = item.root

        // Заполняем страницу новыми данными
        val index: Int = ui.dayContainer.indexOfChild(item.root)
        val date = LocalDate.now().plusDays(index.toLong())
        val format: DateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd")
        fillDay(date.format(format))
    }
}
