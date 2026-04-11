package com.medvault.ui.screens.addvisit.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.window.PopupProperties

/**
 * A generic autocomplete field that displays a dropdown of suggestions as the user types.
 *
 * @param value             Current text value.
 * @param onValueChange     Callback when text changes.
 * @param suggestions       List of suggestion items.
 * @param onSuggestionSelected Callback when a suggestion is picked.
 * @param label             Label for the text field.
 * @param modifier          Modifier for the text field.
 * @param itemContent       Composable to render each suggestion item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<T>,
    onSuggestionSelected: (T) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    itemContent: @Composable (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }

    // Show dropdown only if there are suggestions and the field is focused
    val shouldShowDropdown = expanded && suggestions.isNotEmpty() && isFocused

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { 
                    isFocused = it.isFocused
                    if (it.isFocused && value.isNotEmpty()) {
                        expanded = true
                    }
                },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        DropdownMenu(
            expanded = shouldShowDropdown,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f), // Avoid taking full width to keep some margin
            properties = PopupProperties(focusable = false) // Allow typing while menu is open
        ) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = { itemContent(suggestion) },
                    onClick = {
                        onSuggestionSelected(suggestion)
                        expanded = false
                    }
                )
            }
        }
    }
}
