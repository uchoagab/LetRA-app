package org.tensorflow.lite.examples.objectdetection.detectors

import android.content.Context
import android.util.Log
import org.tensorflow.lite.examples.objectdetection.ObjectDetectorHelper
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector as TFLiteObjectDetector

/**
 * Detector que usa a Task Library do TensorFlow Lite.
 */
class TaskVisionDetector(
    private val options: TFLiteObjectDetector.ObjectDetectorOptions,
    private val model: Int,
    private val context: Context
) : ObjectDetector {

    private var detector: TFLiteObjectDetector? = null

    init {
        setupDetector()
    }

    private fun setupDetector() {
        try {
            val modelName = when (model) {
                ObjectDetectorHelper.MODEL_EFFICIENTDETV0 -> "efficientdet-lite0.tflite"
                ObjectDetectorHelper.MODEL_EFFICIENTDETV1 -> "efficientdet-lite1.tflite"
                ObjectDetectorHelper.MODEL_EFFICIENTDETV2 -> "efficientdet-lite2.tflite"
                ObjectDetectorHelper.MODEL_MOBILENETV1 -> "mobilenetv1.tflite"
                else -> {
                    Log.e("TaskVisionDetector", "Modelo não suportado: $model")
                    "efficientdet-lite0.tflite" // Padrão
                }
            }
            detector = TFLiteObjectDetector.createFromFileAndOptions(context, modelName, options)
        } catch (e: Exception) {
            Log.e("TaskVisionDetector", "Falha ao inicializar o detector: ${e.message}")
        }
    }

    override fun detect(
        tensorImage: TensorImage,
        imageRotation: Int
    ): DetectionResult {
        if (detector == null) {
            return DetectionResult(tensorImage.bitmap, emptyList())
        }

        val results: List<Detection> = detector?.detect(tensorImage) ?: emptyList()

        val objectDetections = results.map { detection ->
            val tfliteCategory = detection.categories.first()
            ObjectDetection(
                boundingBox = detection.boundingBox,
                category = Category(
                    label = tfliteCategory.label,
                    confidence = tfliteCategory.score
                )
            )
        }

        return DetectionResult(
            image = tensorImage.bitmap,
            detections = objectDetections
        )
    }

    fun close() {
        detector?.close()
        detector = null
    }
}
