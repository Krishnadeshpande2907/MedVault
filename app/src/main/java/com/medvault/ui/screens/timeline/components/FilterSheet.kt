package com.medvault.ui.screens.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * State object for the visit filters.
 */
data class VisitFilter(
    val doctorNames: Set<String> = emptySet(),
    val tags: Set<String> = emptySet(),
    val mediaTypes: Set<String> = emptySet(),
    val startDate: Long? = null,
    val endDate: Long? = null
)

/**
 * A bottom sheet for filtering visits by various criteria.
 *
 * @param filter            Current active filter.
 * @param availableDoctors   List of all unique doctor names in the DB.
 * @param availableTags      List of all unique tags in the DB.
 * @param onFilterApplied   Callback when the user clicks 'Apply'.
 * @param onDismiss         Callback when the sheet is dismissed.
 * @param sheetState        State of the bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheet(
    filter: VisitFilter,
    availableDoctors: List<String>,
    availableTags: List<String>,
    onFilterApplied: (VisitFilter) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    // Local state to manage changes before applying
    var currentFilter by remember(filter) { mutableStateOf(filter) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { 
            // Custom drag handle with title
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp).fillMaxWidth(0.1f).padding(top = 8.dp)) // Placeholder for handle
                Text(
                    text = "Filter Visits",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Doctors Section ──────────────────────────────────────────
            FilterSection(title = "Doctors") {
                FilterChipGroup(
                    items = availableDoctors,
                    selectedItems = currentFilter.doctorNames,
                    onToggle = { doctor ->
                        val newSet = if (currentFilter.doctorNames.contains(doctor)) currentFilter.doctorNames - doctor 
                                     else currentFilter.doctorNames + doctor
                        currentFilter = currentFilter.copy(doctorNames = newSet)
                    }
                )
            }

            // ── Tags Section ─────────────────────────────────────────────
            FilterSection(title = "Tags") {
                FilterChipGroup(
                    items = availableTags,
                    selectedItems = currentFilter.tags,
                    onToggle = { tag ->
                        val newSet = if (currentFilter.tags.contains(tag)) currentFilter.tags - tag 
                                     else currentFilter.tags + tag
                        currentFilter = currentFilter.copy(tags = newSet)
                    }
                )
            }

            // ── Media Types Section ─────────────────────────────────────
            FilterSection(title = "Media Types") {
                FilterChipGroup(
                    items = listOf("X-Ray", "Report", "Photo", "PDF"),
                    selectedItems = currentFilter.mediaTypes,
                    onToggle = { type ->
                        val newSet = if (currentFilter.mediaTypes.contains(type)) currentFilter.mediaTypes - type 
                                     else currentFilter.mediaTypes + type
                        currentFilter = currentFilter.copy(mediaTypes = newSet)
                    }
                )
            }

            // ── Date Range Placeholder ──────────────────────────────────
            // Note: DateRangePicker usually takes full screen, so here we provide a static toggle or simple selection
            // For Phase 1, we focus on Doctor/Tag/Type.
            
            Spacer(modifier = Modifier.height(32.dp))

            // ── Actions ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { 
                        currentFilter = VisitFilter() 
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear All")
                }
                Button(
                    onClick = { onFilterApplied(currentFilter) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply Filters")
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp)) // Extra padding for bottom navigation bars
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
        Spacer(modifier = Modifier.height(16.dp))
        // Modern divider
        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(MaterialTheme.colorScheme.outlineVariant))
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun <T> FilterChipGroup(
    items: List<T>,
    selectedItems: Set<T>,
    onToggle: (T) -> Unit,
    labelProvider: (T) -> String = { it.toString() }
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { item ->
            val isSelected = selectedItems.contains(item)
            FilterChip(
                selected = isSelected,
                onClick = { onToggle(item) },
                label = { Text(labelProvider(item)) }
            )
        }
    }
}
