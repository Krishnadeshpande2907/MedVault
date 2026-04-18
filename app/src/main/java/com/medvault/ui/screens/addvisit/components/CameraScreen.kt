package com.medvault.ui.screens.addvisit.components

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.PermissionChecker
import coil.compose.AsyncImage
import com.medvault.camera.CameraCapture
import kotlinx.coroutines.launch

/**
 * Full-screen camera composable for prescription photo capture.
 *
 * Handles the CAMERA runtime permission itself — shows a permission rationale
 * card if denied, and only starts the CameraX preview once permission is granted.
 *
 * Internal states:
 *  - Permission rationale (if not granted)
 *  - Viewfinder: live camera preview + shutter button
 *  - Preview: captured photo + "Retake" / "Use this" buttons
 *
 * @param cameraCapture     Hilt-injected [CameraCapture] from the parent ViewModel.
 * @param onPhotoCaptured   Called with the captured [Uri] when user taps "Use this".
 * @param onCancel          Called when user closes camera without capturing.
 */
@Composable
fun CameraScreen(
    cameraCapture: CameraCapture,
    onPhotoCaptured: (Uri) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // ── Permission state ──────────────────────────────────────────────────────
    var hasCameraPermission by remember {
        mutableStateOf(
            PermissionChecker.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PermissionChecker.PERMISSION_GRANTED
        )
    }
    var permissionDeniedPermanently by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) permissionDeniedPermanently = true
    }

    // Request permission automatically the first time this screen appears
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ── Camera capture state ──────────────────────────────────────────────────
    var capturedUri by remember { mutableStateOf<Uri?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var captureError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            // ── Permission denied / not yet granted ───────────────────────────
            !hasCameraPermission -> {
                CameraPermissionRationale(
                    onRequestPermission = {
                        permissionDeniedPermanently = false
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onCancel = onCancel,
                    showOpenSettingsHint = permissionDeniedPermanently
                )
            }

            // ── Photo preview (after capture) ─────────────────────────────────
            capturedUri != null -> {
                AsyncImage(
                    model = capturedUri,
                    contentDescription = "Captured prescription",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Dark overlay at the bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Use this photo?",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { capturedUri = null; captureError = null },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color.White.copy(alpha = 0.6f)
                                )
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Retake")
                            }

                            Button(
                                onClick = { capturedUri?.let(onPhotoCaptured) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Use this")
                            }
                        }
                    }
                }
            }

            // ── Live viewfinder ───────────────────────────────────────────────
            else -> {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).also { previewView ->
                            cameraCapture.startCamera(lifecycleOwner, previewView)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Close button
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close camera",
                        tint = Color.White
                    )
                }

                // Guide text
                Text(
                    text = "Position the prescription clearly in frame",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 64.dp)
                        .background(Color.Black.copy(alpha = 0.35f), MaterialTheme.shapes.small)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )

                // Viewfinder guide rectangle
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.85f)
                        .height(260.dp)
                        .border(
                            2.dp,
                            Color.White.copy(alpha = 0.5f),
                            MaterialTheme.shapes.medium
                        )
                )

                // Bottom controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(
                        visible = captureError != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = captureError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Shutter button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(3.dp, Color.White, CircleShape)
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    captureError = null
                                    isCapturing = true
                                    val tempFile = cameraCapture.createTempImageFile()
                                    scope.launch {
                                        try {
                                            val uri = cameraCapture.takePhotoSuspend(tempFile)
                                            capturedUri = uri
                                        } catch (e: Exception) {
                                            captureError = "Capture failed — please try again."
                                        } finally {
                                            isCapturing = false
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Camera,
                                    contentDescription = "Capture photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Permission rationale screen ───────────────────────────────────────────────

@Composable
private fun CameraPermissionRationale(
    onRequestPermission: () -> Unit,
    onCancel: () -> Unit,
    showOpenSettingsHint: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Close button
        IconButton(
            onClick = onCancel,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "Camera permission needed",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "MedVault needs camera access to scan your prescription. Photos are processed on-device and never uploaded.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            if (showOpenSettingsHint) {
                Text(
                    text = "Permission was denied. Please open Settings → Apps → MedVault → Permissions → Camera to enable it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Allow camera access")
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
            ) {
                Text("Cancel")
            }
        }
    }
}
