package org.tensorflow.lite.examples.objectdetection

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.QuotaExceededException
import kotlinx.coroutines.delay
import org.json.JSONObject

data class ConteudoEducacional(val frase: String, val silabas: String)

sealed class GeminiResult {
    data class Success(val conteudo: ConteudoEducacional) : GeminiResult()
    data class Error(val message: String, val isQuotaError: Boolean = false) : GeminiResult()
}

// DICIONÁRIO LOCAL (MOCK)
private val dicionarioMock = mapOf(
    "chair" to ConteudoEducacional(frase = "A cadeira serve para sentar.", silabas = "ca-dei-ra"),
    "person" to ConteudoEducacional(frase = "Esta é uma pessoa.", silabas = "pes-so-a"),
    "bottle" to ConteudoEducacional(frase = "A garrafa tá sem água.", silabas = "gar-ra-fa"),
    "tv" to ConteudoEducacional(frase = "Eu vejo desenhos na televisão.", silabas = "te-le-vi-são"),
    "mouse" to ConteudoEducacional(frase = "O mouse controla o computador.", silabas = "mou-se")
)


/**
 * Chama a API do Gemini ou usa o dicionário local para gerar conteúdo.
 */
suspend fun gerarConteudoEducacional(
    apiKey: String,
    nomeDoObjetoEmIngles: String
): GeminiResult {

    // Primeiro, verifica se a palavra está no nosso dicionário local
    if (dicionarioMock.containsKey(nomeDoObjetoEmIngles)) {
        Log.d("GeminiApp", "Objeto encontrado no dicionário local: $nomeDoObjetoEmIngles")
        // Simula um pequeno atraso de rede para parecer real
        delay(500)
        return GeminiResult.Success(dicionarioMock.getValue(nomeDoObjetoEmIngles))
    }

    // Se não estiver no dicionário, continua e chama a API real do Gemini
    Log.d("GeminiApp", "Objeto não encontrado no dicionário. Chamando a API do Gemini...")
    val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    val prompt = """
    Você é um assistente de alfabetização para adultos.
    O objeto detetado está em inglês: "$nomeDoObjetoEmIngles".
    Primeiro, traduza o nome do objeto para o português do Brasil.
    Depois, para a palavra em português, faça duas coisas:
    1. Crie uma frase curta, simples e positiva.
    2. Separe o nome do objeto em sílabas, usando hífens. Se a palavra for monossílaba ou um estrangeirismo como "mouse", não a separe.

    Sua resposta deve ser um objeto JSON válido com as chaves "frase" e "silabas".
    """.trimIndent()

    return try {
        val response = generativeModel.generateContent(prompt)
        val responseText = response.text

        if (responseText != null) {
            val cleanedJson = responseText.trim().removeSurrounding("```json", "```").trim()
            val jsonObject = org.json.JSONObject(cleanedJson)
            val frase = jsonObject.getString("frase")
            val silabas = jsonObject.getString("silabas")
            val conteudo = ConteudoEducacional(frase, silabas)
            GeminiResult.Success(conteudo)
        } else {
            GeminiResult.Error("A resposta do Gemini foi nula.")
        }
    } catch (e: QuotaExceededException) {
        Log.e("GeminiApp", "Quota da API excedida", e)
        GeminiResult.Error("Quota excedida", isQuotaError = true)
    } catch (e: Exception) {
        Log.e("GeminiApp", "Erro ao chamar ou analisar a API do Gemini: ${e.message}", e)
        GeminiResult.Error("Erro desconhecido: ${e.message}")
    }
}
