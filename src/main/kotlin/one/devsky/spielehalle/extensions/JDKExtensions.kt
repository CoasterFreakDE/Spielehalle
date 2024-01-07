package one.devsky.spielehalle.extensions

import org.slf4j.LoggerFactory
import java.awt.Color

fun <T : Any> T.getLogger(): org.slf4j.Logger {
    return LoggerFactory.getLogger(this::class.java)
}

fun <T : Any> T.nullIf(condition: (T) -> Boolean): T? {
    return if (condition(this)) null else this
}

fun Boolean?.toInt(): Int {
    return if (this == true) 1 else 0
}


operator fun Color.plus(other: Color): Color {
    return Color(
        ((this.red + other.red) / 2).coerceAtMost(255),
        ((this.green + other.green) / 2).coerceAtMost(255),
        ((this.blue + other.blue) / 2).coerceAtMost(255)
    )
}

fun Int.times(text: String): String {
    val builder = StringBuilder()
    repeat(this) {
        builder.append(text)
    }
    return builder.toString()
}

fun Int.times(function: (Int) -> Unit) {
    repeat(this) {
        function(it)
    }
}

fun zeroArray(size: Int): Array<Int> {
    val array = arrayOfNulls<Int>(size)
    array.fill(0)
    return array.requireNoNulls()
}