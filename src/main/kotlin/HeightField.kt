import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.*

class HeightField(size: Int) {

    val data = DoubleArray(size * size)
    val free = BooleanArray(size * size)
    val size = size.coerceIn(8, 4096)

    fun generate(scale: Double) {
        Arrays.fill(free, true)
        data[0] = ThreadLocalRandom.current().nextDouble(32.0)
        data[size - 1] = ThreadLocalRandom.current().nextDouble(32.0)
        data[data.size - size - 1] = ThreadLocalRandom.current().nextDouble(32.0)
        data[data.size - 1] = ThreadLocalRandom.current().nextDouble(32.0)
        divide(0, 0, size, size, (log2(size.toDouble()) - 1.0).toInt(), scale)
    }

    fun lower(px: Int, py: Int, radius: Double) {
        var y = floor(py - radius).toInt()
        while (y < py + radius) {
            if (y in 1 until size) {
                var x = floor(px - radius).toInt()
                while (x < px + radius) {
                    if (x in 1 until size) {
                        val dx = x - px.toDouble()
                        val dy = y - py.toDouble()
                        val dist = sqrt(dx * dx + dy * dy)
                        if (dist < radius) {
                            data[y * size + x] = data[y * size + x] - 2.0 * (radius - dist) / radius
                        }
                    }
                    ++x
                }
            }
            ++y
        }
    }

    fun canyonise() {
        var minHeight = Double.POSITIVE_INFINITY
        var maxHeight = Double.NEGATIVE_INFINITY
        for (datum in data) {
            minHeight = min(minHeight, datum)
            maxHeight = max(maxHeight, datum)
        }
        val range = maxHeight - minHeight
        for (y in 0 until size) {
            for (x in 0 until size) {
                val h = data[y * size + x]
                var sh = (h - minHeight) / range
                sh = sh.pow(2.0)
                sh = sh * range + minHeight
                data[y * size + x] = sh
            }
        }
    }

    fun smooth() {
        val copy = HeightField(size)
        System.arraycopy(data, 0, copy.data, 0, data.size)
        for (y in 1 until size - 1) {
            for (x in 1 until size - 1) {
                var accum = 0.0
                var accumCount = 0
                for (y2 in y - 1..y + 1) {
                    for (x2 in x - 1..x + 1) {
                        accum += copy.data[y2 * size + x2]
                        accumCount++
                    }
                }
                data[y * size + x] = accum / accumCount
            }
        }
    }

    private fun divide(sx: Int, sy: Int, ex: Int, ey: Int, step: Int, scale: Double) {
        var x2 = ex
        var y2 = ey
        if (step >= 0) {
            val cx = x2 + sx shr 1
            val cy = y2 + sy shr 1
            x2 = min(x2, size - 1)
            y2 = min(y2, size - 1)
            val h1 = data[sy * size + sx]
            val h2 = data[sy * size + x2]
            val h3 = data[y2 * size + x2]
            val h4 = data[y2 * size + sx]
            set(cx, sy, (h1 + h2) / 2.0, scale)
            set(cx, y2, (h3 + h4) / 2.0, scale)
            set(sx, cy, (h1 + h4) / 2.0, scale)
            set(x2, cy, (h2 + h3) / 2.0, scale)
            set(cx, cy, (h1 + h2 + h3 + h4) / 4.0, scale)
            divide(sx, sy, cx, cy, step - 1, scale / 2.0)
            divide(cx, sy, x2, cy, step - 1, scale / 2.0)
            divide(sx, cy, cx, y2, step - 1, scale / 2.0)
            divide(cx, cy, x2, y2, step - 1, scale / 2.0)
        }
    }

    private operator fun set(x: Int, y: Int, value: Double, scale: Double) {
        val i = y * size + x
        if (free[i]) {
            free[i] = false
            data[i] = value + ThreadLocalRandom.current().nextDouble(-scale, scale)
        }
    }
}
