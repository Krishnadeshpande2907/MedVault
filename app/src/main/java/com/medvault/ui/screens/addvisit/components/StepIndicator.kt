package com.medvault.ui.screens.addvisit.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val STEP_LABELS = listOf("Doctor", "Prescription", "Media", "Review")

/**
 * 4-step horizontal progress indicator for the Add/Edit Visit form.
 *
 * @param currentStep  The active step, 1-indexed (1..4).
 * @param totalSteps   Total number of steps (default 4).
 * @param modifier     Modifier applied to the root Row.
 */
@Composable
fun StepIndicator(
    currentStep: Int,
    modifier: Modifier = Modifier,
    totalSteps: Int = 4
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..totalSteps) {
            // ── Step circle ──────────────────────────────────────────────
            StepCircle(
                stepNumber = step,
                state = when {
                    step < currentStep  -> StepState.Completed
                    step == currentStep -> StepState.Active
                    else                -> StepState.Upcoming
                }
            )

            // ── Connector line between circles ───────────────────────────
            if (step < totalSteps) {
                val lineColor by animateColorAsState(
                    targetValue = if (step < currentStep)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outlineVariant,
                    animationSpec = tween(300),
                    label = "connector_$step"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(lineColor)
                )
            }
        }
    }
}

// ── With-labels variant ───────────────────────────────────────────────────────

/**
 * Full step indicator with labels beneath each circle.
 * Circles and labels are rendered in the same Row loop so each label is
 * guaranteed to be centred directly below its circle, regardless of how
 * wide the flexible connector lines grow.
 */
@Composable
fun StepIndicatorWithLabels(
    currentStep: Int,
    modifier: Modifier = Modifier,
    totalSteps: Int = 4
) {
    // Circle diameter is 32 dp. The connector Box (2 dp tall) must be offset
    // by (32 - 2) / 2 = 15 dp from the top so its centre aligns with the
    // circle centres when the Row uses Alignment.Top.
    val connectorTopPadding = 15.dp

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        for (step in 1..totalSteps) {

            // ── Circle + label stacked in a Column ───────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val state = when {
                    step < currentStep  -> StepState.Completed
                    step == currentStep -> StepState.Active
                    else                -> StepState.Upcoming
                }
                StepCircle(stepNumber = step, state = state)

                val isActive = step == currentStep
                val isDone   = step < currentStep
                Text(
                    text = STEP_LABELS[step - 1],
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = when {
                        isActive -> MaterialTheme.colorScheme.primary
                        isDone   -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        else     -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // ── Connector line between columns ────────────────────────────
            if (step < totalSteps) {
                val lineColor by animateColorAsState(
                    targetValue = if (step < currentStep)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outlineVariant,
                    animationSpec = tween(300),
                    label = "wl_connector_$step"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = connectorTopPadding)
                        .height(2.dp)
                        .background(lineColor)
                )
            }
        }
    }
}

// ── Internal helpers ─────────────────────────────────────────────────────────

private enum class StepState { Completed, Active, Upcoming }

@Composable
private fun StepCircle(
    stepNumber: Int,
    state: StepState
) {
    val bgColor by animateColorAsState(
        targetValue = when (state) {
            StepState.Completed -> MaterialTheme.colorScheme.primary
            StepState.Active    -> MaterialTheme.colorScheme.primary
            StepState.Upcoming  -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300),
        label = "circle_bg_$stepNumber"
    )

    val contentColor: Color = when (state) {
        StepState.Completed -> MaterialTheme.colorScheme.onPrimary
        StepState.Active    -> MaterialTheme.colorScheme.onPrimary
        StepState.Upcoming  -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (state == StepState.Completed) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Step $stepNumber complete",
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = stepNumber.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = contentColor
            )
        }
    }
}
