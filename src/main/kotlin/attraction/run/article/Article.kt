package attraction.run.article

import jakarta.persistence.*
import java.time.LocalDate
import kotlin.jvm.Transient

@Entity
class Article(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(nullable = false)
        val title: String,
        @Column(nullable = false)
        val newsLetterEmail: String,
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
    @Column(nullable = false)
    lateinit var createdAt: LocalDate

    fun isSameUserEmail(userEmail: String) = this.userEmail == userEmail
    override fun toString(): String {
        return "Article(id=$id, title='$title', newsLetterEmail='$newsLetterEmail', userEmail='$userEmail', contentHTML='$contentHTML', receivedAt=$receivedAt)"
    }


}