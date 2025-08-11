package org.tensorflow.lite.examples.objectdetection.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.tensorflow.lite.examples.objectdetection.ObjectDetectorHelper
import org.tensorflow.lite.examples.objectdetection.SettingsManager
import org.tensorflow.lite.examples.objectdetection.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        // Carrega a configuração atual e define o estado do botão
        val currentModel = SettingsManager.loadModelChoice(requireContext())
        binding.switchModelSelector.isChecked = (currentModel == ObjectDetectorHelper.MODEL_YOLO)

        // Salva a nova configuração quando o botão é alterado
        binding.switchModelSelector.setOnCheckedChangeListener { _, isChecked ->
            val newModel = if (isChecked) {
                ObjectDetectorHelper.MODEL_YOLO
            } else {
                ObjectDetectorHelper.MODEL_EFFICIENTDETV0
            }
            SettingsManager.saveModelChoice(requireContext(), newModel)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}