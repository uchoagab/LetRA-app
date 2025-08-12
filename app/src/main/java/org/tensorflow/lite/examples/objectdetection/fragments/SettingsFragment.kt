package org.tensorflow.lite.examples.objectdetection.fragments

import android.content.res.ColorStateList
import android.graphics.Typeface
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
        // Carrega a configuração atual
        val currentModel = SettingsManager.loadModelChoice(requireContext())
        binding.switchModelSelector.isChecked = (currentModel == ObjectDetectorHelper.MODEL_YOLO)

        // Atualiza a aparência dos textos com base na escolha inicial
        updateModelLabels(binding.switchModelSelector.isChecked)

        // Salva a nova configuração e atualiza a aparência quando o botão é alterado
        binding.switchModelSelector.setOnCheckedChangeListener { _, isChecked ->
            val newModel = if (isChecked) {
                ObjectDetectorHelper.MODEL_YOLO
            } else {
                ObjectDetectorHelper.MODEL_EFFICIENTDETV0
            }
            SettingsManager.saveModelChoice(requireContext(), newModel)
            updateModelLabels(isChecked)
        }
    }

    // Função de ajuda para alterar a cor e o estilo do texto
    private fun updateModelLabels(isYoloSelected: Boolean) {
        if (isYoloSelected) {
            // Se YOLO estiver selecionado, realça o texto "YOLO"
            binding.labelYolo.setTextColor(ContextCompat.getColor(requireContext(), R.color.cor_titulo))
            binding.labelYolo.setTypeface(null, Typeface.BOLD)
            binding.labelEfficientdet.setTextColor(ContextCompat.getColor(requireContext(), R.color.cor_subtitulo))
            binding.labelEfficientdet.setTypeface(null, Typeface.NORMAL)
        } else {
            // Se EfficientDet estiver selecionado, realça o texto "EfficientDet"
            binding.labelEfficientdet.setTextColor(ContextCompat.getColor(requireContext(), R.color.cor_titulo))
            binding.labelEfficientdet.setTypeface(null, Typeface.BOLD)
            binding.labelYolo.setTextColor(ContextCompat.getColor(requireContext(), R.color.cor_subtitulo))
            binding.labelYolo.setTypeface(null, Typeface.NORMAL)
        }
    }

    private fun setupClearDictionaryButton() {
        binding.buttonClearDictionary.setOnClickListener {
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
