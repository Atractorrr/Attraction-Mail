package attraction.run.html

import attraction.run.article.Article
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

object HTMLHandler {

    private val EXTENSIONS = listOf(".jpg", ".jpeg", ".png")
    private const val WORDS_PER_MINUTE = 200
    private const val MAX_CONTENT_LENGTH = 200
    private const val THUMBNAIL_IMG_INDEX = 1
    private const val DEFAULT_IMG_INDEX = 0

    fun extractArticleFromHtmlContent(articles: List<Article>): List<Article> {
        return articles.map { article ->
            val elements = Jsoup.parse(article.contentHTML).body()
            val textContent = elements.text()

            article.apply {
                this.thumbnailUrl = getThumbnailUrl(elements)
                this.contentSummary = getContentSummaryFromText(textContent)
                this.readingTime = calculateReadingTimeFromText(textContent)
            }
        }
    }

    private fun getThumbnailUrl(body: Element): String {
        val imgTags = body.getElementsByTag("img")

        val images = imgTags.map { it.attr("src") }
                .filter { url ->
                    EXTENSIONS.any { url.endsWith(it, ignoreCase = true) }
                }.take(2)

        return when (images.size) {
            2 -> images[THUMBNAIL_IMG_INDEX]
            1 -> images[DEFAULT_IMG_INDEX]
            else -> ""
        }
    }

    private fun getContentSummaryFromText(textContent: String) =
            if (MAX_CONTENT_LENGTH > textContent.length) textContent else extractSummary(textContent)

    private fun extractSummary(fullText: String): String {
        val sentenceDelimiter = ". "
        val startPoint = fullText.indexOf(sentenceDelimiter) + sentenceDelimiter.length
        val summaryEndPoint = MAX_CONTENT_LENGTH + startPoint + sentenceDelimiter.length

        return fullText.substring(startPoint until summaryEndPoint)
    }

    // 사람은 1분에 약 150 ~ 200단어를 읽을 수 있다. (불용어 제외X)
    private fun calculateReadingTimeFromText(fullText: String) = fullText.split(" ").size / WORDS_PER_MINUTE
}