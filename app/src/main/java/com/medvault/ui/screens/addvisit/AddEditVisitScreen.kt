package com.medvault.ui.screens.addvisit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medvault.ui.screens.addvisit.components.CameraScreen
import com.medvault.ui.screens.addvisit.components.PrescriptionStep
import com.medvault.ui.screens.addvisit.components.StepIndicatorWithLabels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditVisitScreen(
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddEditVisitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val title = if (viewModel.isEditing) "Edit visit" else "Add visit"
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate back automatically once the save completes
    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) onSaved()
    }

    // Show save errors in a Snackbar
    LaunchedEffect(uiState.saveError) {
        uiState.saveError?.let { snackbarHostState.showSnackbar(it) }
    }

    // imePadding on the root Box shifts the entire Scaffold (including the bottom
    // nav bar) above the keyboard. The Scaffold then re-measures the content area
    // correctly, so the Column's verticalScroll can reach all fields without
    // adding any extra blank space.
    Box(modifier = Modifier.fillMaxSize().imePadding()) {

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        // Always goes back to home — step navigation is handled
                        // by the "Back" button in the bottom StepNavigationBar.
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                StepNavigationBar(
                    currentStep = uiState.currentStep,
                    isSaving = uiState.isSaving,
                    isNextEnabled = !uiState.isOcrLoading && !uiState.isSaving,
                    onNext = {
                        if (uiState.currentStep < 4) viewModel.nextStep()
                        else viewModel.saveVisit()
                    },
                    onBack = {
                        if (uiState.currentStep > 1) viewModel.prevStep()
                        else onNavigateBack()
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Step indicator
                StepIndicatorWithLabels(
                    currentStep = uiState.currentStep,
                    modifier = Modifier.padding(vertical = 20.dp)
                )

                // Step content
                when (uiState.currentStep) {
                    1 -> StepPlaceholder("Doctor info form — coming in B3")
                    2 -> PrescriptionStep(
                        prescriptionMode = uiState.prescriptionMode,
                        onTakePhoto = { viewModel.onPhotoModeSelected() },
                        onTypeManually = { viewModel.onManualModeSelected() },
                        capturedImageUri = uiState.capturedImageUri,
                        onChangeMethod = { viewModel.onPrescriptionModeReset() },
                        isOcrLoading = uiState.isOcrLoading,
                        ocrError = uiState.ocrError,
                        ocrRawText = uiState.ocrRawText,
                        onRawTextChanged = { viewModel.onOcrRawTextChanged(it) },
                        medicines = uiState.medicines,
                        onMedicinesChanged = { viewModel.onMedicinesChanged(it) },
                        onRetryOcr = { viewModel.retryOcr() },
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    3 -> StepPlaceholder("Attach media — coming in B3")
                    4 -> StepPlaceholder("Review & save — coming in B3")
                }
            }
        }

        // Camera overlay — rendered above Scaffold, full-screen
        if (uiState.showCamera) {
            CameraScreen(
                cameraCapture = viewModel.cameraCapture,
                onPhotoCaptured = { uri -> viewModel.onPhotoCaptured(uri) },
                onCancel = { viewModel.onCameraCancel() }
            )
        }
    }
}

// ── Step placeholder ──────────────────────────────────────────────────────────

@Composable
private fun StepPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Step navigation bar ───────────────────────────────────────────────────────

@Composable
private fun StepNavigationBar(
    currentStep: Int,
    isSaving: Boolean = false,
    isNextEnabled: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            if (currentStep > 1) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text("Back")
                }
            }

            Button(
                onClick = onNext,
                enabled = isNextEnabled,
                modifier = Modifier.weight(if (currentStep > 1) 1f else 1f)
            ) {
                if (currentStep == 4 && isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (currentStep < 4) "Next" else "Save visit")
                    if (currentStep < 4) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
