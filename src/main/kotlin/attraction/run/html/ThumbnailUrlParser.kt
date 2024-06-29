package attraction.run.html

import attraction.run.s3.S3Service
import com.sksamuel.scrimage.webp.WebpWriter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.net.URI
import java.util.*
import javax.imageio.ImageIO


@Component
class ThumbnailUrlParser(
        @Value("\${file.thumbnail-path}")
        private val path: String,
        private val s3Service: S3Service
) {
    private companion object {
        private val EXTENSIONS = listOf(".jpg", ".jpeg", ".png")
        private val IMAGE_ASPECT_RATIO_RANGE = 30.00..120.00
        private const val TARGET_WIDTH = 720
        private const val WEBP_SUFFIX = ".webp"
    }

    private val log = LoggerFactory.getLogger(this.javaClass)!!

    fun getThumbnailUrl(thumbnailUrls: List<String>): String {
        initFilePath()
        val bufferImages = getBufferImages(thumbnailUrls)
        val images = bufferImages.filter(::imageRule).take(2)
        log.info("size = ${images.size}")
        return when (images.size) {
            1 -> imageResizeAndConvertWebp(images[0])
            2 -> imageResizeAndConvertWebp(images[1])
            else -> ""
        }
    }

    fun initFilePath() {
        val file = File(path)
        if (!file.exists()) {
            file.mkdir()
        }
    }

    private fun getBufferImages(thumbnailUrls: List<String>): List<ThumbnailImg> {
        return thumbnailUrls.filter { url ->
            EXTENSIONS.any {
                val lastIndexOf = url.lastIndexOf(it, ignoreCase = true)
                lastIndexOf != -1
            }
        }.map {
            log.info("image url = $it")
            ThumbnailImg(ImageIO.read(URI(it).toURL()))
        }
    }

    private fun imageRule(it: ThumbnailImg) =
            it.hasMinimumImageSize(300) && it.hasAspectRatioRange(IMAGE_ASPECT_RATIO_RANGE)

    private fun imageResizeAndConvertWebp(thumbnailImg: ThumbnailImg): String {
        val convertImage = if (thumbnailImg.isImageSizeSmallerWidth(TARGET_WIDTH)) {
            thumbnailImg.getImmutableImage()
        } else {
            thumbnailImg.getImmutableImageWithScaleToWidth(TARGET_WIDTH)
        }.output(WebpWriter.DEFAULT, File("$path/${UUID.randomUUID()}$WEBP_SUFFIX"))

        s3Service.uploadThumbnailImg(convertImage)
        convertImage.delete()
        return convertImage.name
    }
}