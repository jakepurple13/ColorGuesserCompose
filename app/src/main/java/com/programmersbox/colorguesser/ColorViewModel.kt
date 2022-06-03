package com.programmersbox.colorguesser

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.ViewModel
import kotlin.math.min
import kotlin.random.Random

private const val RGB_MAX = 255
private const val CMYK_MAX = 100

enum class GameState { Play, Restart }

class ColorViewModel : ViewModel() {

    var state by mutableStateOf(GameState.Play)
    var scoreReset by mutableStateOf(0)

    var color by mutableStateOf(Random.nextColor(a = 255))
    var currentScore by mutableStateOf(0)

    var hexValue by mutableStateOf("")

    var rValue by mutableStateOf("")
    var gValue by mutableStateOf("")
    var bValue by mutableStateOf("")

    var cValue by mutableStateOf("")
    var mValue by mutableStateOf("")
    var yValue by mutableStateOf("")
    var kValue by mutableStateOf("")

    val rgbColor
        get() = if (rValue.isNotEmpty() && gValue.isNotEmpty() && bValue.isNotEmpty()) {
            Color(rValue.toInt(), gValue.toInt(), bValue.toInt())
        } else null

    val hexColor
        get() = try {
            val c: Int = android.graphics.Color.parseColor("#$hexValue")
            Color(c)
        } catch (e: NumberFormatException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }

    val cmykColor
        get() = if (cValue.isNotEmpty() && mValue.isNotEmpty() && yValue.isNotEmpty() && kValue.isNotEmpty()) {
            Color(
                com.github.ajalt.colormath.model.CMYK(
                    cValue.toInt(),
                    mValue.toInt(),
                    yValue.toInt(),
                    kValue.toInt()
                ).toSRGB().toRGBInt().argb.toInt()
            )
        } else null

    fun guess() {
        var addedScore = 0

        val color: Int = this.color.toArgb()

        //------RGB-----
        addedScore += rgbScore(color)

        //----HEX----
        addedScore += hexScore(color)

        //-----CMYK-----
        addedScore += cmykScore(color)

        currentScore += addedScore

        state = GameState.Restart
        scoreReset++
    }

    fun rgbScore(color: Int): Int {
        val rGuessed: String = rValue
        val gGuessed: String = gValue
        val bGuessed: String = bValue
        //if any of the fields are empty, no points...no matter what
        //if any of the fields are empty, no points...no matter what
        return if (rGuessed.isNotEmpty() && gGuessed.isNotEmpty() && bGuessed.isNotEmpty()) {
            val r = rGuessed.toInt()
            val g = gGuessed.toInt()
            val b = bGuessed.toInt()
            val rScore: Int = getScore(color.red, r)
            val gScore: Int = getScore(color.green, g)
            val bScore: Int = getScore(color.blue, b)

            //if all three are correct, CONFETTI!!!
            if (rScore == RGB_MAX && gScore == RGB_MAX && bScore == RGB_MAX) {
                //confetti(Color.RED, Color.GREEN, Color.BLUE)
            }
            rScore + gScore + bScore
        } else 0
    }

    fun hexScore(color: Int): Int {
        val hexGuess: String = hexValue

        return try {
            //if the hex field is empty, ZERO points!
            if (hexGuess != "") {
                //parse the color
                val c: Int = android.graphics.Color.parseColor("#$hexGuess")
                //get the rgb values for it
                val r = c shr 16 and 0xff
                val g = c shr 8 and 0xff
                val b = c and 0xff
                val rScore: Int = getScore(color.red, r)
                val gScore: Int = getScore(color.green, g)
                val bScore: Int = getScore(color.blue, b)
                //if its perfect, CONFETTI!
                if (rScore == RGB_MAX && gScore == RGB_MAX && bScore == RGB_MAX) {
                    //confetti(Color.RED, Color.GREEN, Color.BLUE)
                }
                rScore + gScore + bScore
            } else 0
        } catch (e: NumberFormatException) {
            0
        }
    }

