package ru.tgmaksim.gymnasium.pages.schedule

import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.api.ScheduleDay

/**
 * Адаптер расписания на странице
 * @author Максим Дрючин (tgmaksim)
 * @see SchedulePage
 * */
class DayPagerAdapter(
    private val activity: FragmentActivity,
    private var schedule: List<ScheduleDay?>
) : RecyclerView.Adapter<DayPagerAdapter.DayViewHolder>() {
    private lateinit var _lessons: RecyclerView
    val lessons: RecyclerView?
        get() = if (::_lessons.isInitialized) _lessons else null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.schedule_day, parent, false)
        _lessons = view.findViewById(R.id.lessons)
        return DayViewHolder(view, activity)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(schedule[position])
    }

    override fun getItemCount(): Int = schedule.size

    /**
     * Обновление расписания
     * @param newSchedule Новое расписание
     * @author Максим Дрючин (tgmaksim)
     * */
    fun updateSchedule(newSchedule: List<ScheduleDay>) {
        schedule = newSchedule
        notifyItemRangeChanged(0, itemCount)
    }

    class DayViewHolder(view: View, private var activity: FragmentActivity) : RecyclerView.ViewHolder(view) {
        private val lessons = view.findViewById<RecyclerView>(R.id.lessons)
        private val weekend = view.findViewById<View>(R.id.weekend_photo)

        init {
            lessons.layoutManager = LinearLayoutManager(
                view.context,
                LinearLayoutManager.VERTICAL,
                false
            )
        }

        /**
         * Показ определенного дня расписания
         * @param day День расписания
         * @author Максим Дрючин (tgmaksim)
         * */
        fun bind(day: ScheduleDay?) {
            if (day == null) {
                // День отсутствует в расписании
                lessons.visibility = View.GONE
                weekend.visibility = View.GONE
            } else if (day.lessons.isEmpty()) {
                // Показывается фото выходного дня, так как уроков нет
                lessons.visibility = View.GONE
                weekend.visibility = View.VISIBLE
            } else {
                // Показывается обновленное расписание
                weekend.visibility = View.GONE
                lessons.visibility = View.VISIBLE

                // Инициализация адаптера или обновление данных
                if (lessons.adapter == null)
                    lessons.adapter = LessonsAdapter(activity, day.lessons, day.ea, day.hoursEA)
                else
                    (lessons.adapter as LessonsAdapter).updateLessons(
                        day.lessons,
                        day.hoursEA,
                        day.ea
                    )
            }
        }
    }
}
