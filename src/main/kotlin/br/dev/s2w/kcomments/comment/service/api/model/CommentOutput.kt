package br.dev.s2w.kcomments.comment.service.api.model

import java.time.OffsetDateTime
import java.util.*

data class CommentOutput(
    val id: UUID,
    val text: String,
    val author: String,
    var createdAt: OffsetDateTime

)
