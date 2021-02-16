import java.util.*
import javax.swing.SwingUtilities

class LakeGenerator(private val heightField: HeightField) {

    var water = BooleanArray(heightField.size * heightField.size)
    var refresh: Runnable? = null

    private var thread: Thread? = null
    private var landLocked = false

    fun reset() {
        stop()
        Arrays.fill(water, false)
    }

    fun generate(x: Int, y: Int) {
        stop()
        landLocked = true
        thread = Thread {
            while (landLocked) {
                dissolve(x, y, 0)
            }
            if (refresh != null) {
                SwingUtilities.invokeLater(refresh)
            }
        }
        thread!!.priority = Thread.MIN_PRIORITY
        thread!!.start()
    }

    private fun stop() {
        landLocked = false
        if (thread != null && thread!!.isAlive) {
            try {
                thread!!.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun dissolve(x: Int, y: Int, counter: Int) {
        val size = heightField.size
        if (x <= 0 || y <= 0 || x >= size - 1 || y >= size - 1) {
            landLocked = false
        } else {
            val data = heightField.data
            val xm = x - 1
            val xp = x + 1
            val ym = y - 1
            val yp = y + 1
            val h = data[y * size + x]
            val h1 = h - data[ym * size + xm]
            val h2 = h - data[ym * size + x]
            val h3 = h - data[ym * size + xp]
            val h4 = h - data[y * size + xm]
            val h5 = h - data[y * size + xp]
            val h6 = h - data[yp * size + xm]
            val h7 = h - data[yp * size + x]
            val h8 = h - data[yp * size + xp]
            if ((h1 <= 0) and (h2 <= 0) and (h3 <= 0) and (h4 <= 0) and (h5 <= 0) and (h6 <= 0) and (h7 <= 0) and (h8 <= 0)) {
                data[y * size + x] = h + 1.0
                water[y * size + x] = true
                if (refresh != null && counter % 50 == 0) {
                    SwingUtilities.invokeLater(refresh)
                }
            } else {
                var idx = -1
                var l = Double.NEGATIVE_INFINITY
                if (h1 > l) {
                    l = h1
                    idx = 0
                }
                if (h2 > l) {
                    l = h2
                    idx = 1
                }
                if (h3 > l) {
                    l = h3
                    idx = 2
                }
                if (h4 > l) {
                    l = h4
                    idx = 3
                }
                if (h5 > l) {
                    l = h5
                    idx = 4
                }
                if (h6 > l) {
                    l = h6
                    idx = 5
                }
                if (h7 > l) {
                    l = h7
                    idx = 6
                }
                if (h8 > l) {
                    idx = 7
                }
                when (idx) {
                    0 -> {
                        water[ym * size + xm] = true
                        data[ym * size + xm] -= 0.01
                        dissolve(xm, ym, counter + 1)
                    }
                    1 -> {
                        water[ym * size + x] = true
                        data[ym * size + x] -= 0.01
                        dissolve(x, ym, counter + 1)
                    }
                    2 -> {
                        water[ym * size + xp] = true
                        data[ym * size + xp] -= 0.01
                        dissolve(xp, ym, counter + 1)
                    }
                    3 -> {
                        water[y * size + xm] = true
                        data[y * size + xm] -= 0.01
                        dissolve(xm, y, counter + 1)
                    }
                    4 -> {
                        water[y * size + xp] = true
                        data[y * size + xp] -= 0.01
                        dissolve(xp, y, counter + 1)
                    }
                    5 -> {
                        water[yp * size + xm] = true
                        data[yp * size + xm] -= 0.01
                        dissolve(xm, yp, counter + 1)
                    }
                    6 -> {
                        water[yp * size + x] = true
                        data[yp * size + x] -= 0.01
                        dissolve(x, yp, counter + 1)
                    }
                    7 -> {
                        water[yp * size + xp] = true
                        data[yp * size + xp] -= 0.01
                        dissolve(xp, yp, counter + 1)
                    }
                }
            }
        }
    }
}
