package com.medvault.ui.screens.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    // Settings state will be managed here
    // - App lock toggle, PIN, auto-lock timeout
    // - Data export/import/delete
    // TODO: PHASE 2 — AI: Add AI API key configuration state
}
