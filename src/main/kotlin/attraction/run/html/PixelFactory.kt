package attraction.run.html

import com.sksamuel.scrimage.pixels.Pixel
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte

object PixelFactory {
    fun getPixelArrayFromImage(image: BufferedImage): Array<Pixel> {
        val width = image.width
        val height = image.height
        val hasAlphaChannel = image.alphaRaster != null
        val data = (image.raster.dataBuffer as DataBufferByte).getData()

        if (hasAlphaChannel) {
            return getPixelsWithAlphaChannel(data, width, height)
        }
        return getPixels(data, width, height)
    }

    private fun getPixelsWithAlphaChannel(
            data: ByteArray,
            width: Int,
            height: Int
    ): Array<Pixel> {
        val result = Array(height * width) { Pixel(0, 0, 0) }

        val pixelLength = 4
        var index = 0
        for (pixel in data.indices step pixelLength) {
            if (pixel + 3 >= data.size) break

            var argb = 0
            argb += (data[pixel].toInt() and 0xff shl 24) // alpha
            argb += (data[pixel + 1].toInt() and 0xff) // blue
            argb += (data[pixel + 2].toInt() and 0xff shl 8) // green
            argb += (data[pixel + 3].toInt() and 0xff shl 16) // red

            val row = index / width
            val col = index % width

            result[index] = Pixel(col, row, argb)
            index++
        }
        return result
    }

    private fun getPixels(
            data: ByteArray,
            width: Int,
            height: Int
    ): Array<Pixel> {
        val result = Array(height * width) { Pixel(0, 0, 0) }

        val pixelLength = 3
        var index = 0
        for (pixel in data.indices step pixelLength) {
            if (pixel + 2 >= data.size) break

            var argb = 0
            argb += -16777216 // 255 alpha
            argb += (data[pixel].toInt() and 0xff) // blue
            argb += (data[pixel + 1].toInt() and 0xff shl 8) // green
            argb += (data[pixel + 2].toInt() and 0xff shl 16) // red

            val row = index / width
            val col = index % width

            result[index] = Pixel(col, row, argb)
            index++
        }
        return result
    }

}