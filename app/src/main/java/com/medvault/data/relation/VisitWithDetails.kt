package com.medvault.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.medvault.data.entity.AttachmentEntity
import com.medvault.data.entity.PrescriptionEntity
import com.medvault.data.entity.VisitEntity

/**
 * Room relation class that loads a visit along with its prescription and attachments
 * in a single @Transaction query.
 */
data class VisitWithDetails(
    @Embedded val visit: VisitEntity,

    @Relation(
        parentColumn = "visitId",
        entityColumn = "visitId"
    )
    val prescription: PrescriptionEntity?,

    @Relation(
        parentColumn = "visitId",
        entityColumn = "visitId"
    )
    val attachments: List<AttachmentEntity>
)
