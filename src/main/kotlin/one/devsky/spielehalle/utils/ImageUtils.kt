package one.devsky.spielehalle.utils

import dev.fruxz.ascend.extension.getResource
import net.dv8tion.jda.api.entities.Guild
import java.awt.*
import java.awt.geom.Ellipse2D.Double
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import javax.imageio.ImageIO

object ImageUtils {


    fun generateBanner(guild: Guild, boosterAvatarUrls: List<String>): ByteArray {
        val avatars = boosterAvatarUrls.map { ImageIO.read(URI(it).toURL()) }

        // Set image dimensions
        val width = 960
        val height = 540

        val iWidth = width - 60

        // Create a BufferedImage
        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)


        // Get the graphics context
        val g2d = bufferedImage.createGraphics()

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)


        // register font
        val fontFile = File(getResource("assets/fonts/MediaPro.ttf").toString())
        val mediaPro = Font.createFont(Font.TRUETYPE_FONT, fontFile)

        // Bilder hinzufügen
        val bgImage = ImageIO.read(File(getResource("assets/banner/banner.png").toString()))

        g2d.drawImage(bgImage, 0, 0, null)

        // Verdunkeltes Rechteck für Hintergrund
        g2d.color = Color(0, 0, 0, 200)
        g2d.fillRoundRect(0, height - 136, width, 136, 50, 0)

        g2d.translate(30, 30)

        // Set font and color for text
        val font = mediaPro.deriveFont(80f)
        g2d.font = font
        g2d.color = Color.WHITE

        drawText(g2d, "${guild.members.size} Mitglieder", 28, iWidth, height - 140, true)
        drawText(g2d, "Ein riesen Danke an unsere Booster <3", 28, 10, height - 140)

        val avatarWidth = 64
        var avatarX = 20
        var avatarY = height - 100
        avatars.forEach { avatar ->
            val scaledAvatar = avatar.getScaledInstance(avatarWidth, avatarWidth, Image.SCALE_SMOOTH).toBufferedImage()
            val roundAvatar = roundAvatar(scaledAvatar)
            g2d.drawImage(roundAvatar, avatarX, avatarY, null)

            if (avatarX + avatarWidth > width) {
                avatarX = 20
                avatarY += avatarWidth + 5
                return@forEach
            }

            avatarX += avatarWidth + 5
        }

        // Dispose of the graphics context
        g2d.dispose()

        // Convert BufferedImage to ByteArray
        val byteArrayOutputStream = ByteArrayOutputStream()
        ImageIO.write(bufferedImage, "png", byteArrayOutputStream)

        return byteArrayOutputStream.toByteArray()
    }

    private fun drawText(g2d: Graphics2D, text: String, fontSize: Int, x: Int, y: Int, leftBounded: Boolean = false) {
        val font = g2d.font.deriveFont(fontSize.toFloat())
        g2d.font = font
        val fm = g2d.fontMetrics
        val textWidth = fm.stringWidth(text)
        g2d.drawString(text, x - (if(leftBounded) textWidth else 0), y + (fm.ascent - fm.descent - fm.leading) / 2)
    }

    fun roundAvatar(image: BufferedImage): BufferedImage {
        val diameter = Math.min(image.width, image.height)

        val mask = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
        val g2d = mask.createGraphics()
        g2d.fill(Double(0.0, 0.0, diameter.toDouble(), diameter.toDouble()))
        g2d.dispose()

        val masked = BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB)
        val dstG2d = masked.createGraphics()
        dstG2d.drawImage(image, 0, 0, diameter, diameter, null)
        dstG2d.dispose()

        // Apply the mask
        for (y in 0 until masked.height) {
            for (x in 0 until masked.width) {
                val maskPixel = mask.getRGB(x, y)
                if (maskPixel == 0) {
                    val imgPixel = masked.getRGB(x, y)
                    val resultPixel = imgPixel and maskPixel
                    masked.setRGB(x, y, resultPixel)
                }
            }
        }
        return masked
    }

    fun Image.toBufferedImage(): BufferedImage {
        val bufferedImage = BufferedImage(this.getWidth(null), this.getHeight(null), BufferedImage.TYPE_INT_ARGB)
        val g2 = bufferedImage.createGraphics()
        g2.drawImage(this, 0, 0, null)
        g2.dispose()
        return bufferedImage
    }
}