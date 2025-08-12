package org.tensorflow.lite.examples.objectdetection.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.tensorflow.lite.examples.objectdetection.databinding.FragmentInitialMenuBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class InitialMenuFragment : Fragment() {

    private val TAG = "InitialMenuFragment"
    private var _binding: FragmentInitialMenuBinding? = null
    private val binding get() = _binding!!

    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(context, "Permissão de câmara negada.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInitialMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.buttonStartDetection.setOnClickListener {
            findNavController().navigate(
                InitialMenuFragmentDirections.actionInitialMenuFragmentToCameraFragment()
            )
        }

        binding.buttonDictionary.setOnClickListener {
            findNavController().navigate(
                InitialMenuFragmentDirections.actionInitialMenuFragmentToDictionaryFragment()
            )
        }

        binding.buttonSettings.setOnClickListener {
            findNavController().navigate(
                InitialMenuFragmentDirections.actionInitialMenuFragmentToSettingsFragment()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // inicia a câmera
        checkCameraPermissionAndStartCamera()
    }

    override fun onPause() {
        super.onPause()
        // para a câmera quando invisível
        cameraProvider?.unbindAll()
    }

    private fun checkCameraPermissionAndStartCamera() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraPreview()
        }, ContextCompat.getMainExecutor(requireContext()))
    }



    private fun bindCameraPreview() {
        val cameraProvider = cameraProvider ?: return

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this as androidx.lifecycle.LifecycleOwner, cameraSelector, preview)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }
}
