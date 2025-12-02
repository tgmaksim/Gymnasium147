package ru.tgmaksim.gymnasium.fragment

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.fragment.app.Fragment

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

        ui.settingsWebViewSwitch.setOnCheckedChangeListener { _, isChecked ->
            CacheManager.openWebView = isChecked
        }

        return ui.root
    }
}