@file:OptIn(ExperimentalUnsignedTypes::class, ExperimentalUnsignedTypes::class, ExperimentalUnsignedTypes::class)
package tech.kaidevrim.khip_8

import kotlinx.datetime.Clock
import java.lang.Exception
import kotlin.random.Random
import kotlin.random.nextUInt
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import kotlin.math.floor


class Chip8 {
    private var drawer: Drawer? = null
    private val scale: Double = 10.0
    private var opcode: UShort? = null
    private var memory: UByteArray = UByteArray(4096)
    private var graphics: UByteArray = UByteArray(64 * 32)
    private var registers: UByteArray = UByteArray(16)
    private var registerIndex: UShort? = null
    private var programCounter: UShort? = null

    private var delayTimer: UByte? = null
    private var soundTimer: UByte? = null

    private var stack: UShortArray = UShortArray(16)
    private var sp: UShort? = null

    private val chip8Fontset: IntArray = intArrayOf(
        0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
        0x20, 0x60, 0x20, 0x20, 0x70, // 1
        0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
        0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
        0x90, 0x90, 0xF0, 0x10, 0x10, // 4
        0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
        0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
        0xF0, 0x10, 0x20, 0x40, 0x40, // 7
        0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
        0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
        0xF0, 0x90, 0xF0, 0x90, 0x90, // A
        0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
        0xF0, 0x80, 0x80, 0x80, 0xF0, // C
        0xE0, 0x90, 0x90, 0x90, 0xE0, // D
        0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
        0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    )

    private var keys: UByteArray = UByteArray(16)
    fun randomUInt() = Random(Clock.System.now().epochSeconds).nextUInt()
    private fun shiftLeft(uShort: UShort, bits: Int): UShort {
        return (uShort.toInt() shl bits).toUShort()
    }
    fun shiftRight(uShort: UShort, bits: Int): UShort {
        return (uShort.toInt() shr bits).toUShort()
     }
    @OptIn(ExperimentalUnsignedTypes::class)
    fun init(self: Chip8, drawerIn: Drawer) {
        self.drawer = drawerIn
        self.programCounter = 512u
        self.opcode = 0u
        self.registerIndex = 0u
        self.sp = 0u

        self.memory.forEachIndexed { index, _ -> self.memory[index] = 0u }
        self.graphics.forEachIndexed { index, _ -> self.graphics[index] = 0u }
        self.registers.forEachIndexed { index, _ -> self.registers[index] = 0u }
        self.stack.forEachIndexed { index, _ -> self.stack[index] = 0u }
        self.keys.forEachIndexed { index, _ -> self.keys[index] = 0u }

        self.delayTimer = 0u
        self.soundTimer = 0u

        self.chip8Fontset.forEachIndexed {index, element -> self.memory[index] = element.toUByte() }

        self.graphics[5] = 1u
        self.graphics[1] = 1u
        self.graphics[95] = 1u
    }

    private fun incrementPc(self: Chip8) {
        self.programCounter = (self.programCounter?.plus(2u))?.toUShort()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun cycle(self: Chip8) {
        if (self.programCounter!! > 4095u) {
            throw Exception("OPcode out of range! Your program has an error!")
        }
        setDisplay(self)
        shiftRight(0u, 0)
        shiftLeft(0u, 0)
        randomUInt()
        self.opcode = self.memory[self.programCounter?.toInt()!!].toUShort()
        self.opcode = shiftLeft(self.opcode!!, 8).or(self.memory[(self.programCounter!! + 1u).toInt()].toUShort())

        when(opcode?.and(4096u)) {
            224u.toUShort() -> {
                self.graphics.forEachIndexed { index, _ -> self.graphics[index] = 0u }
                self.incrementPc(self)
            }

            238u.toUShort() -> {
                self.sp = (self.sp!! - 1u).toUShort()
                self.programCounter = self.stack[self.sp?.toInt()!!]
                self.incrementPc(self)
            }

            32768u.toUShort() -> {
                when(opcode?.and(15u)) {
                    0u.toUShort() -> {
                    }
                }
            }

            else -> {
                // var first = shiftRight(self.opcode!!, 12)
                println("The current opcode is: " + self.opcode)
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun setDisplay(self: Chip8) {
        for (i in self.graphics.indices) {
            if (self.graphics[i] == 0u.toUByte()) {
                self.drawer?.fill = ColorRGBa.BLACK
            }
            else {
                self.drawer?.fill = ColorRGBa.WHITE
            }
            val x: Int = (i % 64)
            val y: Int = floor(i / 64.0).toInt()

            self.drawer?.rectangle(x*scale, y*scale, scale, scale)
        }
    }
}