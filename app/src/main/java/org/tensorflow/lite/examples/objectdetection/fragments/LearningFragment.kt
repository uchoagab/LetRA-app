package org.tensorflow.lite.examples.objectdetection.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.objectdetection.BuildConfig
import org.tensorflow.lite.examples.objectdetection.GeminiResult
import org.tensorflow.lite.examples.objectdetection.databinding.FragmentLearningBinding
import org.tensorflow.lite.examples.objectdetection.gerarConteudoEducacional

class LearningFragment : Fragment() {

    private var _binding: FragmentLearningBinding? = null
    private val binding get() = _binding!!
    private val args: LearningFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val palavraEmIngles = args.palavra
        binding.toolbar.title = palavraEmIngles.replaceFirstChar { it.uppercase() }
        binding.imageViewSnapshot.setImageURI(Uri.parse(args.imageUri))
        binding.textViewPalavra.text = "Analisando..." // Placeholder

        callGeminiApi(palavraEmIngles)

        binding.buttonContexto.setOnClickListener {
            Toast.makeText(context, "Função a ser implementada!", Toast.LENGTH_SHORT).show()
        }
        binding.buttonSalvar.setOnClickListener {
            Toast.makeText(context, "Função a ser implementada!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun callGeminiApi(palavraEmIngles: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.textViewSilabas.text = ""

        viewLifecycleOwner.lifecycleScope.launch {
            val apiKey = BuildConfig.GEMINI_API_KEY
            val resultado = gerarConteudoEducacional(apiKey, palavraEmIngles)

            binding.progressBar.visibility = View.GONE

            // Lida com o resultado usando a nossa nova classe selada
            when (resultado) {
                is GeminiResult.Success -> {
                    val conteudo = resultado.conteudo
                    // A palavra traduzida pode ser extraída das sílabas
                    val palavraTraduzida = conteudo.silabas.replace("-", "")
                    binding.textViewPalavra.text = palavraTraduzida.uppercase()
                    binding.toolbar.title = palavraTraduzida.replaceFirstChar { it.uppercase() }
                    binding.textViewSilabas.text = conteudo.silabas
                }
                is GeminiResult.Error -> {
                    binding.textViewPalavra.text = palavraEmIngles.uppercase()
                    if (resultado.isQuotaError) {
                        binding.textViewSilabas.text = "Quota da API excedida."
                    } else {
                        binding.textViewSilabas.text = "Erro ao analisar."
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
