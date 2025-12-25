package com.example.gallerycleaner

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.example.gallerycleaner.ui.GalleryScreen
import com.example.gallerycleaner.ui.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val deleteRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Photo deleted successfully", Toast.LENGTH_SHORT).show()
            viewModel.onPermissionResult(isGranted = true)
        } else {
            Toast.makeText(this, "Delete cancelled", Toast.LENGTH_SHORT).show()
            viewModel.onPermissionResult(isGranted = false)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val api33Granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_IMAGES] == true
        } else false
        
        val legacyGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true

        if (api33Granted || legacyGranted) {
            viewModel.loadPhotos()
        } else {
            Toast.makeText(this, "Permission denied. Cannot load photos.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkPermissions()

        setContent {
            MaterialTheme {
                val intentSender by viewModel.intentSenderEvent.collectAsState()

                LaunchedEffect(intentSender) {
                    intentSender?.let { sender ->
                        launchDeleteRequest(sender)
                    }
                }

                GalleryScreen(viewModel = viewModel)
            }
        }
    }

    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
             if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q && 
                 ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                 // Android 10 ve altı için Write gerekebilir (gerçi MediaStore delete için Android 10'da exception handle ediyoruz)
                 permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
             }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun launchDeleteRequest(intentSender: IntentSender) {
        val request = IntentSenderRequest.Builder(intentSender).build()
        deleteRequestLauncher.launch(request)
    }
}
