package com.example.playlistmaker.settings.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.playlistmaker.databinding.FragmentSettingsBinding
import com.example.playlistmaker.settings.model.ThemeSettings
import com.example.playlistmaker.settings.view_model.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<SettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.switchtheme.isChecked = viewModel.getThemeSettings().isDarkTheme

        binding.switchtheme.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateThemeSettings(ThemeSettings(isChecked))
        }

        viewModel.themeChanged.observe(viewLifecycleOwner) { _ -> }


        binding.shareBtn.setOnClickListener {
            viewModel.shareApp()
        }

        binding.supportBtn.setOnClickListener {
            viewModel.openSupport()
        }

        binding.policyBtn.setOnClickListener {
            viewModel.openTerms()
        }

        viewModel.actionCommand.observe(viewLifecycleOwner) { intent ->
            intent?.let { startActivity(it) }
            viewModel.clearActionCommand()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

