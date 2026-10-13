package br.dev.s2w.kcomments.comment.service.api.model

import java.util.*

data class ModerationResponse(
    val commentId: UUID,
    val approved: Boolean,
    val reason: String
) {

    fun isApproved(): Boolean = approved

}
