package br.dev.s2w.kcomments.comment.service.api.controller

import br.dev.s2w.kcomments.comment.service.api.client.ModerationClient
import br.dev.s2w.kcomments.comment.service.api.client.ModerationClientBadGatewayException
import br.dev.s2w.kcomments.comment.service.api.model.ModerationResponse
import br.dev.s2w.kcomments.comment.service.domain.repository.CommentRepository
import com.jayway.jsonpath.JsonPath
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.client.ResourceAccessException
import java.net.SocketTimeoutException
import java.time.OffsetDateTime
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(
    scripts = ["/data/cleanup.sql"],
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS
)
class CommentControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var commentRepository: CommentRepository

    @MockitoBean
    lateinit var moderationClient: ModerationClient

    @Test
    @Sql(
        scripts = ["/data/cleanup.sql", "/data/seed-comments.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    fun `should return paginated comments`() {
        mockMvc.perform(
            get("/api/comments")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "id,asc")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(
                jsonPath("$.content[0].id")
                    .value("a7f3c2e9-6b41-4d85-9a20-3e8f1b5c7d64")
            )
            .andExpect(
                jsonPath("$.content[0].text")
                    .value("Java é muito top, mas prefiro Kotlin.")
            )
            .andExpect(jsonPath("$.content[0].author").value("Wybson Santana"))
            .andExpect(
                jsonPath("$.content[1].id")
                    .value("c9e2b6a4-1d73-48f0-b5a9-7c3e2d8f6140")
            )
            .andExpect(
                jsonPath("$.content[1].text")
                    .value("Java é muito bom, mas ainda prefiro Kotlin.")
            )
            .andExpect(jsonPath("$.content[1].author").value("Wybson Santana"))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.number").value(0))
            .andExpect(jsonPath("$.size").value(10))
    }

    @Test
    @Sql(
        scripts = ["/data/cleanup.sql", "/data/seed-comments.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    fun `should return a comment by ID`() {
        mockMvc.perform(
            get(
                "/api/comments/{id}",
                UUID.fromString("a7f3c2e9-6b41-4d85-9a20-3e8f1b5c7d64")
            )
        )
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$.id")
                    .value("a7f3c2e9-6b41-4d85-9a20-3e8f1b5c7d64")
            )
            .andExpect(jsonPath("$.text").value("Java é muito top, mas prefiro Kotlin."))
            .andExpect(jsonPath("$.author").value("Wybson Santana"))
            .andExpect(jsonPath("$.createdAt").value("2026-10-12T10:00:00Z"))
    }

    @Test
    @Sql(
        scripts = ["/data/cleanup.sql"],
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    fun `should create a comment when moderation is approved`() {
        whenever(moderationClient.moderateComment(any()))
            .thenReturn(
                ModerationResponse(
                    UUID.randomUUID(),
                    approved = true,
                    reason = "Comentário aprovado: nenhuma palavra proibida encontrada."
                )
            )

        val response = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "text": "Java é muito top, mas prefiro Kotlin.",
                        "author": "Wybson Santana"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andExpect(
                MockMvcResultMatchers.content()
                    .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andExpect(
                jsonPath("$.id").value(
                    Matchers.matchesPattern(
                        "^[0-9a-fA-F]{8}-(?:[0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}$"
                    )
                )
            )
            .andExpect(
                jsonPath("$.text")
                    .value("Java é muito top, mas prefiro Kotlin.")
            )
            .andExpect(jsonPath("$.author").value("Wybson Santana"))
            .andExpect(jsonPath("$.createdAt").value(Matchers.notNullValue()))
            .andReturn()
            .response
            .contentAsString

        val commentId = UUID.fromString(JsonPath.read(response, "$.id"))

        val createdAt = JsonPath.read<String>(response, "$.createdAt")
        val createdAtDateTime = OffsetDateTime.parse(createdAt)

        val savedComment = commentRepository.findById(commentId)
            .orElseThrow { AssertionError("Comment was not persisted") }

        assertEquals("Java é muito top, mas prefiro Kotlin.", savedComment.text)
        assertEquals("Wybson Santana", savedComment.author)
        assertNotNull(savedComment.createdAt)
        assertNotNull(createdAtDateTime)
    }

    @Test
    fun `should reject a comment with a banned word`() {
        whenever(moderationClient.moderateComment(any()))
            .thenReturn(
                ModerationResponse(
                    UUID.randomUUID(),
                    approved = false,
                    reason = "Contém palavras proibidas: foda"
                )
            )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "text": "Java é muito top, mas prefiro Kotlin; foda demais!",
                        "author": "Wybson Santana"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(
                MockMvcResultMatchers.content()
                    .contentTypeCompatibleWith("application/problem+json")
            )
            .andExpect(jsonPath("$.type").value("/errors/unprocessable-entity"))
            .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
            .andExpect(jsonPath("$.status").value(422))
            .andExpect(jsonPath("$.detail").value("Contém palavras proibidas: foda"))
            .andExpect(jsonPath("$.instance").value("/api/comments"))
    }

    @Test
    fun `should reject a comment with banned words`() {
        whenever(moderationClient.moderateComment(any()))
            .thenReturn(
                ModerationResponse(
                    UUID.randomUUID(),
                    approved = false,
                    reason = "Contém palavras proibidas: foda, puta"
                )
            )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "text": "Java é muito top, mas prefiro Kotlin; foda demais, puta que pariu!",
                        "author": "Wybson Santana"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(
                MockMvcResultMatchers.content()
                    .contentTypeCompatibleWith("application/problem+json")
            )
            .andExpect(jsonPath("$.type").value("/errors/unprocessable-entity"))
            .andExpect(jsonPath("$.title").value("Unprocessable Entity"))
            .andExpect(jsonPath("$.status").value(422))
            .andExpect(jsonPath("$.detail").value("Contém palavras proibidas: foda, puta"))
            .andExpect(jsonPath("$.instance").value("/api/comments"))
    }

    @Test
    fun `should return 504 when moderation service times out`() {
        whenever(moderationClient.moderateComment(any()))
            .thenThrow(
                ResourceAccessException(
                    "Connection refused: getsockopt",
                    SocketTimeoutException("Connection refused: getsockopt")
                )
            )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "text": "Java é muito top, mas prefiro Kotlin.",
                        "author": "Wybson Santana"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isGatewayTimeout)
            .andExpect(
                MockMvcResultMatchers.content()
                    .contentTypeCompatibleWith("application/problem+json")
            )
            .andExpect(jsonPath("$.type").value("/errors/gateway-timeout"))
            .andExpect(jsonPath("$.title").value("Gateway timeout"))
            .andExpect(jsonPath("$.status").value(504))
            .andExpect(jsonPath("$.detail").value("Connection refused: getsockopt"))
            .andExpect(jsonPath("$.instance").value("/api/comments"))
    }

    @Test
    fun `should return 502 when moderation service fails unexpectedly`() {
        whenever(moderationClient.moderateComment(any()))
            .thenThrow(ModerationClientBadGatewayException())

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "text": "Java é muito top, mas prefiro Kotlin.",
                        "author": "Wybson Santana"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isBadGateway)
            .andExpect(
                MockMvcResultMatchers.content()
                    .contentTypeCompatibleWith("application/problem+json")
            )
            .andExpect(jsonPath("$.type").value("/errors/bad-gateway"))
            .andExpect(jsonPath("$.title").value("Bad Gateway"))
            .andExpect(jsonPath("$.status").value(502))
            .andExpect(
                jsonPath("$.detail").value(
                    "An unexpected error occurred while communicating with the moderation service."
                )
            )
            .andExpect(jsonPath("$.instance").value("/api/comments"))
    }

    @Test
    fun `should return 404 when comment ID does not exist`() {
        mockMvc.perform(
            get(
                "/api/comments/{id}",
                UUID.fromString("8f2a6c91-7b43-4d5e-a019-6c8e2f5b739d")
            )
        )
            .andExpect(status().isNotFound)
    }

}