    fun cmykScore(color: Int): Int {
        val cGuessed: String = cValue
        val mGuessed: String = mValue
        val yGuessed: String = yValue
        val kGuessed: String = kValue
        //this is all to get the CMYK values. Taken from online.
        //http://www.javascripter.net/faq/hex2cmyk.htm
        //this is all to get the CMYK values. Taken from online.
        //http://www.javascripter.net/faq/hex2cmyk.htm
        var computedC = 1 - (color.red.toString() + "").toDouble() / RGB_MAX
        var computedM = 1 - (color.green.toString() + "").toDouble() / RGB_MAX
        var computedY = 1 - (color.blue.toString() + "").toDouble() / RGB_MAX

        val minCMY = min(computedC, min(computedM, computedY))

        computedC = (computedC - minCMY) / (1 - minCMY)
        computedM = (computedM - minCMY) / (1 - minCMY)
        computedY = (computedY - minCMY) / (1 - minCMY)
        //the CMYK values
        //the CMYK values
        val actualC = (computedC * 100)
        val actualM = (computedM * 100)
        val actualY = (computedY * 100)
        val actualK = (minCMY * 100)
        //if any of the fields are empty, no points
        //if any of the fields are empty, no points
        return if (cGuessed.isNotEmpty() && mGuessed.isNotEmpty() && yGuessed.isNotEmpty() && kGuessed.isNotEmpty()) {
            val c = cGuessed.toDouble()
            val m = mGuessed.toDouble()
            val y = yGuessed.toDouble()
            val k = kGuessed.toDouble()
            //making sure everything is within the right values
            if ((c <= 100 || c >= 0) && (m <= 100 || m >= 0) && (y <= 100 || y >= 0) && (k <= 100 || k >= 0)) {
                val cs: Double = getCMYKScore(actualC, c)
                val ms: Double = getCMYKScore(actualM, m)
                val ys: Double = getCMYKScore(actualY, y)
                val ks: Double = getCMYKScore(actualK, k)
                //if everything is perfect, CONFETTI!
                if (cs == CMYK_MAX.toDouble() && ms == CMYK_MAX.toDouble() && ys == CMYK_MAX.toDouble() && ks == CMYK_MAX.toDouble()) {

                }
                (cs + ms + ys + ks).toInt()
            } else {
                0
            }
        } else 0
    }

    fun reset() {
        hexValue = ""
        rValue = ""
        gValue = ""
        bValue = ""
        cValue = ""
        mValue = ""
        yValue = ""
        kValue = ""
        state = GameState.Play
        newColor()
    }

    fun newColor() {
        color = Random.nextColor(a = 255)
    }
}

/**
 * getScore - gets the score
 * @param actual - the actual value
 * @param guessed - the guessed value
 * @return - points
 */
fun getScore(actual: Int, guessed: Int): Int {
    //RGB_MAX - difference
    return RGB_MAX - (if (actual >= guessed) actual - guessed else guessed - actual).coerceIn(0, 255)
}

/**
 * getCMYKScore - gets the score
 * @param actual - the actual value
 * @param guessed - the guessed value
 * @return - points
 */
fun getCMYKScore(actual: Double, guessed: Double): Double {
    //CMYK_MAX - difference
    return CMYK_MAX - (if (actual >= guessed) actual - guessed else guessed - actual).coerceIn(0.0, 100.0)
}

data class CMYK(val c: Double, val m: Double, val y: Double, val k: Double)

fun getCMYKFromRGB(r: Int, g: Int, b: Int): CMYK {
    var computedC: Double = 1 - (r.toString() + "").toDouble() / RGB_MAX
    var computedM: Double = 1 - (g.toString() + "").toDouble() / RGB_MAX
    var computedY: Double = 1 - (b.toString() + "").toDouble() / RGB_MAX

    val minCMY = min(computedC, min(computedM, computedY))

    computedC = (computedC - minCMY) / (1 - minCMY)
    computedM = (computedM - minCMY) / (1 - minCMY)
    computedY = (computedY - minCMY) / (1 - minCMY)
    var computedK = minCMY

    computedC = String.format("%.3f", computedC).toDouble()
    computedM = String.format("%.3f", computedM).toDouble()
    computedY = String.format("%.3f", computedY).toDouble()
    computedK = String.format("%.3f", computedK).toDouble()
    return CMYK(computedC, computedM, computedY, computedK)
}