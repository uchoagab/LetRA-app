package org.tensorflow.lite.examples.objectdetection

import android.content.Context
import java.io.File

object SettingsManager {

    private const val PREFS_NAME = "app_settings"
    private const val KEY_MODEL_CHOICE = "model_choice"

    fun saveModelChoice(context: Context, model: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_MODEL_CHOICE, model).apply()
    }

    fun loadModelChoice(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_MODEL_CHOICE, ObjectDetectorHelper.MODEL_EFFICIENTDETV0)
    }

    // apaga dicionário
    fun clearDictionary(context: Context) {
        val metadataFile = File(context.filesDir, "dictionary_metadata.json")
        if (metadataFile.exists()) {
            metadataFile.delete()
        }

        val files = context.filesDir.listFiles()
        files?.filter { it.isFile && it.name.endsWith(".png") }?.forEach {
            it.delete()
        }
    }
}