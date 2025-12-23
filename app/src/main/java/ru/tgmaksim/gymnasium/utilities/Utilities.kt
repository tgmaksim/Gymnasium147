package ru.tgmaksim.gymnasium.utilities

import android.util.Log
import android.widget.Toast
import android.content.Intent
import android.content.Context
import androidx.core.net.toUri
import androidx.annotation.StringRes
import android.content.DialogInterface
import android.content.ActivityNotFoundException

import com.google.firebase.Firebase
import com.google.firebase.analytics.logEvent
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.ParametersBuilder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.FirebaseCrashlytics

import ru.tgmaksim.gymnasium.R

/**
 * Утилиты приложения
 * @author Максим Дрючин (tgmaksim)
 * */
object Utilities {
    /**
     * Открытие ссылки в браузере
     * @param context Android-контекст
     * @param url Ссылка для открытия
     * @return Результат операции
     * @author Максим Дрючин (tgmaksim)
     * */
    fun openUrl(context: Context, url: String): Boolean {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(browserIntent)
            return true
        } catch (e: ActivityNotFoundException) {
            log(e)
            showText(context, "Не найдено приложение для открытия ссылки")
            return false
        }
    }

    /**
     * Показ системного текстового сообщения
     * @param context Android-контекст
     * @param text текст сообщения
     * @param long показывать ли долгое сообщение
     * @author Максим Дрючин (tgmaksim)
     * */
    fun showText(context: Context, text: String, long: Boolean = false) {
        Toast.makeText(
            context,
            text,
            if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Показ системного текстового сообщения
     * @param context Android-контекст
     * @param resId текст сообщения в виде ресурса
     * @param long показывать ли долгое сообщение
     * @author Максим Дрючин (tgmaksim)
     * */
    fun showText(context: Context, @StringRes resId: Int, long: Boolean = false) {
        Toast.makeText(
            context,
            resId,
            if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Логирование данных локально и отправка в Firebase
     * @param string текстовые данные для логирования
     * @param tag дополнительный тег для логов
     * @param analytics параметры для firebase-аналитики
     * @author Максим Дрючин (tgmaksim)
     * */
    fun log(string: String, tag: String, analytics: (ParametersBuilder.() -> Unit)? = null) {
        Log.d("Gymnasium.$tag", string)

        Firebase.analytics.logEvent(tag, analytics ?: return)
    }

    /**
     * Логирование данных о некорректных данных локально и отправка в Firebase Crashlytics
     * @param e описание ошибки
     * @author Максим Дрючин (tgmaksim)
     * */
    fun log(e: String) {
        Log.e("Gymnasium.error", e)

        FirebaseCrashlytics.getInstance().log(e)
    }

    /**
     * Логирование данных об ошибке локально и отправка в Firebase Crashlytics
     * @param e возникшая ошибка
     * @author Максим Дрючин (tgmaksim)
     * */
    fun log(e: Exception) {
        Log.e("Gymnasium.error", "Ошибка", e)

        FirebaseCrashlytics.getInstance().recordException(e)
    }

    /**
     * Показ диалогового окна с уведомлением
     * @param context Android-контекст
     * @param title заголовок диалогового окна
     * @param message текст сообщения
     * @param buttonText текст кнопки
     * @param back показывать ли кнопку назад
     * @param buttonListener действие при нажатии кнопки
     * @author Максим Дрючин (tgmaksim)
     * */
    fun showAlertDialog(
        context: Context,
        title: String,
        message: String,
        buttonText: String,
        back: Boolean = true,
        buttonListener: DialogInterface.OnClickListener
    ) {
        MaterialAlertDialogBuilder(context, R.style.AppDialogTheme).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(buttonText, buttonListener)
            if (back) setNegativeButton("Отмена", null)
            setCancelable(back)
            show()
        }
    }
}