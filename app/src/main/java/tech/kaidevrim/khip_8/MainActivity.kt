package tech.kaidevrim.khip_8

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import java.io.File


private var chip8: Chip8 = Chip8()

@OptIn(ExperimentalUnsignedTypes::class)
fun main() = application {
    configure {
        width = 640
        height = 320
        title = "Khip-8"
    }

    program {
        chip8.init(chip8, drawer)
        val romBytes = File("/Users/kai/Downloads/IBM.ch8").readBytes().toUByteArray()
        chip8.loadROM(chip8, romBytes)
        drawer.fill = ColorRGBa.WHITE
        drawer.strokeWeight = 0.0
        extend {
            chip8.cycle(chip8)
        }
    }
}