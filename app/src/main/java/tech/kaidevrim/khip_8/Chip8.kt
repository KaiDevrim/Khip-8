@file:OptIn(ExperimentalUnsignedTypes::class, ExperimentalUnsignedTypes::class)

package tech.kaidevrim.khip_8

import kotlinx.datetime.Clock
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import kotlin.math.floor
import kotlin.random.Random
import kotlin.random.nextUInt


class Chip8 {
    private lateinit var drawer: Drawer
    private var scale: Double = 10.0

    private var opcode: UShort = 0u
    private var memory: UByteArray = UByteArray(4096)
    private var graphics: UByteArray = UByteArray(64 * 32)
    private var registers: UByteArray = UByteArray(16)
    private var registerIndex: UShort = 0u
    private var programCounter: UShort = 0x200u

    private var delayTimer: UByte = 0u
    private var soundTimer: UByte = 0u

    private var stack: UShortArray = UShortArray(16)
    private var sp: UShort = 0u

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

    private fun randomUInt() = Random(Clock.System.now().epochSeconds).nextUInt()
    private fun shiftLeft(uShort: UShort, bits: Int): UShort {
        return (uShort.toInt() shl bits).toUShort()
    }

    private fun shiftRight(uShort: UShort, bits: Int): UShort {
        return (uShort.toInt() shr bits).toUShort()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun init(self: Chip8, drawerIn: Drawer) {
        self.drawer = drawerIn
        self.scale = 10.0
        self.programCounter = 0x200u
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

        self.chip8Fontset.forEachIndexed { index, element -> self.memory[index] = element.toUByte() }

        self.graphics[5] = 1u
        self.graphics[1] = 1u
        self.graphics[95] = 1u
    }

    private fun incrementPc(self: Chip8) {
        self.programCounter = (self.programCounter + 2u).toUShort()
    }

    fun cycle(self: Chip8) {
        setDisplay(self)
        getOpcode(self)
        decode(self)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun getOpcode(self: Chip8) {
        if (self.programCounter > 0xFFFu) {
            throw Exception("opcode out of range! Your program has an error!")
        }
        self.opcode = self.memory[self.programCounter.toInt()].toUShort()
        self.opcode = shiftLeft(self.opcode, 8).or(self.memory[(self.programCounter + 1u).toInt()].toUShort())
    }

    private fun spAdd(self: Chip8, by: UInt): Int {
        return (self.sp + by).toInt()
    }

    private fun spMinus(self: Chip8, by: UInt): Int {
        return (self.sp - by).toInt()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun decode(self: Chip8) {
        var x: Int = 0

        // Exact instructions
        when (opcode.toUInt()) {
            // Clear Screen
            0x00E0u -> {
                self.graphics.forEachIndexed { index, _ -> self.graphics[index] = 0u }
                self.incrementPc(self)
            }

            // Return from subroutine
            0x00EEu -> {
                self.sp = (self.sp - 1u).toUShort()
                self.programCounter = self.stack[self.sp.toInt()]
                self.incrementPc(self)
            }
        }

        // Access data in opcode
        when (opcode.and((0xF000u)).toUInt()) {
            // 1NNN Jump to location nnn
            0x1000u -> {
                programCounter = self.opcode.and(0x0FFFu)
            }

            0x2000u -> {

            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun setDisplay(self: Chip8) {
        for (i in self.graphics.indices) {
            if (self.graphics[i] == 0u.toUByte()) {
                self.drawer.fill = ColorRGBa.BLACK
            } else {
                self.drawer.fill = ColorRGBa.WHITE
            }
            val x: Int = (i % 64)
            val y: Int = floor(i / 64.0).toInt()

            self.drawer.rectangle(x * scale, y * scale, scale, scale)
        }
    }
}