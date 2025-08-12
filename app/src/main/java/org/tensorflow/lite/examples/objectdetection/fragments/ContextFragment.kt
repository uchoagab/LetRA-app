package org.tensorflow.lite.examples.objectdetection.fragments

import android.graphics.Typeface
import android.graphics.text.LineBreaker
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
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
        binding.textViewSilabas.text = args.silabas.uppercase()

        args.frases.forEach { frase ->
            val textView = TextView(requireContext()).apply {
                text = highlightWordInSentence(frase, args.palavra)
                textSize = 20f
                setPadding(0, 12, 0, 12)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
                }
            }
            binding.linearLayoutFrases.addView(textView)
        }

        binding.buttonVoltarDicionario.setOnClickListener {
            // Este comando volta para a tela anterior, que pode ser a de Aprendizagem ou a do Dicionário
            findNavController().navigateUp()
        }
    }

    /**
     * função encontrar palavra na frase
     */
    private fun highlightWordInSentence(sentence: String, wordToHighlight: String): SpannableStringBuilder {
        val spannable = SpannableStringBuilder(sentence)
        val highlightColor = ContextCompat.getColor(requireContext(), R.color.cor_titulo)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.cor_subtitulo)

        spannable.setSpan(
            ForegroundColorSpan(defaultColor),
            0,
            sentence.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // encontra palavra
        val startIndex = sentence.indexOf(wordToHighlight, ignoreCase = true)
        if (startIndex != -1) {
            val endIndex = startIndex + wordToHighlight.length
            // aplica cor
            spannable.setSpan(
                ForegroundColorSpan(highlightColor),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
