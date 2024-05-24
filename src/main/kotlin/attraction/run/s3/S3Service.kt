package attraction.run.s3

import attraction.run.article.Article
import io.awspring.cloud.s3.ObjectMetadata
import io.awspring.cloud.s3.S3Operations
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.MimeTypeUtils.TEXT_HTML_VALUE
import java.io.File
import java.util.*

@Service
class S3Service(
        @Value("\${aws.s3.bucket}")
        private val bucket: String,
        @Value("\${file.path}")
        private val path: String,
        private val s3Operations: S3Operations
) {
    fun initFilePath() {
        val file = File(path)
        if (!file.exists()) {
            file.mkdir()
        }
    }

    fun upload(articles: List<Article>): List<Article> {
        return articles.onEach(::articleUpload)
    }

    private fun articleUpload(article: Article): Article {
        val file = createFileFromHTMLContent(article.contentHTML)

        val upload = s3Operations.upload(bucket, file.name, file.inputStream(),
                ObjectMetadata.builder()
                        .contentType(TEXT_HTML_VALUE)
                        .build())
        val uploadUrl = upload.url.toString()

        return article.apply {
            file.delete()
            this.contentUrl = uploadUrl
        }
    }

    private fun createFileFromHTMLContent(htmlContent: String): File {
        val filePath = "$path/${UUID.randomUUID()}.html"
        val file = File(filePath)

        file.writeText(htmlContent)
        return file
    }
}