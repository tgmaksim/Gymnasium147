package ru.tgmaksim.gymnasium.utilities

import android.net.Uri
import android.util.Log
import android.widget.Toast
import android.content.Intent
import android.content.Context
import androidx.core.content.ContextCompat.startActivity

object Utilities {
    /** Открывает ссылку в браузере */
    fun openUrl(context: Context, url: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(context, browserIntent, null)
        } catch (e: android.content.ActivityNotFoundException) {
            Log.e("android-error", null, e)
            Toast.makeText(
                context,
                "Не найдено приложение для открытия веб-страницы",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}