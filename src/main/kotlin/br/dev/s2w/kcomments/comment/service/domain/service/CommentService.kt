package br.dev.s2w.kcomments.comment.service.domain.service

import br.dev.s2w.kcomments.comment.service.api.client.ModerationClient
import br.dev.s2w.kcomments.comment.service.api.model.CommentInput
import br.dev.s2w.kcomments.comment.service.api.model.ModerationRequest
import br.dev.s2w.kcomments.comment.service.domain.exception.CommentNotFoundException
import br.dev.s2w.kcomments.comment.service.domain.exception.ModerationRejectedException
import br.dev.s2w.kcomments.comment.service.domain.model.Comment
import br.dev.s2w.kcomments.comment.service.domain.repository.CommentRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val moderationClient: ModerationClient
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun getAllComments(pageable: Pageable): Page<Comment> {
        log.info("Fetching comments")
        val comments = commentRepository.findAll(pageable)

        log.info("Comments fetched successfully. Total elements: ${comments.totalElements}")
        return comments
    }

    fun getCommentById(id: UUID): Comment {
        log.info("Fetching comment with ID: $id")
        val comment = commentRepository.findById(id)
            .orElseThrow { CommentNotFoundException() }

        log.info("Comment found successfully. ID: ${comment.id}")
        return comment
    }

    fun createComment(input: CommentInput): Comment {
        log.info("Starting comment creation")
        val comment = Comment(text = input.text, author = input.author)

        log.info("Sending comment for moderation. ID: ${comment.id}")
        val request = ModerationRequest(comment.id, input.text)
        val response = moderationClient.moderateComment(request)

        if (!response.isApproved()) {
            log.info("Comment moderation rejected. ID: ${comment.id}, Reason: ${response.reason}")
            throw ModerationRejectedException(response.reason)
        }

        log.info("Comment moderation approved. Saving comment. ID: ${comment.id}")
        val savedComment = commentRepository.saveAndFlush(comment)

        log.info("Comment created successfully. ID: ${savedComment.id}")
        return savedComment
    }


}