package com.medvault.ui.screens.addvisit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.medvault.domain.model.Medicine

/**
 * A repeatable form component for entering multiple medicines.
 *
 * @param medicines     The current list of medicines.
 * @param onMedicinesChanged Callback when the list of medicines is updated.
 * @param modifier      Modifier for the root container.
 */
@Composable
fun MedicineForm(
    medicines: List<Medicine>,
    onMedicinesChanged: (List<Medicine>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        medicines.forEachIndexed { index, medicine ->
            MedicineItem(
                medicine = medicine,
                onMedicineChanged = { updated ->
                    val newList = medicines.toMutableList()
                    newList[index] = updated
                    onMedicinesChanged(newList)
                },
                onRemove = {
                    val newList = medicines.toMutableList()
                    newList.removeAt(index)
                    onMedicinesChanged(newList)
                },
                showRemove = medicines.size > 1,
                index = index + 1
            )
        }

        OutlinedButton(
            onClick = {
                onMedicinesChanged(medicines + Medicine("", "", "", ""))
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add another medicine")
        }
    }
}

@Composable
private fun MedicineItem(
    medicine: Medicine,
    onMedicineChanged: (Medicine) -> Unit,
    onRemove: () -> Unit,
    showRemove: Boolean,
    index: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.large,
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Medicine #$index",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (showRemove) {
                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove medicine",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Medicine Name
            OutlinedTextField(
                value = medicine.name,
                onValueChange = { onMedicineChanged(medicine.copy(name = it)) },
                label = { Text("Medicine name") },
                placeholder = { Text("e.g. Paracetamol") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dosage and Frequency
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = medicine.dosage,
                    onValueChange = { onMedicineChanged(medicine.copy(dosage = it)) },
                    label = { Text("Dosage") },
                    placeholder = { Text("e.g. 500mg") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = medicine.frequency,
                    onValueChange = { onMedicineChanged(medicine.copy(frequency = it)) },
                    label = { Text("Frequency") },
                    placeholder = { Text("e.g. 1-0-1") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Duration and Instructions
            OutlinedTextField(
                value = medicine.duration,
                onValueChange = { onMedicineChanged(medicine.copy(duration = it)) },
                label = { Text("Duration") },
                placeholder = { Text("e.g. 5 days") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = medicine.instructions ?: "",
                onValueChange = { onMedicineChanged(medicine.copy(instructions = it.ifBlank { null })) },
                label = { Text("Instructions (Optional)") },
                placeholder = { Text("e.g. After food") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = MaterialTheme.shapes.medium
            )
        }
    }
}
