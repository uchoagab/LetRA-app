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
import org.tensorflow.lite.examples.objectdetection.databinding.FragmentCameraBinding
import org.tensorflow.lite.examples.objectdetection.detectors.ObjectDetection
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max

class CameraFragment : Fragment(), ObjectDetectorHelper.DetectorListener {

    private val TAG = "CameraFragment"
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    private lateinit var bitmapBuffer: Bitmap
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService

    private var latestDetectedObject: ObjectDetection? = null
    private var inputImageWidth: Int = 0
    private var inputImageHeight: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        objectDetectorHelper = ObjectDetectorHelper(
            context = requireContext(),
            objectDetectorListener = this,
            currentModel = ObjectDetectorHelper.MODEL_YOLO
        )
        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.viewFinder.post { setUpCamera() }

        binding.buttonAprender.setOnClickListener {
            latestDetectedObject?.let { detection ->
                // Usamos o bitmap da PreviewView, que é o que o utilizador vê
                val snapshot = binding.viewFinder.bitmap ?: return@setOnClickListener
                val croppedBitmap = cropBitmap(snapshot, detection.boundingBox)

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

    // ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼
    // FUNÇÃO DE RECORTE FINAL E CORRIGIDA (VERSÃO 3)
    // Esta versão remove a lógica de centralização incorreta.
    // ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼
    private fun cropBitmap(viewBitmap: Bitmap, box: RectF): Bitmap? {
        try {
            val viewWidth = viewBitmap.width
            val viewHeight = viewBitmap.height

            val isModelLandscape = inputImageWidth > inputImageHeight
            val isViewPortrait = viewWidth < viewHeight

            val modelWidth: Float
            val modelHeight: Float

            if (isModelLandscape && isViewPortrait) {
                modelWidth = inputImageHeight.toFloat()
                modelHeight = inputImageWidth.toFloat()
            } else {
                modelWidth = inputImageWidth.toFloat()
                modelHeight = inputImageHeight.toFloat()
            }

            val matrix = Matrix()
            val scaleFactor = max(viewWidth / modelWidth, viewHeight / modelHeight)
            matrix.postScale(scaleFactor, scaleFactor)

            // A PreviewView com scaleType="fillStart" não centra a imagem,
            // por isso o deslocamento (dx, dy) deve ser zero.
            // Esta era a causa do erro.

            val mappedBox = RectF()
            matrix.mapRect(mappedBox, box)

            val left = mappedBox.left.toInt()
            val top = mappedBox.top.toInt()
            val width = mappedBox.width().toInt()
            val height = mappedBox.height().toInt()

            if (left < 0 || top < 0 || left + width > viewWidth || top + height > viewHeight) {
                Log.e(TAG, "Coordenadas de recorte fora dos limites: L:$left, T:$top, W:$width, H:$height | View W:$viewWidth, H:$viewHeight")
                return null
            }

            return Bitmap.createBitmap(viewBitmap, left, top, width, height)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao recortar bitmap: ${e.message}")
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

            inputImageWidth = imageWidth
            inputImageHeight = imageHeight
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

    // Funções da câmara (setUpCamera, bindCameraUseCases, etc.)
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
                    objectDetectorHelper.detect(bitmapBuffer, image.imageInfo.rotationDegrees)
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
