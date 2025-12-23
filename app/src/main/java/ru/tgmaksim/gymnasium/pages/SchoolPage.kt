package ru.tgmaksim.gymnasium.pages

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.fragment.app.Fragment

import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.databinding.SchoolPageBinding
import ru.tgmaksim.gymnasium.utilities.CacheManager

/**
 * Страница с мероприятиями и другими событиями и объявлениями школы
 * @author Максим Дрючин (tgmaksim)
 * @see ru.tgmaksim.gymnasium.ui.MainActivity
 * */
class SchoolPage : Fragment() {
    private lateinit var ui: SchoolPageBinding
    private var isDarkTheme: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (::ui.isInitialized && CacheManager.isDarkTheme == isDarkTheme)
            return ui.root

        ui = SchoolPageBinding.inflate(inflater, container, false)
        isDarkTheme = CacheManager.isDarkTheme

        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utilities.log("SchoolPage загружена", tag="load") {
            param("place", "SchoolPage")
        }
    }
}