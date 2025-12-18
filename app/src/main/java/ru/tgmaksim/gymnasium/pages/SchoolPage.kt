package ru.tgmaksim.gymnasium.pages

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.fragment.app.Fragment

import ru.tgmaksim.gymnasium.utilities.Utilities
import ru.tgmaksim.gymnasium.databinding.SchoolPageBinding

/**
 * Страница с мероприятиями и другими событиями и объявлениями школы
 * @author Максим Дрючин (tgmaksim)
 * @see ru.tgmaksim.gymnasium.ui.MainActivity
 * */
class SchoolPage : Fragment() {
    private lateinit var ui: SchoolPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (::ui.isInitialized)
            return ui.root

        ui = SchoolPageBinding.inflate(inflater, container, false)

        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utilities.log("SchoolPage загружена")
    }
}