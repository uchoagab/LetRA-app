package org.tensorflow.lite.examples.objectdetection.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.tensorflow.lite.examples.objectdetection.ObjectDetectorHelper
import org.tensorflow.lite.examples.objectdetection.R
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

        setupModelSwitch()
        setupClearDictionaryButton()
    }

    private fun setupModelSwitch() {
        val currentModel = SettingsManager.loadModelChoice(requireContext())
        binding.switchModelSelector.isChecked = (currentModel == ObjectDetectorHelper.MODEL_YOLO)

        // Aplica as cores do protótipo ao Switch
        val states = arrayOf(
            intArrayOf(-android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_checked)
        )
        val thumbColors = intArrayOf(
            ContextCompat.getColor(requireContext(), android.R.color.darker_gray),
            ContextCompat.getColor(requireContext(), R.color.cor_botao_principal)
        )
        val trackColors = intArrayOf(
            ContextCompat.getColor(requireContext(), android.R.color.white),
            ContextCompat.getColor(requireContext(), R.color.cor_subtitulo)
        )
        binding.switchModelSelector.thumbTintList = ColorStateList(states, thumbColors)
        binding.switchModelSelector.trackTintList = ColorStateList(states, trackColors)

        binding.switchModelSelector.setOnCheckedChangeListener { _, isChecked ->
            val newModel = if (isChecked) {
                ObjectDetectorHelper.MODEL_YOLO
            } else {
                ObjectDetectorHelper.MODEL_EFFICIENTDETV0
            }
            SettingsManager.saveModelChoice(requireContext(), newModel)
        }
    }

    private fun setupClearDictionaryButton() {
        binding.buttonClearDictionary.setOnClickListener {
            // Mostra um diálogo de confirmação antes de apagar
            AlertDialog.Builder(requireContext())
                .setTitle("Apagar Dicionário")
                .setMessage("Tem a certeza de que quer apagar todas as palavras e imagens salvas? Esta ação não pode ser desfeita.")
                .setPositiveButton("Apagar") { _, _ ->
                    SettingsManager.clearDictionary(requireContext())
                    Toast.makeText(context, "Dicionário apagado.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
