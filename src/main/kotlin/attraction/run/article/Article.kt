package attraction.run.article

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.jvm.Transient

@Entity
@EntityListeners(AuditingEntityListener::class)
class Article(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(nullable = false)
        val title: String,
        @Column(nullable = false)
        val newsletterEmail: String,
        @Column(nullable = false)
        val newsletterNickname: String,
        @Column(nullable = false)
        val userEmail: String,
        @Transient
        val contentHTML: String,
        @Column(nullable = false)
        val receivedAt: LocalDate
) {
    @Column(nullable = false, columnDefinition = "TINYINT(1) default 0")
    val isDeleted = false
    @Column(nullable = false)
    lateinit var thumbnailUrl: String
    @Column(nullable = false)
    lateinit var contentUrl: String
    @Column(nullable = false)
    var readingTime: Int? = null
    @Column(nullable = false)
    lateinit var contentSummary: String

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    fun isSameUserEmail(userEmail: String) = this.userEmail == userEmail
}