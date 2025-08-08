package org.tensorflow.lite.examples.objectdetection.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.tensorflow.lite.examples.objectdetection.R
import org.tensorflow.lite.examples.objectdetection.databinding.FragmentContextBinding

class ContextFragment : Fragment() {

    private var _binding: FragmentContextBinding? = null
    private val binding get() = _binding!!
    private val args: ContextFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageViewSnapshot.setImageURI(Uri.parse(args.imageUri))
        binding.textViewPalavra.text = args.palavra.uppercase()
        binding.textViewSilabas.text = args.silabas

        args.frases.forEach { frase ->
            val textView = TextView(requireContext()).apply {
                text = frase
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.darker_gray, null))
                setPadding(0, 8, 0, 8)
            }
            binding.linearLayoutFrases.addView(textView)
        }

        binding.buttonVoltarDicionario.setOnClickListener {
            findNavController().popBackStack(R.id.cameraFragment, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
