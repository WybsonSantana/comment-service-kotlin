package br.dev.s2w.kcomments.comment.service.api.exception

import br.dev.s2w.kcomments.comment.service.api.client.ModerationClientBadGatewayException
import br.dev.s2w.kcomments.comment.service.domain.exception.ModerationRejectedException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URI
import java.nio.channels.ClosedChannelException

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(
        SocketTimeoutException::class,
        ConnectException::class,
        ClosedChannelException::class
    )
    fun handle(e: IOException): ProblemDetail =
        ProblemDetail.forStatus(HttpStatus.GATEWAY_TIMEOUT).apply {
            title = "Gateway timeout"
            detail = e.message
            type = URI.create("/errors/gateway-timeout")
        }

    @ExceptionHandler(ModerationClientBadGatewayException::class)
    fun handle(e: ModerationClientBadGatewayException?): ProblemDetail =
        ProblemDetail.forStatus(HttpStatus.BAD_GATEWAY).apply {
            title = "Bad Gateway"
            detail = "An unexpected error occurred while communicating with the moderation service."
            type = URI.create("/errors/bad-gateway")
        }

    @ExceptionHandler(ModerationRejectedException::class)
    fun handle(e: ModerationRejectedException): ProblemDetail =
        ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY).apply {
            title = "Unprocessable Entity"
            detail = e.message
            type = URI.create("/errors/unprocessable-entity")
        }

}
