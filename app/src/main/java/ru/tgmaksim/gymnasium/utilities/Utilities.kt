package ru.tgmaksim.gymnasium.utilities

import android.util.Log
import android.widget.Toast
import android.content.Intent
import android.content.Context
import androidx.core.net.toUri
import androidx.annotation.StringRes
import android.content.DialogInterface
import android.content.ActivityNotFoundException
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import ru.tgmaksim.gymnasium.R

object Utilities {
    /** Открытие ссылки в браузере */
    fun openUrl(context: Context, url: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(browserIntent)
        } catch (e: ActivityNotFoundException) {
            log(e)
            showText(context, "Не найдено приложение для открытия ссылки")
        }
    }

    /** Показ уведомлений */
    fun showText(context: Context, text: String, long: Boolean = false) {
        Toast.makeText(
            context,
            text,
            if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).show()
    }

    fun showText(context: Context, @StringRes resId: Int, long: Boolean = false) {
        Toast.makeText(
            context,
            resId,
            if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).show()
    }

    /** Логирование */
    fun log(text: String) {
        Log.d("Gymnasium", text)
    }

    fun log(e: Exception) {
        Log.e("Gymnasium", null, e)
    }

    /** Показ диалогового окна с уведомлением */
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