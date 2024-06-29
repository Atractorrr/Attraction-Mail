package attraction.run.html

import com.sksamuel.scrimage.ImmutableImage
import java.awt.image.BufferedImage

class ThumbnailImg(
        private val image: BufferedImage
) {
    fun hasMinimumImageSize(size: Int): Boolean {
        println("""
            width = ${image.width} 
            height = ${image.height}
            image.width >= size = ${image.width >= size}
            image.height >= size = ${image.height >= size}
        """.trimIndent())
        return image.width >= size && image.height >= size
    }

    fun hasAspectRatioRange(range: ClosedRange<Double>): Boolean {
        println("""
            퍼센트 = ${(image.height.toDouble() / image.width) * 100}
            result = ${(image.height.toDouble() / image.width) * 100 in range}
        """.trimIndent())
        return (image.height.toDouble() / image.width) * 100 in range
    }

    fun isImageSizeSmallerWidth(size: Int): Boolean {
        return image.width < size
    }

    fun getImmutableImage(): ImmutableImage =
            ImmutableImage.create(
                    image.width,
                    image.height,
                    PixelFactory.getPixelArrayFromImage(image),
                    BufferedImage.TYPE_3BYTE_BGR
            )

    fun getImmutableImageWithScaleToWidth(width: Int): ImmutableImage = getImmutableImage().scaleToWidth(width)

}