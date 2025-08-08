package org.tensorflow.lite.examples.objectdetection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

data class DictionaryWord(
    val palavra: String,
    val silabas: String,
    val frases: List<String>,
    val imageFileName: String
)

/**
 * Classe para gerir o dicionário.
 */
object DictionaryManager {

    private const val METADATA_FILE = "dictionary_metadata.json"
    private val gson = Gson()

    fun loadWords(context: Context): MutableList<DictionaryWord> {
        val file = File(context.filesDir, METADATA_FILE)
        if (!file.exists()) {
            return mutableListOf()
        }
        val json = file.readText()
        val type = object : TypeToken<MutableList<DictionaryWord>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    private fun saveWordsList(context: Context, words: List<DictionaryWord>) {
        val file = File(context.filesDir, METADATA_FILE)
        val json = gson.toJson(words)
        file.writeText(json)
    }

    fun saveWord(
        context: Context,
        conteudo: ConteudoEducacional,
        imageUri: Uri
    ) {
        val words = loadWords(context)
        val palavra = conteudo.palavraTraduzida

        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val imageFileName = "${palavra.lowercase()}.png"
        saveBitmapToInternalStorage(context, bitmap, imageFileName)

        // verifica existência
        val existingWordIndex = words.indexOfFirst { it.palavra.equals(palavra, ignoreCase = true) }

        val newWord = DictionaryWord(
            palavra = palavra,
            silabas = conteudo.silabas,
            frases = conteudo.frases,
            imageFileName = imageFileName
        )

        if (existingWordIndex != -1) {
            words[existingWordIndex] = newWord
        } else {
            words.add(newWord)
        }

        saveWordsList(context, words)
    }

    fun loadBitmap(context: Context, fileName: String): Bitmap? {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, filename: String) {
        val file = File(context.filesDir, filename)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}