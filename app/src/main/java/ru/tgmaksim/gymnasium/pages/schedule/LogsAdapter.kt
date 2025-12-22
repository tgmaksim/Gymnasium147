package ru.tgmaksim.gymnasium.pages.schedule

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.GradientDrawable

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.api.ScheduleLog

class LogsAdapter(
    private val logs: List<ScheduleLog>
) : RecyclerView.Adapter<LogsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.schedule_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(logs[position])
    }

    override fun getItemCount(): Int = logs.size

    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.scheduleLog)

        fun bind(log: ScheduleLog) {
            text.text = log.value
            (text.background.mutate() as GradientDrawable).setColor(getLogColor(log))
        }

        /**
         * Получение цвета оценки или отметки о посещаемости урока
         * @param log оценка или отметка о посещаемости урока
         * @return цвет фона
         * @author Максим Дрючин (tgmaksim)
         * */
        private fun getLogColor(log: ScheduleLog): Int {
            return ContextCompat.getColor(
                view.context,
                when (log.mood) {
                    "good" -> R.color.lesson_log_good
                    "average" -> R.color.lesson_log_average
                    "bad" -> R.color.lesson_log_bad
                    "more" -> R.color.lesson_log_more
                    else -> R.color.lesson_log_more
                }
            )
        }
    }
}
