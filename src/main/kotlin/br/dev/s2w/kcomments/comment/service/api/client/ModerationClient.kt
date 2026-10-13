package br.dev.s2w.kcomments.comment.service.api.client

import br.dev.s2w.kcomments.comment.service.api.model.ModerationRequest
import br.dev.s2w.kcomments.comment.service.api.model.ModerationResponse
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

@HttpExchange("/api/moderate")
interface ModerationClient {

    @PostExchange
    fun moderateComment(@RequestBody request: ModerationRequest): ModerationResponse

}
