package attraction.run.article

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.jvm.Transient

@Table(name = "article")
@Entity
@EntityListeners(AuditingEntityListener::class)
class Article(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(nullable = false)
        val title: String,
        @Column(name = "newsletter_email", nullable = false)
        val newsletterEmail: String,
        @Column(name = "newsletter_nickname", nullable = false)
        val newsletterNickname: String,
        @Column(name = "user_email", nullable = false)
        val userEmail: String,
        @Transient
        val contentHTML: String,
        @Column(name = "received_at", nullable = false)
        val receivedAt: LocalDate
) {
    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1) default 0")
    val isDeleted = false

    @Column(name = "thumbnail_url", nullable = false)
    lateinit var thumbnailUrl: String

    @Column(name = "content_url", nullable = false)
    lateinit var contentUrl: String

    @Column(name = "reading_time", nullable = false)
    var readingTime: Int? = null

    @Column(name = "content_summary", nullable = false, length = 200)
    lateinit var contentSummary: String

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @LastModifiedDate
    @Column(name = "modified_at", nullable = false, updatable = false)
    var modifiedAt: LocalDateTime = LocalDateTime.now()

    fun isSameUserEmail(userEmail: String) = this.userEmail == userEmail
    override fun toString(): String {
        return """
            AdminArticle(
                title='$title', newsletterEmail='$newsletterEmail', newsletterNickname='$newsletterNickname', 
                userEmail='$userEmail', receivedAt=$receivedAt, isDeleted=$isDeleted, thumbnailUrl='$thumbnailUrl', 
                contentUrl='$contentUrl', readingTime=$readingTime, contentSummary='$contentSummary', 
                createdAt=$createdAt, modifiedAt=$modifiedAt
            )
        """.trimIndent()
    }


}