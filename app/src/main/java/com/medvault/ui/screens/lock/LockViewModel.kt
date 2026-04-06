package com.medvault.ui.screens.lock

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LockViewModel @Inject constructor() : ViewModel() {
    // Lock screen state:
    // - Biometric availability
    // - PIN verification
    // - Auto-lock timeout
}
