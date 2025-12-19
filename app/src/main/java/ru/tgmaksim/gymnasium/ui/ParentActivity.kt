package ru.tgmaksim.gymnasium.ui

import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.utilities.CacheManager

/**
 * Базовый класс для всех Activity
 * @author Максим Дрючин (tgmaksim)
 * */
open class ParentActivity : AppCompatActivity() {
    /**
     * Смена темы приложения на сохраненную в кеше
     * @author Максим Дрючин (tgmaksim)
     * */
    fun setupActivityTheme() {
        setTheme(R.style.Theme_Gymnasium)

        if (CacheManager.isDarkTheme)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    /**
     * Настройка системных краев (верхнего и нижнего)
     * @author Максим Дрючин (tgmaksim)
     * */
    fun setupSystemBars(contentContainer: ViewGroup) {
        // Установка отступа сверху на высоту системной панели
        ViewCompat.setOnApplyWindowInsetsListener(contentContainer) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            if (contentContainer is FrameLayout) {
                v.updateLayoutParams<RelativeLayout.LayoutParams> {
                    topMargin = topInset
                }
            } else if (contentContainer is LinearLayout) {
                v.updateLayoutParams<FrameLayout.LayoutParams> {
                    topMargin = topInset
                }
            }

            insets
        }

        // Взятие системных полей под контроль приложения для SDK < 35
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            window.setDecorFitsSystemWindows(false)
        }
    }
}