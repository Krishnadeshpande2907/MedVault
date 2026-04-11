package com.medvault.ui.screens.timeline.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.medvault.data.entity.AttachmentEntity
import com.medvault.data.entity.PrescriptionEntity
import com.medvault.data.entity.VisitEntity
import com.medvault.data.relation.VisitWithDetails
import kotlinx.serialization.json.Json

/**
 * A richly-styled visit card for the Timeline screen.
 *
 * Displays date, doctor name, hospital, diagnosis, tag chips,
 * and an icon row indicating the types and count of media attachments.
 *
 * @param visitWithDetails  The full visit data, including prescription and attachments.
 * @param onClick           Called when the card is tapped (navigates to Visit Detail).
 * @param modifier          Modifier applied to the root Card.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VisitCard(
    visitWithDetails: VisitWithDetails,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visit: VisitEntity = visitWithDetails.visit
    val attachments: List<AttachmentEntity> = visitWithDetails.attachments
    val prescription: PrescriptionEntity? = visitWithDetails.prescription

    // Parse tags from JSON string
    val tags: List<String> = remember(visit.tags) {
        try {
            Json.decodeFromString<List<String>>(visit.tags)
        } catch (_: Exception) {
            emptyList()
        }
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Row 1: Date + chevron ────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDisplayDate(visit.date),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Row 2: Doctor name ───────────────────────────────────
            Text(
                text = visit.doctorName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // ── Row 3: Hospital (if present) ─────────────────────────
            visit.hospitalName?.let { hospital ->
                Text(
                    text = hospital,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ── Row 4: Specialty (if present) ────────────────────────
            visit.doctorSpecialty?.let { specialty ->
                Text(
                    text = specialty,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Row 5: Diagnosis ─────────────────────────────────────
            Text(
                text = visit.diagnosis ?: "No diagnosis recorded",
                style = MaterialTheme.typography.bodyMedium,
                color = if (visit.diagnosis != null)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // ── Row 6: Tags (if any) ─────────────────────────────────
            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.forEach { tag ->
                        SuggestionChip(
                            onClick = { /* no action — display only */ },
                            label = {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = null
                        )
                    }
                }
            }

            // ── Row 7: Attachment summary icons ──────────────────────
            val hasMedia = attachments.isNotEmpty() || prescription?.photoUri != null
            if (hasMedia) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Prescription photo
                    if (prescription?.photoUri != null) {
                        AttachmentBadge(
                            icon = Icons.Outlined.MedicalServices,
                            label = "Rx"
                        )
                    }

                    // Group attachments by type for compact display
                    val imageCount = attachments.count { it.type in listOf("xray", "before", "after", "other") }
                    val reportCount = attachments.count { it.type == "report" }

                    if (imageCount > 0) {
                        AttachmentBadge(
                            icon = Icons.Outlined.Image,
                            label = "$imageCount"
                        )
                    }
                    if (reportCount > 0) {
                        AttachmentBadge(
                            icon = Icons.Outlined.PictureAsPdf,
                            label = "$reportCount"
                        )
                    }

                    // Total count on the right
                    val totalCount = attachments.size + if (prescription?.photoUri != null) 1 else 0
                    Spacer(modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Attachment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "$totalCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ── Internal helpers ─────────────────────────────────────────────────────────

/**
 * Compact icon + label badge used in the attachment summary row.
 */
@Composable
private fun AttachmentBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Converts a "YYYY-MM-DD" date string to a more readable format: "12 Apr 2026".
 * Falls back to the raw string if parsing fails.
 */
private fun formatDisplayDate(isoDate: String): String {
    return try {
        val parts = isoDate.split("-")
        val year = parts[0]
        val month = parts[1].toInt()
        val day = parts[2].toInt()
        val monthName = listOf(
            "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )[month]
        "$day $monthName $year"
    } catch (_: Exception) {
        isoDate
    }
}
