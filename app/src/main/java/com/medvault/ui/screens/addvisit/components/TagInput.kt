package com.medvault.ui.screens.addvisit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * A custom tag input component that allows users to type a tag and add it to a list.
 * Tags are displayed as removable chips in a wrap-around layout.
 *
 * @param tags          Current list of tags.
 * @param onTagsChanged Callback when the tag list is modified (add/remove).
 * @param modifier      Modifier for the root container.
 * @param label         Label for the text field.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TagInput(
    tags: List<String>,
    onTagsChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Tags"
) {
    var textState by remember { mutableStateOf("") }

    fun addTag() {
        val trimmed = textState.trim()
        if (trimmed.isNotEmpty() && !tags.contains(trimmed)) {
            onTagsChanged(tags + trimmed)
            textState = ""
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = textState,
            onValueChange = { textState = it },
            label = { Text(label) },
            placeholder = { Text("Enter tag and press Enter") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = { addTag() },
                    enabled = textState.trim().isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add tag",
                        tint = if (textState.trim().isNotEmpty()) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { addTag() }
            ),
            shape = MaterialTheme.shapes.medium
        )

        if (tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tags.forEach { tag ->
                    InputChip(
                        selected = false,
                        onClick = { /* Just for display and removal */ },
                        label = {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { onTagsChanged(tags - tag) },
                                modifier = Modifier.size(InputChipDefaults.IconSize)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove $tag",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        },
                        colors = InputChipDefaults.inputChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            trailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                        ),
                        border = null
                    )
                }
            }
        }
    }
}
