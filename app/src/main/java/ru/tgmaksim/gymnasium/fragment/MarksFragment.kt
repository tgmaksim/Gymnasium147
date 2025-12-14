package ru.tgmaksim.gymnasium.fragment

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.fragment.app.Fragment

import ru.tgmaksim.gymnasium.databinding.FragmentMarksBinding

/**
 * Fragment-страница с оценками пользователя
 * @author Максим Дрючин (tgmaksim)
 * @see ru.tgmaksim.gymnasium.ui.MainActivity
 * */
class MarksFragment : Fragment() {
    private lateinit var ui: FragmentMarksBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui = FragmentMarksBinding.inflate(inflater, container, false)

        return ui.root
    }
}