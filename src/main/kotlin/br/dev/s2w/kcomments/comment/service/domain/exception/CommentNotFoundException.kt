package br.dev.s2w.kcomments.comment.service.domain.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class CommentNotFoundException : RuntimeException()