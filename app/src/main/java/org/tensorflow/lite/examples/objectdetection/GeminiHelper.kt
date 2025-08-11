package org.tensorflow.lite.examples.objectdetection

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.QuotaExceededException
import kotlinx.coroutines.delay
import org.json.JSONObject

data class ConteudoEducacional(val palavraTraduzida: String, val silabas: String, val frases: List<String>)

sealed class GeminiResult {
    data class Success(val conteudo: ConteudoEducacional) : GeminiResult()
    data class Error(val message: String, val isQuotaError: Boolean = false) : GeminiResult()
}

private val dicionarioMock = mapOf(
    "chair" to ConteudoEducacional(
        palavraTraduzida = "Cadeira",
        silabas = "Ca-dei-ra",
        frases = listOf(
            "A cadeira da sala de estar é muito confortável.",
            "Por favor, puxe uma cadeira e junte-se a nós para o jantar.",
            "Ele comprou uma cadeira melhor para estudar."
        )
    ),
    "person" to ConteudoEducacional(
        palavraTraduzida = "Pessoa",
        silabas = "Pes-so-a",
        frases = listOf(
            "Aquela pessoa parece simpática.",
            "Cada pessoa tem uma história única.",
            "Uma pessoa educada sempre diz 'obrigado'."
        )
    ),
    "bottle" to ConteudoEducacional(
        palavraTraduzida = "Garrafa",
        silabas = "Gar-ra-fa",
        frases = listOf(
            "Coloque o mouse na mesa.",
            "Quero aprender a usar o mouse.",
            "Esse mouse é confortável."
        )
    ),
    "mouse" to ConteudoEducacional(
        palavraTraduzida = "Mouse",
        silabas = "Mou-se",
        frases = listOf(
            "Encha a sua garrafa com água antes de sair.",
            "A garrafa de vidro é reciclável.",
            "Ele deixou a garrafa em cima da mesa."
        )
    )

)


/**
 * Chama a API do Gemini ou usa o dicionário local para gerar conteúdo.
 */
suspend fun gerarConteudoEducacional(
    apiKey: String,
    nomeDoObjetoEmIngles: String
): GeminiResult {
    if (dicionarioMock.containsKey(nomeDoObjetoEmIngles)) {
        Log.d("GeminiApp", "Objeto encontrado no dicionário local: $nomeDoObjetoEmIngles")
        delay(500)
        return GeminiResult.Success(dicionarioMock.getValue(nomeDoObjetoEmIngles))
    }

    Log.d("GeminiApp", "Objeto não encontrado no dicionário. Chamando a API do Gemini...")
    // Código do Gemini removido
    return GeminiResult.Error("Objeto não encontrado no dicionário de simulação.")
}
