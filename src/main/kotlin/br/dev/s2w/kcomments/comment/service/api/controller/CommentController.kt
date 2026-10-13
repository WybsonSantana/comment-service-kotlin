package br.dev.s2w.kcomments.comment.service.api.controller

import br.dev.s2w.kcomments.comment.service.api.converter.toCommentOutput
import br.dev.s2w.kcomments.comment.service.api.model.CommentInput
import br.dev.s2w.kcomments.comment.service.api.model.CommentOutput
import br.dev.s2w.kcomments.comment.service.domain.service.CommentService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/comments")
class CommentController(
    private val commentService: CommentService
) {

    @GetMapping
    fun getAllComments(pageable: Pageable): Page<CommentOutput> =
        commentService.getAllComments(pageable).map { it.toCommentOutput() }

    @GetMapping("/{id}")
    fun getCommentById(@PathVariable id: UUID): CommentOutput =
        commentService.getCommentById(id).toCommentOutput()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createComment(@RequestBody input: CommentInput): CommentOutput =
        commentService.createComment(input).toCommentOutput()


}