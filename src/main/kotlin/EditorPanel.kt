import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import javax.swing.JPanel
import kotlin.math.floor
import kotlin.math.min

class EditorPanel : JPanel() {
    private val image = BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB_PRE)
    private val pixels = (image.raster.dataBuffer as DataBufferInt).data
    private val terrain: HeightField
    private val lakeGenerator: LakeGenerator

    var mode: EditMode = EditMode.ERODE

    fun generate() {
        lakeGenerator.reset()
        terrain.generate(200.0)
        repaint()
    }

    fun smooth() {
        lakeGenerator.reset()
        terrain.smooth()
        repaint()
    }

    fun canyonise() {
        lakeGenerator.reset()
        terrain.canyonise()
        repaint()
    }

    private fun erode(x: Int, y: Int, radius: Double) {
        val rect = computeImageBounds()
        if (rect.contains(x, y)) {
            val point = pointToImageSpace(x, y)
            terrain.lower(point.x, point.y, radius)
            repaint()
        }
    }

    private fun generateLake(x: Int, y: Int) {
        val rect = computeImageBounds()
        if (rect.contains(x, y)) {
            val point = pointToImageSpace(x, y)
            lakeGenerator.generate(point.x, point.y)
            repaint()
        }
    }

    override fun paintComponent(g: Graphics) {
        updatePixels()
        val g2 = g as Graphics2D
        val imgRect = computeImageBounds()
        val sy = imgRect.width / image.width.toDouble()
        val transform = AffineTransform.getScaleInstance(sy, sy)
        g2.drawImage(image, AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR), imgRect.x, imgRect.y)
    }

    private fun computeImageBounds(): Rectangle {
        val f = min(width, height) / image.width.toDouble()
        val w = f * image.width
        val h = f * image.height
        return Rectangle(((width - w) / 2.0).toInt(), ((height - h) / 2.0).toInt(), w.toInt(), h.toInt())
    }

    private fun pointToImageSpace(x: Int, y: Int): Point {
        val rect = computeImageBounds()
        val tx = (x - rect.x) / rect.width.toDouble() * image.width
        val ty = (y - rect.y) / rect.height.toDouble() * image.height
        return Point(tx.toInt(), ty.toInt())
    }

    private fun updatePixels() {
        for (i in pixels.indices) {
            val index = (if (lakeGenerator.water[i]) 0.0 else terrain.data[i]).coerceIn(0.0, 255.0).toInt()
            pixels[i] = COLORS[index]
        }
    }

    init {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        size = Dimension(screenSize.width * 80 / 100, screenSize.height * 80 / 100)
        preferredSize = size

        terrain = HeightField(1024)
        terrain.generate(200.0)
        lakeGenerator = LakeGenerator(terrain)
        lakeGenerator.refresh = Runnable { this.repaint() }

        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                super.mousePressed(e)
                if (mode == EditMode.LAKE) generateLake(e.x, e.y)
            }
        })

        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                super.mouseDragged(e)
                if (mode == EditMode.ERODE) erode(e.x, e.y, 50.0)
            }
        })
    }

    companion object {

        private val COLORS = makePalette(
            intArrayOf(
                0x2185c5, 0x238cca, 0x2c92cf, 0x3598d4,
                0x3c9ed8, 0x45a4dd, 0x4eaae2, 0xffffa8,
                0xbef272, 0xb3e866, 0xa8dd5b, 0x9ed350,
                0x93c845, 0x88be39, 0x7eb52f, 0x8f8241,
                0x857833, 0x7a6c27, 0x6f621b, 0x64560f,
                0x584b04, 0x4d3f00, 0xcacaca, 0xd5d5d5,
                0xe0e0e0
            )
        )

        private fun makePalette(markers: IntArray): IntArray {
            val colors = IntArray(256)
            var eR = (0xFF0000 and markers[0] shr 16).toDouble()
            var eG = (0x00FF00 and markers[0] shr 8).toDouble()
            var eB = (0x0000FF and markers[0]).toDouble()
            val step = 256.0 / (markers.size - 1)
            for (j in 1 until markers.size) {
                var sR = eR
                var sG = eG
                var sB = eB
                eR = (0xFF0000 and markers[j] shr 16).toDouble()
                eG = (0x00FF00 and markers[j] shr 8).toDouble()
                eB = (0x0000FF and markers[j]).toDouble()
                val dr = (eR - sR) / step
                val dg = (eG - sG) / step
                val db = (eB - sB) / step
                var i = 0
                while (i < step) {
                    val r = floor(sR).toInt().coerceIn(0, 255)
                    val g = floor(sG).toInt().coerceIn(0, 255)
                    val b = floor(sB).toInt().coerceIn(0, 255)
                    colors[(floor((j - 1).toDouble()) * step + i).toInt()] = 255 shl 24 or (r shl 16) or (g shl 8) or b
                    sR += dr
                    sG += dg
                    sB += db
                    ++i
                }
            }
            return colors
        }
    }
}
