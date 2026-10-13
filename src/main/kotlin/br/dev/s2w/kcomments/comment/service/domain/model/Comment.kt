package br.dev.s2w.kcomments.comment.service.domain.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.OffsetDateTime
import java.util.*

@Entity
data class Comment(
    @Id
    val id: UUID = UUID.randomUUID(),
    val text: String,
    val author: String,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)
