package org.tensorflow.lite.examples.objectdetection

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import org.tensorflow.lite.examples.objectdetection.detectors.ObjectDetection
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.examples.objectdetection.detectors.ObjectDetector
import org.tensorflow.lite.examples.objectdetection.detectors.TaskVisionDetector
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.task.vision.detector.ObjectDetector.ObjectDetectorOptions

class ObjectDetectorHelper(
    var threshold: Float = 0.5f,
    var numThreads: Int = 2,
    var maxResults: Int = 3,
    var currentDelegate: Int = DELEGATE_CPU, // Padrão para CPU para máxima compatibilidade
    var currentModel: Int = MODEL_EFFICIENTDETV0, // Padrão para o novo modelo
    val context: Context,
    val objectDetectorListener: DetectorListener?
) {

    private var objectDetector: ObjectDetector? = null

    init {
        setupObjectDetector()
    }

    fun clearObjectDetector() {
        objectDetector = null
    }

    fun setupObjectDetector() {
        try {
            // ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼
            // CORREÇÃO: A lógica agora usa o TaskVisionDetector, que é compatível
            // com os modelos MobileNet e EfficientDet.
            // ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼
            val optionsBuilder =
                ObjectDetectorOptions.builder()
                    .setScoreThreshold(threshold)
                    .setMaxResults(maxResults)

            val baseOptionsBuilder = BaseOptions.builder().setNumThreads(numThreads)

            when (currentDelegate) {
                DELEGATE_CPU -> { /* Padrão */ }
                DELEGATE_GPU -> { baseOptionsBuilder.useGpu() }
                DELEGATE_NNAPI -> { baseOptionsBuilder.useNnapi() }
            }

            optionsBuilder.setBaseOptions(baseOptionsBuilder.build())
            val options = optionsBuilder.build()

            // O TaskVisionDetector irá carregar o modelo correto com base no 'currentModel'
            objectDetector = TaskVisionDetector(
                options,
                currentModel,
                context
            )

        } catch (e: Exception) {
            objectDetectorListener?.onError(e.toString())
        }
    }

    fun detect(image: Bitmap, imageRotation: Int) {
        if (objectDetector == null) {
            setupObjectDetector()
        }

        val imageProcessor = ImageProcessor.Builder()
            .add(Rot90Op(-imageRotation / 90))
            .build()

        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(image))
        var inferenceTime = SystemClock.uptimeMillis()
        val results = objectDetector?.detect(tensorImage, imageRotation)
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime

        if (results != null) {
            objectDetectorListener?.onResults(
                results.detections,
                inferenceTime,
                results.image.height,
                results.image.width
            )
        }
    }

    interface DetectorListener {
        fun onError(error: String)
        fun onResults(
            results: List<ObjectDetection>,
            inferenceTime: Long,
            imageHeight: Int,
            imageWidth: Int
        )
    }

    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DELEGATE_NNAPI = 2
        const val MODEL_MOBILENETV1 = 0
        const val MODEL_EFFICIENTDETV0 = 1 // Este é o nosso novo modelo
        const val MODEL_EFFICIENTDETV1 = 2
        const val MODEL_EFFICIENTDETV2 = 3
        const val MODEL_YOLO = 4
    }
}
