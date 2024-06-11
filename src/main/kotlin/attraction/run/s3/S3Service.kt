package attraction.run.s3

import attraction.run.article.Article
import io.awspring.cloud.s3.ObjectMetadata
import io.awspring.cloud.s3.S3Operations
import io.awspring.cloud.s3.S3Resource
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.MimeTypeUtils.TEXT_HTML_VALUE
import java.io.File
import java.util.*

@Service
class S3Service(
        @Value("\${aws.s3.bucket}")
        private val bucket: String,
        @Value("\${file.mail-path}")
        private val path: String,
        private val s3Operations: S3Operations
) {
    fun initFilePath() {
        val file = File(path)
        if (!file.exists()) {
            file.mkdir()
        }
    }

    fun uploadAllArticle(articles: List<Article>): List<Article> {
        return articles.onEach(::uploadArticle)
    }

    private fun uploadArticle(article: Article): Article {
        val file = createFileFromHTMLContent(article.contentHTML)
        val upload = uploadFileToS3(file)

        if (!uploadUrlContainsFileName(upload, file)) {
            throw IllegalArgumentException("파일 이름이 올바르지 않습니다.")
        }

        return article.apply {
            file.delete()
            this.contentUrl = file.name
        }
    }

    private fun createFileFromHTMLContent(htmlContent: String): File {
        val filePath = "$path/${UUID.randomUUID()}.html"
        val file = File(filePath)

        return file.apply {
            writeText(htmlContent)
        }
    }

    private fun uploadFileToS3(file: File) = s3Operations
            .upload(
                    bucket,
                    file.name,
                    file.inputStream(),
                    ObjectMetadata.builder()
                            .contentType(TEXT_HTML_VALUE)
                            .build()
            )

    private fun uploadUrlContainsFileName(upload: S3Resource, file: File) = upload.url.toString().contains(file.name)
}