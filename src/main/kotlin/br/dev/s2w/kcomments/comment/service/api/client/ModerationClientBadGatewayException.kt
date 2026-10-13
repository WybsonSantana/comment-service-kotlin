package br.dev.s2w.kcomments.comment.service.api.client

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_GATEWAY)
class ModerationClientBadGatewayException : RuntimeException()