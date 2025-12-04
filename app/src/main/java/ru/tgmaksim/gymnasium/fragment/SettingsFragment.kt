package ru.tgmaksim.gymnasium.fragment

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.fragment.app.Fragment

import ru.tgmaksim.gymnasium.R
import ru.tgmaksim.gymnasium.api.VersionChecker
import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.utilities.CacheManager
import ru.tgmaksim.gymnasium.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private lateinit var ui: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = FragmentSettingsBinding.inflate(inflater, container, false)
        ui.settingsWebViewSwitch.isChecked = CacheManager.openWebView

        // Установка Switch в нужное положение
        ui.settingsWebViewSwitch.setOnCheckedChangeListener { _, isChecked ->
            CacheManager.openWebView = isChecked
        }

        // Если идет процесс скачивания (или уже есть результат)
        if (CacheManager.versionStatus.newLatestVersion) {
            ui.updateApplication.visibility = View.VISIBLE

            // Показ логов обновления
            if (CacheManager.versionStatus.updateLog != null)
                ui.updateDescription.text = CacheManager.versionStatus.updateLog
            else
                ui.updateDescription.text = getString(R.string.important_update)

            // Запуск или завершение анимации
            if (CacheManager.loadedUpdate == false)
                startLoading()
            else
                finishLoading()
        } else {
            ui.updateApplication.visibility = View.GONE
        }

        // Нажатие на кнопку обновления
        ui.buttonUpdate.setOnClickListener {
            // Обновление загружено
            if (CacheManager.loadedUpdate == true) {
                // Проверка разрешения на установку Apk
                if (VersionChecker.checkPermissionApk(requireActivity())) {
                    // Попытка обновления
                    try {
                        VersionChecker.installApk(requireActivity())
                    } catch (e: Exception) {
                        Utilities.log(e)
                        Utilities.showText(requireContext(), "Не удалось обновить")
                    }
                } else {
                    // Запрос разрешения на установку Apk
                    Utilities.showText(requireContext(), "Разрешите самостоятельное обновление")
                    VersionChecker.requestPermissionApk(requireActivity())
                }
            } else if (CacheManager.loadedUpdate == false) {
                Utilities.showText(requireContext(), "Обновление еще скачивается!")
            } else {
                Utilities.showText(requireContext(), "Не удалось скачать обновление...")
            }
        }

        return ui.root
    }

    /** Callback для показа прогресса скачивания */
    fun progressLoadUpdate(percent: Int) {
        if (::ui.isInitialized)
            ui.updateProgress.progress = percent
    }

    /** Запуск анимации загрузки (круговой ProgressBar) */
    fun startLoading() {
        if (::ui.isInitialized) {
            ui.loadingOverlay.visibility = View.VISIBLE
            ui.updateProgress.visibility = View.VISIBLE
        }
    }

    /** Скрытие анимации загрузки (круговой ProgressBar) */
    fun finishLoading() {
        if (::ui.isInitialized) {
            ui.loadingOverlay.visibility = View.GONE
            ui.updateProgress.visibility = View.GONE
        }
    }
}