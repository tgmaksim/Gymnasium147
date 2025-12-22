package ru.tgmaksim.gymnasium.pages.schedule

import android.view.View
import android.text.Spanned
import android.graphics.Color
import android.text.TextPaint
import android.view.ViewGroup
import android.widget.TextView
import android.widget.LinearLayout
import android.view.LayoutInflater
import android.text.SpannableString
import android.text.style.ClickableSpan
import androidx.core.content.ContextCompat
import android.text.method.LinkMovementMethod
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.api.ScheduleHours
import ru.tgmaksim.gymnasium.api.ScheduleLesson
import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.utilities.CacheManager
import ru.tgmaksim.gymnasium.fragments.WebViewFragment
import ru.tgmaksim.gymnasium.api.ScheduleExtracurricularActivity

/**
 * Адаптер списка уроков в расписании на странице
 * @author Максим Дрючин (tgmaksim)
 * @see DayPagerAdapter
 * */
class LessonsAdapter(
    private val activity: FragmentActivity,
    private var lessons: List<ScheduleLesson> = emptyList(),
    private var ea: List<ScheduleExtracurricularActivity> = emptyList(),
    private var hoursEA: ScheduleHours? = null
) : RecyclerView.Adapter<LessonsAdapter.LessonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.schedule_lesson, parent, false)
        return LessonViewHolder(view, activity)
    }

    override fun getItemCount(): Int =
        lessons.size + if (ea.isEmpty()) 0 else 1  // Карточка внеурочных занятий всегда одна

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        holder.bind(getLesson(holder, position))
    }

    /**
     * Обновление расписания
     * @param newLessons новые уроки в виде списка уроков [ScheduleLesson]
     * @param newHoursEA время внеурочек данного дня (если есть) в виде [ScheduleHours]
     * @param newEA внеурочные занятия данного дня
     * @author Максим Дрючин (tgmaksim)
     * */
    fun updateLessons(
        newLessons: List<ScheduleLesson>,
        newHoursEA: ScheduleHours?,
        newEA: List<ScheduleExtracurricularActivity>
    ) {
        lessons = newLessons
        hoursEA = newHoursEA
        ea = newEA
        notifyItemRangeChanged(0, itemCount)
    }

    private fun getLesson(holder: LessonViewHolder, position: Int): ScheduleLesson {
        // Для уроков объекты уже загружены
        if (position < lessons.size) {
            // Возвращение цвета
            holder.view.background = ContextCompat.getDrawable(holder.view.context, R.drawable.bg_lesson)

            return lessons[position]
        } else {
            // Выделение фона другим цветом
            holder.view.background = ContextCompat.getDrawable(
                holder.view.context, R.drawable.bg_lesson_extra)

            val subjects = ea.joinToString("\n") { it.subject }
            val place = ea.joinToString("; ") { it.place }

            // Для всех внеурочек создается один объект
            return ScheduleLesson(
                number = position,
                subject = subjects,
                place = place,
                hours = hoursEA ?: ScheduleHours(start = "00:00", end = "00:00"),
                logs = emptyList(),
                othersMarks = emptyList(),
                homework = null,
                files = emptyList()
            )
        }
    }

    class LessonViewHolder(val view: View, private val activity: FragmentActivity) : RecyclerView.ViewHolder(view) {
        val time: TextView = view.findViewById(R.id.time)
        val subject: TextView = view.findViewById(R.id.subject)
        val place: TextView = view.findViewById(R.id.place)
        val logs: RecyclerView = view.findViewById(R.id.logs)
        val homework: TextView = view.findViewById(R.id.homework)
        val homeworkGroup: LinearLayout = view.findViewById(R.id.homeworkGroup)
        val filesContainer: LinearLayout = view.findViewById(R.id.filesContainer)

        fun bind(lesson: ScheduleLesson) {
            // Заполняется информация в элементе
            time.text = lesson.hours.stringFormat
            subject.text = lesson.subject
            place.text = lesson.place

            // Показывается или скрывается домашнее задание
            if (lesson.homework?.isEmpty() == false) {
                homework.text = lesson.homework
                homeworkGroup.visibility = View.VISIBLE
            } else {
                homework.text = R.string.homework_not_found.toString()
                homeworkGroup.visibility = View.GONE
            }

            filesContainer.removeAllViews()
            for (file in lesson.files) {
                val viewHomeworkFile = LayoutInflater.from(view.context).inflate(
                    R.layout.schedule_homework_file,
                    filesContainer,
                    false
                ) as LinearLayout

                createFileSpannable(activity, viewHomeworkFile, file.fileName, file.downloadUrl)
                filesContainer.addView(viewHomeworkFile)
            }

            // Показываются оценки и отметки о посещаемости
            logs.layoutManager = LinearLayoutManager(
                view.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            logs.adapter = LogsAdapter(lesson.logs)
        }

        private fun createFileSpannable(
            activity: FragmentActivity,
            viewHomeworkFile: LinearLayout,
            fileName: String,
            downloadUrl: String
        ) : LinearLayout {
            val fileNameView: TextView = viewHomeworkFile.findViewById(R.id.homeworkDocumentName)
            val spannable = SpannableString(fileName)

            // Кликабельная ссылка
            spannable.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // Открытие либо WebView, либо браузера
                    if (CacheManager.openWebView) {
                        activity.supportFragmentManager.beginTransaction().replace(
                            R.id.content_container,
                            WebViewFragment.newInstance(downloadUrl)
                        ).addToBackStack(null).commit()
                    } else {
                        Utilities.openUrl(activity, downloadUrl)
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

            return viewHomeworkFile
        }
    }
}