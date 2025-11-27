package ru.tgmaksim.gymnasium.fragment

import android.os.Bundle
import android.view.View
import android.view.ViewGroup

import ru.tgmaksim.gymnasium.R
import android.view.LayoutInflater
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
}
