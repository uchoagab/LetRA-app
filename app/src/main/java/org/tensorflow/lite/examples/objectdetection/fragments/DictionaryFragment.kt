package org.tensorflow.lite.examples.objectdetection.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.tensorflow.lite.examples.objectdetection.DictionaryManager
import org.tensorflow.lite.examples.objectdetection.DictionaryWord
import org.tensorflow.lite.examples.objectdetection.adapters.DictionaryAdapter
import org.tensorflow.lite.examples.objectdetection.databinding.FragmentDictionaryBinding
import java.io.File

class DictionaryFragment : Fragment() {

    private var _binding: FragmentDictionaryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDictionaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val savedWords = DictionaryManager.loadWords(requireContext())

        if (savedWords.isEmpty()) {
            binding.textViewEmpty.isVisible = true
            binding.recyclerViewDictionary.isVisible = false
        } else {
            binding.textViewEmpty.isVisible = false
            binding.recyclerViewDictionary.isVisible = true

            val adapter = DictionaryAdapter(savedWords) { word ->
                // Ação ao clicar num item do dicionário
                navigateToContext(word)
            }
            binding.recyclerViewDictionary.adapter = adapter
        }
    }

    private fun navigateToContext(word: DictionaryWord) {
        val imageUri = Uri.fromFile(File(requireContext().filesDir, word.imageFileName)).toString()
        findNavController().navigate(
            DictionaryFragmentDirections.actionDictionaryFragmentToContextFragment(
                word.palavra,
                word.silabas,
                imageUri,
                word.frases.toTypedArray()
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
