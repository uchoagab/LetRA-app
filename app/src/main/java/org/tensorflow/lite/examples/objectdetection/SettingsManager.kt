package org.tensorflow.lite.examples.objectdetection

import android.content.Context

/**
 * Classe de ajuda para gerir as configurações do aplicativo.
 */
object SettingsManager {

    private const val PREFS_NAME = "app_settings"
    private const val KEY_MODEL_CHOICE = "model_choice"

    /**
     * Salva a escolha do modelo (EfficientDet ou YOLO).
     * @param context O contexto da aplicação.
     * @param model A constante do modelo (ex: ObjectDetectorHelper.MODEL_YOLO).
     */
    fun saveModelChoice(context: Context, model: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_MODEL_CHOICE, model).apply()
    }

    /**
     * Carrega a escolha do modelo. Se nenhuma for encontrada, devolve EfficientDet como padrão.
     * @param context O contexto da aplicação.
     * @return A constante do modelo escolhido.
     */
    fun loadModelChoice(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // O valor padrão é MODEL_EFFICIENTDETV0, como você pediu.
        return prefs.getInt(KEY_MODEL_CHOICE, ObjectDetectorHelper.MODEL_EFFICIENTDETV0)
    }
}