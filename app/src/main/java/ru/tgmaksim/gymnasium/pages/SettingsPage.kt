package ru.tgmaksim.gymnasium.pages

import android.os.Bundle
import android.view.View
import android.content.Intent
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.fragment.app.Fragment

import ru.tgmaksim.gymnasium.BuildConfig
import ru.tgmaksim.gymnasium.ui.LoginActivity
import ru.tgmaksim.gymnasium.ui.ParentActivity
import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.utilities.CacheManager
import ru.tgmaksim.gymnasium.pages.schedule.SchedulePage
import ru.tgmaksim.gymnasium.utilities.NotificationManager
import ru.tgmaksim.gymnasium.databinding.SettingsPageBinding

/**
 * Fragment-страница с настройками приложения
 * @author Максим Дрючин (tgmaksim)
 * @see ru.tgmaksim.gymnasium.ui.MainActivity
 * */
class SettingsPage : Fragment() {
    private lateinit var ui: SettingsPageBinding
    private var isDarkTheme: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (::ui.isInitialized && CacheManager.isDarkTheme == isDarkTheme)
            return ui.root

        ui = SettingsPageBinding.inflate(inflater, container, false)
        isDarkTheme = CacheManager.isDarkTheme

        // Установка Switch в нужное положение
        ui.settingsWebView.isChecked = CacheManager.openWebView
        ui.settingsEANotifications.isChecked = CacheManager.EANotifications
        ui.settingsTheme.isChecked = CacheManager.isDarkTheme

        ui.settingsWebView.setOnCheckedChangeListener { _, isChecked ->
            CacheManager.openWebView = isChecked
        }
        ui.settingsEANotifications.setOnCheckedChangeListener { switch, isChecked ->
            if (!isChecked) {
                CacheManager.EANotifications = false
                return@setOnCheckedChangeListener
            }

            val context = requireContext()
            if (NotificationManager.checkPermission(context) && NotificationManager.canScheduleExactAlarms(context)) {
                CacheManager.EANotifications = true
                SchedulePage.createRemindEA(context)
            } else {
                switch.isChecked = false
                NotificationManager.setupPostNotifications(requireActivity())
            }
        }
        ui.settingsTheme.setOnCheckedChangeListener { _, isChecked ->
            if (CacheManager.isDarkTheme != isChecked) {
                CacheManager.isDarkTheme = isChecked
                (requireActivity() as ParentActivity).setupActivityTheme()
            }
        }

        CacheManager.versionStatus?.let {
            ui.updateApplication.visibility = View.VISIBLE
            ui.updateDescription.text = StringBuilder(it.latestVersionString).apply {
                append(' ')
                append("(${it.latestVersionNumber})")
                append('\n')
                append(it.updateLogs)
            }.toString()
        }

        // Нажатие на кнопку обновления
        ui.buttonUpdate.setOnClickListener {
            Utilities.openUrl(requireContext(), BuildConfig.DOMAIN)
        }
        ui.buttonOpenSite.setOnClickListener {
            Utilities.openUrl(requireContext(), BuildConfig.DOMAIN)
        }
        ui.buttonLogout.setOnClickListener {
            CacheManager.apiSession = null
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utilities.log("SettingsPage загружена")
    }
}