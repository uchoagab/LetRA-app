package org.tensorflow.lite.examples.objectdetection.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.tensorflow.lite.examples.objectdetection.databinding.FragmentInitialMenuBinding

class InitialMenuFragment : Fragment() {

    private var _binding: FragmentInitialMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInitialMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonStartDetection.setOnClickListener {
            findNavController().navigate(
                InitialMenuFragmentDirections.actionInitialMenuFragmentToCameraFragment()
            )
        }

        binding.buttonDictionary.setOnClickListener {
            Toast.makeText(context, "Dicionário a ser implementado!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
