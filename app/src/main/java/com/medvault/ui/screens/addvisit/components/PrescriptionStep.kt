package com.medvault.ui.screens.addvisit.components

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.medvault.domain.model.Medicine
import com.medvault.ui.screens.addvisit.PrescriptionMode

/**
 * Step 2 content composable — Prescription entry.
 *
 * Renders one of three sub-states:
 *  - [PrescriptionMode.NONE]   : Mode selector — "Take a photo" / "Type manually" cards
 *  - [PrescriptionMode.PHOTO]  : Prescription thumbnail + [OcrResultView]
 *  - [PrescriptionMode.MANUAL] : [MedicineForm] directly
 *
 * @param prescriptionMode      Current mode selection.
 * @param onTakePhoto           Callback to open the camera overlay.
 * @param onTypeManually        Callback to switch to manual mode.
 * @param capturedImageUri      URI of the captured prescription image, or null.
 * @param onChangeMethod        Callback to reset back to mode selection.
 * @param isOcrLoading          Whether OCR is running.
 * @param ocrError              OCR error message or null.
 * @param ocrRawText            Raw OCR extracted text.
 * @param onRawTextChanged      Callback when user edits raw text.
 * @param medicines             Current medicine list.
 * @param onMedicinesChanged    Callback when medicine list changes.
 * @param onRetryOcr            Callback to re-run OCR on the captured image.
 */
@Composable
fun PrescriptionStep(
    prescriptionMode: PrescriptionMode,
    onTakePhoto: () -> Unit,
    onTypeManually: () -> Unit,
    capturedImageUri: Uri?,
    onChangeMethod: () -> Unit,
    isOcrLoading: Boolean,
    ocrError: String?,
    ocrRawText: String,
    onRawTextChanged: (String) -> Unit,
    medicines: List<Medicine>,
    onMedicinesChanged: (List<Medicine>) -> Unit,
    onRetryOcr: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {

        // Section header
        Text(
            text = "Prescription",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Add your prescription details for this visit",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(Modifier.height(20.dp))

        AnimatedContent(
            targetState = prescriptionMode,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "prescription_mode"
        ) { mode ->
            when (mode) {
                PrescriptionMode.NONE -> {
                    ModeSelector(
                        onTakePhoto = onTakePhoto,
                        onTypeManually = onTypeManually
                    )
                }

                PrescriptionMode.PHOTO -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Captured image thumbnail + change method link
                        PrescriptionPhotoHeader(
                            capturedImageUri = capturedImageUri,
                            onChangeMethod = onChangeMethod
                        )

                        HorizontalDivider()

                        // OCR pipeline result
                        OcrResultView(
                            isLoading = isOcrLoading,
                            ocrError = ocrError,
                            rawText = ocrRawText,
                            onRawTextChanged = onRawTextChanged,
                            medicines = medicines,
                            onMedicinesChanged = onMedicinesChanged,
                            onRetry = onRetryOcr
                        )
                    }
                }

                PrescriptionMode.MANUAL -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Header with change method link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Enter medicines manually",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            TextButton(onClick = onChangeMethod) {
                                Icon(
                                    Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Change method")
                            }
                        }

                        MedicineForm(
                            medicines = medicines,
                            onMedicinesChanged = onMedicinesChanged
                        )
                    }
                }
            }
        }
    }
}

// ── Mode selector cards ───────────────────────────────────────────────────────

@Composable
private fun ModeSelector(
    onTakePhoto: () -> Unit,
    onTypeManually: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "How would you like to add the prescription?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModeSelectorCard(
                icon = Icons.Outlined.CameraAlt,
                title = "Take a photo",
                subtitle = "Scan with camera,\nwe'll extract the text",
                onClick = onTakePhoto,
                modifier = Modifier.weight(1f)
            )
            ModeSelectorCard(
                icon = Icons.Outlined.Edit,
                title = "Type manually",
                subtitle = "Enter medicine\ndetails by hand",
                onClick = onTypeManually,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModeSelectorCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ── Prescription photo header ─────────────────────────────────────────────────

@Composable
private fun PrescriptionPhotoHeader(
    capturedImageUri: Uri?,
    onChangeMethod: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (capturedImageUri != null) {
                AsyncImage(
                    model = capturedImageUri,
                    contentDescription = "Prescription photo",
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp)
                )
            }
        }

        // Info + action
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Prescription photo",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (capturedImageUri != null) "Photo captured" else "Capturing...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextButton(onClick = onChangeMethod) {
            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Change")
        }
    }
}
