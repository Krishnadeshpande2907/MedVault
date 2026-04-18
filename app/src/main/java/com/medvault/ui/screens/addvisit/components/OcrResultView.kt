package com.medvault.ui.screens.addvisit.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.medvault.domain.model.Medicine

/**
 * Displays the OCR pipeline status and results for the prescription step.
 *
 * States:
 *  - Loading  : spinner + "Reading prescription..." label
 *  - Error    : error card with retry button
 *  - Success  : collapsible raw OCR text view + editable [MedicineForm]
 *
 * @param isLoading          Whether OCR is currently running.
 * @param ocrError           Error message string, or null if no error.
 * @param rawText            Raw text extracted via ML Kit.
 * @param onRawTextChanged   Callback when the user edits the raw text field.
 * @param medicines          Current medicine list (may be pre-populated by parser).
 * @param onMedicinesChanged Callback when the user adds / edits / removes medicines.
 * @param onRetry            Called when the user taps "Retry" after an error.
 */
@Composable
fun OcrResultView(
    isLoading: Boolean,
    ocrError: String?,
    rawText: String,
    onRawTextChanged: (String) -> Unit,
    medicines: List<Medicine>,
    onMedicinesChanged: (List<Medicine>) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = Triple(isLoading, ocrError != null, rawText.isNotBlank()),
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ocr_state",
        modifier = modifier
    ) { (loading, hasError, hasText) ->
        when {
            loading -> OcrLoadingState()
            hasError -> OcrErrorState(error = ocrError ?: "Unknown error", onRetry = onRetry)
            else -> OcrSuccessState(
                rawText = rawText,
                onRawTextChanged = onRawTextChanged,
                medicines = medicines,
                onMedicinesChanged = onMedicinesChanged
            )
        }
    }
}

// ── Loading ───────────────────────────────────────────────────────────────────

@Composable
private fun OcrLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Reading prescription...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "ML Kit is scanning on-device",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

// ── Error ─────────────────────────────────────────────────────────────────────

@Composable
private fun OcrErrorState(error: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "OCR failed",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Retry OCR")
            }
        }
    }
}

// ── Success ───────────────────────────────────────────────────────────────────

@Composable
private fun OcrSuccessState(
    rawText: String,
    onRawTextChanged: (String) -> Unit,
    medicines: List<Medicine>,
    onMedicinesChanged: (List<Medicine>) -> Unit
) {
    var isRawTextExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // ── Raw OCR text (collapsible) ────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.TextSnippet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Extracted text",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    TextButton(onClick = { isRawTextExpanded = !isRawTextExpanded }) {
                        Text(if (isRawTextExpanded) "Hide" else "Show & edit")
                    }
                }

                if (isRawTextExpanded) {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rawText,
                        onValueChange = onRawTextChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Raw OCR output") },
                        minLines = 4,
                        maxLines = 8,
                        shape = MaterialTheme.shapes.medium,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // ── Section header ────────────────────────────────────────
        Column {
            Text(
                text = "Medicine details",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Review and correct the extracted information",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ── Editable medicine form ────────────────────────────────
        MedicineForm(
            medicines = medicines,
            onMedicinesChanged = onMedicinesChanged
        )
    }
}
