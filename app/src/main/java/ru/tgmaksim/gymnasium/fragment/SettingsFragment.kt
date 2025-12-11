package ru.tgmaksim.gymnasium.fragment

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.fragment.app.Fragment

import ru.tgmaksim.gymnasium.BuildConfig
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

        // Установка Switch в нужное положение
        ui.settingsWebViewSwitch.isChecked = CacheManager.openWebView
        ui.settingsEANotificationsSwitch.isChecked = CacheManager.EANotifications

        ui.settingsWebViewSwitch.setOnCheckedChangeListener { _, isChecked ->
            CacheManager.openWebView = isChecked
        }
        ui.settingsEANotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            CacheManager.EANotifications = isChecked
        }

        CacheManager.versionStatus.updateLog?.let {
            ui.updateDescription.text = it
        }
        if (CacheManager.versionStatus.newLatestVersion)
            ui.updateApplication.visibility = View.VISIBLE
        else
            ui.updateApplication.visibility = View.GONE

        // Нажатие на кнопку обновления
        ui.buttonUpdate.setOnClickListener {
            Utilities.openUrl(requireContext(), BuildConfig.DOMAIN)
        }

        return ui.root
    }
}