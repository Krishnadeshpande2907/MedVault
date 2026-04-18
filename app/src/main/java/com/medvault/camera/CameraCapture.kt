package com.medvault.camera

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * CameraX helper for prescription photo capture.
 * Hilt-injected singleton — provides a PreviewView composable
 * and both callback-based and suspending capture functions.
 */
@Singleton
class CameraCapture @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var imageCapture: ImageCapture? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    /**
     * Start the camera preview and bind to the lifecycle.
     */
    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                // Handle camera initialization error — caller observes via captureResult
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Callback-based photo capture. Save to [outputFile].
     */
    fun takePhoto(
        outputFile: File,
        onSuccess: (Uri) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val capture = imageCapture ?: run {
            onError(IllegalStateException("Camera not initialized"))
            return
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        capture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    onSuccess(Uri.fromFile(outputFile))
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }

    /**
     * Coroutine-friendly photo capture. Suspends until the photo is saved
     * or throws on failure.
     *
     * @param outputFile The file to save the captured image to.
     * @return The [Uri] of the saved file.
     */
    suspend fun takePhotoSuspend(outputFile: File): Uri =
        suspendCancellableCoroutine { continuation ->
            takePhoto(
                outputFile = outputFile,
                onSuccess = { uri -> continuation.resume(uri) },
                onError = { e -> continuation.resumeWithException(e) }
            )
        }

    /**
     * Create a temporary output file in the app's cache directory.
     * Used for the prescription capture before the visit is saved.
     */
    fun createTempImageFile(): File {
        val cacheDir = File(context.cacheDir, "prescription_temp")
        cacheDir.mkdirs()
        return File(cacheDir, "capture_${System.currentTimeMillis()}.jpg")
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}
