package com.medvault.ui.screens.visitdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medvault.data.relation.VisitWithDetails
import com.medvault.data.repository.VisitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VisitDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val visitRepository: VisitRepository
) : ViewModel() {

    private val visitId: String = checkNotNull(savedStateHandle["visitId"])

    private val _visit = MutableStateFlow<VisitWithDetails?>(null)
    val visit: StateFlow<VisitWithDetails?> = _visit.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadVisit()
    }

    private fun loadVisit() {
        viewModelScope.launch {
            _isLoading.value = true
            _visit.value = visitRepository.getVisitById(visitId)
            _isLoading.value = false
        }
    }

    fun deleteVisit() {
        viewModelScope.launch {
            visitRepository.deleteVisit(visitId)
        }
    }
}
