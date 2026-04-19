package com.medvault.ui.screens.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medvault.data.relation.VisitWithDetails
import com.medvault.data.repository.VisitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.medvault.ui.screens.timeline.components.VisitFilter

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val visitRepository: VisitRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortAscending = MutableStateFlow(false)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    private val _currentFilter = MutableStateFlow(VisitFilter())
    val currentFilter: StateFlow<VisitFilter> = _currentFilter.asStateFlow()

    private val _availableDoctors = MutableStateFlow<List<String>>(emptyList())
    val availableDoctors: StateFlow<List<String>> = _availableDoctors.asStateFlow()

    private val _availableTags = MutableStateFlow<List<String>>(emptyList())
    val availableTags: StateFlow<List<String>> = _availableTags.asStateFlow()

    init {
        observeFilterOptions()
    }

    /**
     * Keeps _availableDoctors and _availableTags up-to-date by observing the
     * visits flow continuously — each new emission re-derives both lists.
     * This replaces the previous single-shot collect() which missed later changes.
     */
    private fun observeFilterOptions() {
        viewModelScope.launch {
            visitRepository.getAllVisitsDesc().collect { list ->
                // Derive doctors
                _availableDoctors.value = list
                    .map { it.visit.doctorName }
                    .distinct()
                    .sorted()

                // Derive tags by decoding each visit's JSON tag list
                _availableTags.value = list
                    .flatMap { item ->
                        try {
                            kotlinx.serialization.json.Json
                                .decodeFromString<List<String>>(item.visit.tags)
                        } catch (_: Exception) {
                            emptyList()
                        }
                    }
                    .distinct()
                    .sorted()
            }
        }
    }

    // Flow of visits based on search query, filters, and sort
    val visits: StateFlow<List<VisitWithDetails>> = combine(
        _searchQuery.flatMapLatest { query ->
            if (query.isBlank()) {
                visitRepository.getAllVisitsDesc()
            } else {
                visitRepository.searchVisitsWithDetails(query)
            }
        },
        _currentFilter,
        _sortAscending
    ) { list, filter, ascending ->
        list.filter { item ->
            val doctorMatch = filter.doctorNames.isEmpty() || filter.doctorNames.contains(item.visit.doctorName)
            
            // Parse item tags
            val itemTags = try {
                kotlinx.serialization.json.Json.decodeFromString<List<String>>(item.visit.tags)
            } catch (_: Exception) {
                emptyList()
            }
            val tagMatch = filter.tags.isEmpty() || itemTags.any { it in filter.tags }
            
            val mediaMatch = filter.mediaTypes.isEmpty() || filter.mediaTypes.any { type ->
                when (type.lowercase()) {
                    "rx", "photo" -> item.prescription?.photoUri != null
                    "report" -> item.attachments.any { it.type == "report" }
                    "pdf" -> item.attachments.any { it.type == "pdf" }
                    "x-ray" -> item.attachments.any { it.type == "xray" }
                    else -> false
                }
            }
            
            doctorMatch && tagMatch && mediaMatch
        }.let { filteredList ->
            if (ascending) {
                filteredList.sortedBy { it.visit.date }
            } else {
                filteredList.sortedByDescending { it.visit.date }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterChanged(newFilter: VisitFilter) {
        _currentFilter.value = newFilter
    }

    fun toggleSortOrder() {
        _sortAscending.value = !_sortAscending.value
    }
}
