package br.dev.s2w.kcomments.comment.service.api.client

import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.time.Duration

@Component
class RestClientFactory(
    private val builder: RestClient.Builder,
) {

    fun moderateRestClient(): RestClient = builder
        .baseUrl("http://localhost:8081")
        .requestFactory(generateClientHttpRequestFactory())
        .defaultStatusHandler(HttpStatusCode::isError) { _, _ ->
            throw ModerationClientBadGatewayException()
        }
        .build()

    private fun generateClientHttpRequestFactory(): ClientHttpRequestFactory =
        SimpleClientHttpRequestFactory().apply {
            setReadTimeout(Duration.ofSeconds(5))
            setConnectTimeout(Duration.ofSeconds(3))
        }

}
