package br.dev.s2w.kcomments.comment.service.api.client

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class RestClientConfig {

    @Bean
    fun moderationClient(factory: RestClientFactory): ModerationClient =
        factory.moderateRestClient()
            .let(RestClientAdapter::create)
            .run {
                HttpServiceProxyFactory.builderFor(this)
                    .build()
                    .createClient(ModerationClient::class.java)
            }


}