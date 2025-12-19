package ru.tgmaksim.gymnasium.pages

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.fragment.app.Fragment

import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.utilities.CacheManager
import ru.tgmaksim.gymnasium.databinding.MarksPageBinding

/**
 * Страница с оценками пользователя
 * @author Максим Дрючин (tgmaksim)
 * @see ru.tgmaksim.gymnasium.ui.MainActivity
 * */
class MarksPage : Fragment() {
    private lateinit var ui: MarksPageBinding
    private var isDarkTheme: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (::ui.isInitialized && CacheManager.isDarkTheme == isDarkTheme)
            return ui.root

        ui = MarksPageBinding.inflate(inflater, container, false)
        isDarkTheme = CacheManager.isDarkTheme

        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utilities.log("MarksPage загружена")
    }
}