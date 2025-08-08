package org.tensorflow.lite.examples.objectdetection.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.objectdetection.BuildConfig
import org.tensorflow.lite.examples.objectdetection.ConteudoEducacional
import org.tensorflow.lite.examples.objectdetection.GeminiResult
import org.tensorflow.lite.examples.objectdetection.databinding.FragmentLearningBinding
import org.tensorflow.lite.examples.objectdetection.gerarConteudoEducacional

class LearningFragment : Fragment() {

    private var _binding: FragmentLearningBinding? = null
    private val binding get() = _binding!!
    private val args: LearningFragmentArgs by navArgs()

    private var conteudoAtual: ConteudoEducacional? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageViewSnapshot.setImageURI(Uri.parse(args.imageUri))
        binding.textViewPalavra.text = "Analisando..."
        binding.buttonContexto.isVisible = false

        callGeminiApi(args.palavra)

        binding.buttonContexto.setOnClickListener {
            conteudoAtual?.let { conteudo ->
                findNavController().navigate(
                    LearningFragmentDirections.actionLearningFragmentToContextFragment(
                        conteudo.palavraTraduzida,
                        conteudo.silabas,
                        args.imageUri,
                        conteudo.frases.toTypedArray()
                    )
                )
            }
        }

        binding.buttonVoltar.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun callGeminiApi(palavraEmIngles: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.textViewSilabas.text = ""

        viewLifecycleOwner.lifecycleScope.launch {
            val apiKey = BuildConfig.GEMINI_API_KEY
            val resultado = gerarConteudoEducacional(apiKey, palavraEmIngles)

            binding.progressBar.visibility = View.GONE

            when (resultado) {
                is GeminiResult.Success -> {
                    conteudoAtual = resultado.conteudo
                    binding.textViewPalavra.text = conteudoAtual!!.palavraTraduzida.uppercase()
                    binding.textViewSilabas.text = conteudoAtual!!.silabas
                    binding.buttonContexto.isVisible = true // Mostra o botão
                }
                is GeminiResult.Error -> {
                    binding.textViewPalavra.text = palavraEmIngles.uppercase()
                    binding.textViewSilabas.text = if (resultado.isQuotaError) "Quota excedida." else "Erro ao analisar."
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
