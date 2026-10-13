package br.dev.s2w.kcomments.comment.service.api.model

import java.util.UUID

data class ModerationRequest(
    val commentId: UUID,
    val text: String
)
