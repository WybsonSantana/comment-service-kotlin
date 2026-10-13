package br.dev.s2w.kcomments.comment.service.api.converter

import br.dev.s2w.kcomments.comment.service.api.model.CommentOutput
import br.dev.s2w.kcomments.comment.service.domain.model.Comment

fun Comment.toCommentOutput(): CommentOutput =
    CommentOutput(
        id = this.id,
        text = this.text,
        author = this.author,
        createdAt = this.createdAt
    )
