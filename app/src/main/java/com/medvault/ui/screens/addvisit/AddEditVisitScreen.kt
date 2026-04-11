package com.medvault.ui.screens.addvisit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medvault.ui.screens.addvisit.components.StepIndicatorWithLabels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditVisitScreen(
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddEditVisitViewModel = hiltViewModel()
) {
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val title = if (viewModel.isEditing) "Edit visit" else "Add visit"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Step indicator
            StepIndicatorWithLabels(
                currentStep = currentStep,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Step content placeholder
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (currentStep) {
                        1 -> "Doctor info form — coming soon"
                        2 -> "Prescription entry — coming soon"
                        3 -> "Attach media — coming soon"
                        4 -> "Review & save — coming soon"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
