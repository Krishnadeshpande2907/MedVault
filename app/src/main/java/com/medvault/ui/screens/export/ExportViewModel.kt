package com.medvault.ui.screens.export

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val visitId: String = checkNotNull(savedStateHandle["visitId"])
}
