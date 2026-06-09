package com.taskmate.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.taskmate.app.R
import com.taskmate.app.databinding.FragmentSettingsBinding
import com.taskmate.app.util.Constants
import com.taskmate.app.util.LocaleHelper

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Тековен јазик
        when (LocaleHelper.getPersistedLanguage(requireContext())) {
            Constants.LANG_EN -> binding.radioEnglish.isChecked = true
            else -> binding.radioMacedonian.isChecked = true
        }

        binding.radioGroupLanguage.setOnCheckedChangeListener { _, checkedId ->
            val lang = if (checkedId == R.id.radioEnglish) Constants.LANG_EN else Constants.LANG_MK
            if (lang != LocaleHelper.getPersistedLanguage(requireContext())) {
                LocaleHelper.setLanguage(requireContext(), lang)
                Firebase.analytics.logEvent(Constants.EVENT_LANGUAGE_CHANGED) {
                    param("language", lang)
                }
                // Повторно креирај ја Activity за да се примени новиот јазик
                requireActivity().recreate()
            }
        }

        // Тема (светла/темна)
        binding.switchDarkMode.isChecked =
            AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.tvVersion.text = getString(R.string.version_label, "1.0")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}