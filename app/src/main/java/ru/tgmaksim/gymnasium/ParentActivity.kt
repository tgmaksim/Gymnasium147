package ru.tgmaksim.gymnasium

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

import ru.tgmaksim.gymnasium.utilities.CacheManager

open class ParentActivity : AppCompatActivity() {
    fun setActivityTheme() {
        if (CacheManager.isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setTheme(R.style.Theme_Gymnasium)
    }

    fun setupSystemBars(contentContainer: ViewGroup) {
        // Установка отступа сверху на высоту системной панели
        ViewCompat.setOnApplyWindowInsetsListener(contentContainer) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            if (contentContainer::class == FrameLayout::class) {
                v.updateLayoutParams<RelativeLayout.LayoutParams> {
                    topMargin = topInset
                }
            } else if (contentContainer::class == LinearLayout::class) {
                v.updateLayoutParams<FrameLayout.LayoutParams> {
                    topMargin = topInset
                }
            }


            insets
        }

        // Взятие системных полей под контроль приложения
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            window.setDecorFitsSystemWindows(false)
        }
    }
}
