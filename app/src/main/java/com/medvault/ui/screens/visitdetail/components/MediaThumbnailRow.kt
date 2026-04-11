package com.medvault.ui.screens.visitdetail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.medvault.data.entity.AttachmentEntity
import java.io.File

/**
 * A horizontal scrollable row of media thumbnails.
 *
 * @param attachments       List of attachments for this visit.
 * @param prescriptionPhoto Relative path to the prescription photo (optional).
 * @param onMediaClick      Callback when a thumbnail is clicked.
 * @param resolvePath       Lambda to convert relative paths to absolute File objects.
 * @param modifier          Modifier for the row.
 */
@Composable
fun MediaThumbnailRow(
    attachments: List<AttachmentEntity>,
    prescriptionPhoto: String?,
    onMediaClick: (path: String) -> Unit,
    resolvePath: (String) -> File,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Prescription Photo ───────────────────────────────────────
        prescriptionPhoto?.let { photoPath ->
            item(key = "prescription") {
                ThumbnailItem(
                    title = "Prescription",
                    path = photoPath,
                    isPdf = false,
                    resolvePath = resolvePath,
                    onClick = { onMediaClick(photoPath) }
                )
            }
        }

        // ── Attachments ──────────────────────────────────────────────
        items(attachments, key = { it.mediaId }) { attachment ->
            ThumbnailItem(
                title = attachment.type.uppercase(),
                path = attachment.localUri,
                isPdf = attachment.localUri.lowercase().endsWith(".pdf"),
                resolvePath = resolvePath,
                onClick = { onMediaClick(attachment.localUri) }
            )
        }
    }
}

@Composable
private fun ThumbnailItem(
    title: String,
    path: String,
    isPdf: Boolean,
    resolvePath: (String) -> File,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() },
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isPdf) {
                // PDF Placeholder
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PictureAsPdf,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                // Image Thumbnail via Coil
                AsyncImage(
                    model = resolvePath(path),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Type Label Badge
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(vertical = 2.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
