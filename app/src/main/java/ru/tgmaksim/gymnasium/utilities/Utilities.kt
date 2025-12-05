package ru.tgmaksim.gymnasium.utilities

import java.io.File
import android.util.Log
import android.widget.Toast
import android.content.Intent
import android.content.Context
import androidx.core.net.toUri
import java.security.MessageDigest
import android.content.DialogInterface
import android.content.ActivityNotFoundException
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import ru.tgmaksim.gymnasium.R

object Utilities {
    /** Открывает ссылку в браузере */
    fun openUrl(context: Context, url: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(browserIntent)
        } catch (e: ActivityNotFoundException) {
            log(e)
            showText(context, "Не найдено приложение для открытия веб-страницы")
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

    /** Логирование */
    fun log(text: String) {
        Log.d("Gymnasium", text)
    }

    fun log(e: Exception) {
        Log.e("Gymnasium", null, e)
    }

    /** Получение хеша файла (sha256) */
    fun getFileSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")

        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int

            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }

        return digest.digest().joinToString("") { "%02x".format(it) }
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