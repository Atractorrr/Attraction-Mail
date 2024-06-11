package attraction.run.html

import attraction.run.s3.S3Service
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.webp.WebpWriter
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.File
import java.net.URI
import java.util.*
import javax.imageio.ImageIO

@Service
class ThumbnailUrlParser(
        @Value("\${file.thumbnail-path}")
        private val path: String,
        private val s3Service: S3Service
) {
    private companion object {
        private val EXTENSIONS = listOf(".jpg", ".jpeg", ".png")
        private val IMAGE_ASPECT_RATIO_RANGE = 56.25..100.00
        private const val TARGET_WIDTH = 720
        private const val WEBP_SUFFIX = ".webp"
    }

    fun getThumbnailUrl(thumbnailUrls: List<String>): String {
        initFilePath()
        val bufferImages = getBufferImages(thumbnailUrls)
        val images = bufferImages.filter(::imageRule).take(2)

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

    private fun getBufferImages(thumbnailUrls: List<String>): List<BufferedImage> {
        return thumbnailUrls.filter {
            EXTENSIONS.any { it.endsWith(it, ignoreCase = true) }
        }.map {
            ImageIO.read(URI(it).toURL())
        }
    }

    private fun imageRule(it: BufferedImage) =
            it.height >= 300 && (it.height.toDouble() / it.width) * 100 in IMAGE_ASPECT_RATIO_RANGE

    private fun imageResizeAndConvertWebp(image: BufferedImage): String {
        val immutableImage = getImmutableImage(image)
                .scaleToWidth(TARGET_WIDTH)
                .output(WebpWriter.DEFAULT, File("$path/${UUID.randomUUID()}$WEBP_SUFFIX"))

        return immutableImage.path
    }

    private fun getImmutableImage(image: BufferedImage): ImmutableImage =
            ImmutableImage.create(
                    image.width,
                    image.height,
                    PixelFactory.getPixelArrayFromImage(image),
                    BufferedImage.TYPE_3BYTE_BGR
            )
}