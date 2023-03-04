package tech.kaidevrim.khip_8

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.tint
import java.lang.Exception
import kotlin.system.exitProcess


private var chip8: Chip8 = Chip8()
private var keepOpen: Boolean = true

fun main() = application {
    configure {
        width = 640
        height = 320
        chip8.init(chip8)
    }

    program {
        extend {
            drawer.drawStyle.colorMatrix = tint(ColorRGBa.WHITE.shade(0.2))
            drawer.fill = ColorRGBa.WHITE
            while (keepOpen) {
                try {
                    chip8.cycle(chip8)
                } catch (err: Exception) {
                    println(err)
                    exitProcess(-1)
                }
                keepOpen = false
            }
        }
    }
}