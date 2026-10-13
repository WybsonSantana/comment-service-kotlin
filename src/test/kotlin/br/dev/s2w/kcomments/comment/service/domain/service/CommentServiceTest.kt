package br.dev.s2w.kcomments.comment.service.domain.service

import br.dev.s2w.kcomments.comment.service.api.client.ModerationClient
import br.dev.s2w.kcomments.comment.service.api.model.CommentInput
import br.dev.s2w.kcomments.comment.service.api.model.ModerationRequest
import br.dev.s2w.kcomments.comment.service.api.model.ModerationResponse
import br.dev.s2w.kcomments.comment.service.domain.exception.CommentNotFoundException
import br.dev.s2w.kcomments.comment.service.domain.exception.ModerationRejectedException
import br.dev.s2w.kcomments.comment.service.domain.model.Comment
import br.dev.s2w.kcomments.comment.service.domain.repository.CommentRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class CommentServiceTest {

    @Mock
    private lateinit var commentRepository: CommentRepository

    @Mock
    private lateinit var moderationClient: ModerationClient

    @InjectMocks
    private lateinit var commentService: CommentService

    @Test
    fun `should return paginated comments`() {
        val pageable = PageRequest.of(0, 10)
        val expectedPage: Page<Comment> = PageImpl(emptyList(), pageable, 0)

        whenever(commentRepository.findAll(pageable))
            .thenReturn(expectedPage)

        val actualPage = commentService.getAllComments(pageable)

        assertSame(expectedPage, actualPage)
        verify(commentRepository).findAll(pageable)
    }

    @Test
    fun `should return a comment when ID exists`() {
        val comment = createComment()

        whenever(commentRepository.findById(comment.id))
            .thenReturn(Optional.of(comment))

        val actualComment = commentService.getCommentById(comment.id)

        assertSame(comment, actualComment)
        verify(commentRepository).findById(comment.id)
    }

    @Test
    fun `should create a comment when moderation approves it`() {
        val input = createCommentInput()

        whenever(moderationClient.moderateComment(any<ModerationRequest>()))
            .thenReturn(createModerationResponse(approved = true, reason = APPROVED_REASON))

        whenever(commentRepository.saveAndFlush(any<Comment>()))
            .thenAnswer { invocation -> invocation.getArgument<Comment>(0) }

        val actualComment = commentService.createComment(input)

        assertEquals(input.text, actualComment.text)
        assertEquals(input.author, actualComment.author)
        verify(moderationClient).moderateComment(any<ModerationRequest>())
        verify(commentRepository).saveAndFlush(any<Comment>())
    }

    @Test
    fun `should throw an exception when comment ID does not exist`() {
        val id = UUID.randomUUID()

        whenever(commentRepository.findById(id))
            .thenReturn(Optional.empty())

        assertThrows(CommentNotFoundException::class.java) {
            commentService.getCommentById(id)
        }

        verify(commentRepository).findById(id)
    }

    @Test
    fun `should reject a comment when moderation rejects it`() {
        val input = createCommentInput(text = REJECTED_COMMENT_TEXT)

        whenever(moderationClient.moderateComment(any<ModerationRequest>()))
            .thenReturn(createModerationResponse(approved = false, reason = REJECTED_REASON))

        assertThrows(ModerationRejectedException::class.java) {
            commentService.createComment(input)
        }

        verify(moderationClient).moderateComment(any<ModerationRequest>())
        verify(commentRepository, never()).saveAndFlush(any<Comment>())
    }

    private fun createCommentInput(
        text: String = DEFAULT_COMMENT_TEXT,
        author: String = DEFAULT_AUTHOR
    ) = CommentInput(
        text = text,
        author = author
    )

    private fun createComment(
        id: UUID = COMMENT_ID,
        text: String = DEFAULT_COMMENT_TEXT,
        author: String = DEFAULT_AUTHOR
    ) = Comment(
        id = id,
        text = text,
        author = author
    )

    private fun createModerationResponse(
        approved: Boolean,
        reason: String
    ) = ModerationResponse(
        commentId = COMMENT_ID,
        approved = approved,
        reason = reason
    )

    private companion object {
        val COMMENT_ID: UUID = UUID.randomUUID()

        const val DEFAULT_COMMENT_TEXT =
            "Java é muito top, mas prefiro Kotlin."
        const val DEFAULT_AUTHOR =
            "Wybson Santana"
        const val APPROVED_REASON =
            "Comentário aprovado: nenhuma palavra proibida encontrada."
        const val REJECTED_REASON =
            "Contém palavras proibidas: foda"
        const val REJECTED_COMMENT_TEXT =
            "Java é muito top, mas prefiro Kotlin; foda demais!"
    }

}
