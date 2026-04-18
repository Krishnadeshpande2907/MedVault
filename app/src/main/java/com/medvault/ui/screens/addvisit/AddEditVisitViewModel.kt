package com.medvault.ui.screens.addvisit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medvault.camera.CameraCapture
import com.medvault.data.repository.ContactRepository
import com.medvault.data.repository.MediaRepository
import com.medvault.data.repository.VisitRepository
import com.medvault.domain.model.Medicine
import com.medvault.ocr.OcrService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Prescription mode ─────────────────────────────────────────────────────────

enum class PrescriptionMode { NONE, PHOTO, MANUAL }

// ── UI State ──────────────────────────────────────────────────────────────────

data class AddEditVisitUiState(
    val currentStep: Int = 1,
    val showCamera: Boolean = false,

    // ── Step 2 — Prescription ─────────────────────────────────────
    val prescriptionMode: PrescriptionMode = PrescriptionMode.NONE,
    /** URI of the temp-captured prescription image (in cacheDir). */
    val capturedImageUri: Uri? = null,
    /** Raw OCR text extracted from the image. */
    val ocrRawText: String = "",
    /** Whether ML Kit OCR is currently running. */
    val isOcrLoading: Boolean = false,
    /** Human-readable error message from OCR, or null if no error. */
    val ocrError: String? = null,
    /** Medicine list — initially empty or pre-populated from OCR. */
    val medicines: List<Medicine> = listOf(Medicine("", "", "", "")),

    // ── Steps 1 / 3 / 4 — populated in B3 ────────────────────────
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val savedSuccessfully: Boolean = false
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class AddEditVisitViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val visitRepository: VisitRepository,
    private val contactRepository: ContactRepository,
    private val mediaRepository: MediaRepository,
    private val ocrService: OcrService,
    val cameraCapture: CameraCapture
) : ViewModel() {

    val visitId: String? = savedStateHandle["visitId"]
    val isEditing = visitId != null

    private val _uiState = MutableStateFlow(AddEditVisitUiState())
    val uiState: StateFlow<AddEditVisitUiState> = _uiState.asStateFlow()

    /** Convenience alias kept for backward compat with StepIndicator binding. */
    val currentStep: StateFlow<Int>
        get() = MutableStateFlow(_uiState.value.currentStep).also { flow ->
            viewModelScope.launch {
                _uiState.collect { state -> flow.value = state.currentStep }
            }
        }

    // ── Step navigation ───────────────────────────────────────────────────────

    fun setStep(step: Int) {
        _uiState.update { it.copy(currentStep = step.coerceIn(1, 4)) }
    }

    fun nextStep() = setStep(_uiState.value.currentStep + 1)
    fun prevStep() = setStep(_uiState.value.currentStep - 1)

    // ── Step 2 — Camera / Manual mode ────────────────────────────────────────

    /** User tapped "Take a photo" — open camera overlay. */
    fun onPhotoModeSelected() {
        _uiState.update {
            it.copy(
                prescriptionMode = PrescriptionMode.PHOTO,
                showCamera = true,
                ocrError = null
            )
        }
    }

    /** User tapped "Type manually" — skip camera, show empty medicine form. */
    fun onManualModeSelected() {
        _uiState.update {
            it.copy(
                prescriptionMode = PrescriptionMode.MANUAL,
                showCamera = false,
                capturedImageUri = null,
                ocrRawText = "",
                ocrError = null,
                medicines = listOf(Medicine("", "", "", ""))
            )
        }
    }

    /** Called from CameraScreen when the user taps "Use this". */
    fun onPhotoCaptured(uri: Uri) {
        _uiState.update {
            it.copy(
                showCamera = false,
                capturedImageUri = uri,
                prescriptionMode = PrescriptionMode.PHOTO
            )
        }
        runOcr(uri)
    }

    /** User tapped "Retake" inside CameraScreen — go back to viewfinder. */
    fun onRetakeRequested() {
        // Delete the previously captured temp file
        _uiState.value.capturedImageUri?.path?.let { path ->
            java.io.File(path).takeIf { it.exists() }?.delete()
        }
        _uiState.update {
            it.copy(
                showCamera = true,
                capturedImageUri = null,
                ocrRawText = "",
                ocrError = null,
                medicines = listOf(Medicine("", "", "", ""))
            )
        }
    }

    /** User closed the camera without capturing (back button). */
    fun onCameraCancel() {
        _uiState.update {
            it.copy(
                showCamera = false,
                prescriptionMode = PrescriptionMode.NONE,
                capturedImageUri = null,
                ocrRawText = "",
                ocrError = null
            )
        }
    }

    /** Reset to mode-selection state (user tapped "Change method"). */
    fun onPrescriptionModeReset() {
        _uiState.value.capturedImageUri?.path?.let { path ->
            java.io.File(path).takeIf { it.exists() }?.delete()
        }
        _uiState.update {
            it.copy(
                prescriptionMode = PrescriptionMode.NONE,
                showCamera = false,
                capturedImageUri = null,
                ocrRawText = "",
                ocrError = null,
                medicines = listOf(Medicine("", "", "", ""))
            )
        }
    }

    // ── OCR pipeline ──────────────────────────────────────────────────────────

    private fun runOcr(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOcrLoading = true, ocrError = null) }
            try {
                val rawText = ocrService.extractTextFromImage(context, uri)
                val parsed = ocrService.parseMedicinesFromText(rawText)
                _uiState.update {
                    it.copy(
                        isOcrLoading = false,
                        ocrRawText = rawText,
                        medicines = parsed.ifEmpty { listOf(Medicine("", "", "", "")) }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOcrLoading = false,
                        ocrError = "Could not read prescription text. Please check the photo or enter medicines manually."
                    )
                }
            }
        }
    }

    /** User tapped "Retry OCR" after an error. */
    fun retryOcr() {
        _uiState.value.capturedImageUri?.let { runOcr(it) }
    }

    // ── Medicines list ────────────────────────────────────────────────────────

    fun onMedicinesChanged(medicines: List<Medicine>) {
        _uiState.update { it.copy(medicines = medicines) }
    }

    fun onOcrRawTextChanged(text: String) {
        _uiState.update { it.copy(ocrRawText = text) }
    }
}
