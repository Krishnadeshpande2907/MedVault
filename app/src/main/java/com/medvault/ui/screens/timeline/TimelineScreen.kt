package com.medvault.ui.screens.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medvault.ui.screens.timeline.components.EmptyState
import com.medvault.ui.screens.timeline.components.FilterSheet
import com.medvault.ui.screens.timeline.components.SearchBar
import com.medvault.ui.screens.timeline.components.VisitCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onAddVisit: () -> Unit,
    onVisitClick: (String) -> Unit,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val visits by viewModel.visits.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortAscending by viewModel.sortAscending.collectAsStateWithLifecycle()
    val currentFilter by viewModel.currentFilter.collectAsStateWithLifecycle()
    val availableDoctors by viewModel.availableDoctors.collectAsStateWithLifecycle()
    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()

    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MedVault") },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
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
        Column(modifier = Modifier.padding(padding)) {
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onClearQuery = { viewModel.onSearchQueryChange("") }
            )

            if (visits.isEmpty()) {
                val isFiltered = searchQuery.isNotEmpty() || 
                                 currentFilter.doctorNames.isNotEmpty() || 
                                 currentFilter.tags.isNotEmpty() || 
                                 currentFilter.mediaTypes.isNotEmpty()
                
                EmptyState(
                    modifier = Modifier.weight(1f),
                    title = if (isFiltered) "No results found" else "No visits yet",
                    subtitle = if (isFiltered) "Try adjusting your search or filters" else "Add your first medical visit record using the button below"
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(visits, key = { it.visit.visitId }) { visitWithDetails ->
                        VisitCard(
                            visitWithDetails = visitWithDetails,
                            onClick = { onVisitClick(visitWithDetails.visit.visitId) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterSheet(
            filter = currentFilter,
            availableDoctors = availableDoctors,
            availableTags = availableTags,
            onFilterApplied = { newFilter ->
                viewModel.onFilterChanged(newFilter)
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showFilterSheet = false
                }
            },
            onDismiss = { showFilterSheet = false },
            sheetState = sheetState
        )
    }
}
