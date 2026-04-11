package com.medvault.ui.screens.addvisit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.medvault.data.repository.ContactRepository
import com.medvault.data.repository.MediaRepository
import com.medvault.data.repository.VisitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AddEditVisitViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val visitId: String? = savedStateHandle["visitId"]
    val isEditing = visitId != null

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()
}
