package org.tensorflow.lite.examples.objectdetection.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.tensorflow.lite.examples.objectdetection.ObjectDetectorHelper
import org.tensorflow.lite.examples.objectdetection.SettingsManager
import org.tensorflow.lite.examples.objectdetection.databinding.FragmentCameraBinding
import org.tensorflow.lite.examples.objectdetection.detectors.ObjectDetection
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment(), ObjectDetectorHelper.DetectorListener {

    private val TAG = "CameraFragment"
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    private lateinit var bitmapBuffer: Bitmap
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService

    private var latestDetectedObject: ObjectDetection? = null
    private var imageRotationDegrees: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ATUALIZAÇÃO: Carrega o modelo escolhido pelo utilizador nas configurações
        val selectedModel = SettingsManager.loadModelChoice(requireContext())

        objectDetectorHelper = ObjectDetectorHelper(
            context = requireContext(),
            objectDetectorListener = this,
            currentModel = selectedModel, // Usa o modelo guardado
            currentDelegate = ObjectDetectorHelper.DELEGATE_CPU
        )
        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.viewFinder.post { setUpCamera() }

        binding.buttonAprender.setOnClickListener {
            latestDetectedObject?.let { detection ->
                val sourceBitmap = bitmapBuffer
                val croppedBitmap = cropAndRotateBitmap(sourceBitmap, detection.boundingBox, imageRotationDegrees)

                if (croppedBitmap != null) {
                    val imageUri = saveBitmapToFile(requireContext(), croppedBitmap)
                    if (imageUri != null) {
                        findNavController().navigate(
                            CameraFragmentDirections.actionCameraFragmentToLearningFragment(
                                detection.category.label,
                                imageUri.toString()
                            )
                        )
                    } else {
                        Toast.makeText(context, "Erro ao salvar imagem", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Erro ao recortar imagem", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonVoltar.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }

    private fun cropAndRotateBitmap(source: Bitmap, box: RectF, rotationDegrees: Int): Bitmap? {
        try {
            val left = box.left.toInt()
            val top = box.top.toInt()
            val width = box.width().toInt()
            val height = box.height().toInt()

            if (left < 0 || top < 0 || left + width > source.width || top + height > source.height) {
                Log.e(TAG, "Coordenadas de recorte fora dos limites da imagem de origem.")
                return null
            }
            val cropped = Bitmap.createBitmap(source, left, top, width, height)

            val matrix = Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            return Bitmap.createBitmap(cropped, 0, 0, cropped.width, cropped.height, matrix, true)

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao recortar e rodar bitmap: ${e.message}")
            return null
        }
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap): Uri? {
        val file = File(context.cacheDir, "snapshot_${System.currentTimeMillis()}.png")
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onResults(results: List<ObjectDetection>, inferenceTime: Long, imageHeight: Int, imageWidth: Int) {
        activity?.runOnUiThread {
            if (_binding == null) return@runOnUiThread

            binding.overlay.setResults(results, imageHeight, imageWidth)
            latestDetectedObject = results.firstOrNull()
            binding.buttonAprender.isEnabled = latestDetectedObject != null
        }
    }

    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return
        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        }
        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { image ->
                    if (!::bitmapBuffer.isInitialized) {
                        bitmapBuffer = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                    }
                    image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }

                    imageRotationDegrees = image.imageInfo.rotationDegrees
                    objectDetectorHelper.detect(bitmapBuffer, imageRotationDegrees)
                }
            }
        cameraProvider.unbindAll()
        try {
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }
}
