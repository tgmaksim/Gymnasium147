package ru.tgmaksim.gymnasium.fragment

import android.os.Bundle
import android.view.View
import android.content.Intent
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.fragment.app.Fragment

import ru.tgmaksim.gymnasium.BuildConfig
import ru.tgmaksim.gymnasium.ui.LoginActivity
import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.utilities.CacheManager
import ru.tgmaksim.gymnasium.databinding.FragmentSettingsBinding

/**
 * Fragment-страница с настройками приложения
 * @author Максим Дрючин (tgmaksim)
 * @see ru.tgmaksim.gymnasium.ui.MainActivity
 * */
class SettingsFragment : Fragment() {
    private lateinit var ui: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = FragmentSettingsBinding.inflate(inflater, container, false)

        // Установка Switch в нужное положение
        ui.settingsWebViewSwitch.isChecked = CacheManager.openWebView
        ui.settingsEANotificationsSwitch.isChecked = CacheManager.EANotifications

        ui.settingsWebViewSwitch.setOnCheckedChangeListener { _, isChecked ->
            CacheManager.openWebView = isChecked
        }
        ui.settingsEANotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            CacheManager.EANotifications = isChecked
            // TODO: проверить разрешения
        }

        CacheManager.versionStatus?.let {
            ui.updateApplication.visibility = View.VISIBLE
            ui.updateDescription.text = "${it.latestVersionString} (${it.latestVersionNumber})\n${it.updateLogs}"
        }

        // Нажатие на кнопку обновления
        ui.buttonUpdate.setOnClickListener {
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
}