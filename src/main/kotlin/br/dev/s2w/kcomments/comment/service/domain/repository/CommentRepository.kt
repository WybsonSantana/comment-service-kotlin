package br.dev.s2w.kcomments.comment.service.domain.repository

import br.dev.s2w.kcomments.comment.service.domain.model.Comment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CommentRepository : JpaRepository<Comment, UUID>