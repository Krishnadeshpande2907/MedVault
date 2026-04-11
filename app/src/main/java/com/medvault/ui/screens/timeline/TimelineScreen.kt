package com.medvault.ui.screens.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medvault.ui.screens.timeline.components.EmptyState
import com.medvault.ui.screens.timeline.components.VisitCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onAddVisit: () -> Unit,
    onVisitClick: (String) -> Unit,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val visits by viewModel.visits.collectAsStateWithLifecycle()
    val sortAscending by viewModel.sortAscending.collectAsStateWithLifecycle()

    val sortedVisits = if (sortAscending) visits.sortedBy { it.visit.date }
    else visits.sortedByDescending { it.visit.date }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MedVault") },
                actions = {
                    IconButton(onClick = { viewModel.toggleSortOrder() }) {
                        Icon(Icons.Default.SwapVert, contentDescription = "Sort")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddVisit,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add visit")
            }
        }
    ) { padding ->
        if (sortedVisits.isEmpty()) {
            EmptyState(
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sortedVisits, key = { it.visit.visitId }) { visitWithDetails ->
                    VisitCard(
                        visitWithDetails = visitWithDetails,
                        onClick = { onVisitClick(visitWithDetails.visit.visitId) }
                    )
                }
            }
        }
    }
}
