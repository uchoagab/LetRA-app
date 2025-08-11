package org.tensorflow.lite.examples.objectdetection.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.tensorflow.lite.examples.objectdetection.databinding.FragmentInitialMenuBinding

class InitialMenuFragment : Fragment() {

    private var _binding: FragmentInitialMenuBinding? = null
    private val binding get() = _binding!!

    // pede permissões ao android
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                navigateToCamera()
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

        binding.buttonStartDetection.setOnClickListener {
            checkCameraPermissionAndNavigate()
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

    private fun checkCameraPermissionAndNavigate() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                navigateToCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun navigateToCamera() {
        findNavController().navigate(
            InitialMenuFragmentDirections.actionInitialMenuFragmentToCameraFragment()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
