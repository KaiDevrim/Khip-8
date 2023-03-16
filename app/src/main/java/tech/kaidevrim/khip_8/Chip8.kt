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
    private var tmp: Int = 0

    private fun randomUInt() = Random(Clock.System.now().epochSeconds).nextUInt(256u)

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
        self.tmp = 0
    }

    private fun incrementPc(self: Chip8, by: UShort = 2u) {
        self.programCounter = (self.programCounter + by).toUShort()
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
        self.opcode =
            (self.opcode.toInt() shl 8).toUShort().or(self.memory[(self.programCounter + 1u).toInt()].toUShort())
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun decode(self: Chip8) {
        // Exact instructions
        when (self.opcode.toUInt()) {
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
        when (self.opcode.and((0xF000u)).toUInt()) {
            // 1NNN Jump to location nnn
            0x1000u -> {
                self.programCounter = self.opcode.and(0x0FFFu)
            }

            0x2000u -> {
                // 2NNN - Call subroutine at nnn.
                self.stack[(++self.sp).toInt()] = self.programCounter
                self.programCounter = self.opcode.and(0x0FFFu)
                self.incrementPc(self)
            }

            0x3000u -> {
                // 3XNN - Skip next instruction if Vx = kk.
                if (self.registers[(self.opcode.and(0x0F00u)).toInt() ushr 8] == (self.opcode.and(0x00FFu)).toUByte()) {
                    self.incrementPc(self, 4u)
                } else {
                    self.incrementPc(self)
                }
            }

            0x4000u -> {
                // 4XNN - Skip next instruction if Vx != kk.
                if (self.registers[(self.opcode.and(0x0F00u)).toInt() ushr 8] != (self.opcode.and(0x00FFu)).toUByte()) {
                    self.incrementPc(self, 4u)
                } else {
                    self.incrementPc(self)
                }
            }

            0x5000u -> {
                // 5XY0 - Skip next instruction if Vx = Vy.
                if (self.registers[(self.opcode.and(0x0F00u)).toInt() ushr 8] == self.registers[self.opcode.and(0x00F0u)
                        .toInt() ushr 4]
                ) {
                    self.incrementPc(self, 4u)
                } else {
                    self.incrementPc(self)
                }
            }

            0x6000u -> {
                // 6XNN - Set Vx = kk.
                self.registers[(self.opcode.and(0x0F00u)).toInt() ushr 8] = (self.opcode.and(0x00FFu)).toUByte()
                self.incrementPc(self)
            }

            0x7000u -> {
                self.tmp = (self.opcode.and(0x0F00u).toInt() ushr 8)
                val tmp2: UInt = (self.opcode.and(0x00FFu)).toUInt()
                val result: UInt = self.registers[self.tmp] + tmp2

                if (result >= 256u) {
                    self.registers[self.tmp] = (result - 256u).toUByte()
                } else {
                    self.registers[self.tmp] = result.toUByte()
                }

                self.incrementPc(self)
            }
        }
        when (self.opcode.and((0xF00Fu)).toUInt()) {
            0x8000u -> {
                self.registers[(self.opcode.and(0x0F00u).toInt() ushr 8)] =
                    self.registers[(self.opcode.and(0x00F0u)).toInt() ushr 4]

                self.incrementPc(self)
            }

            0x8001u -> {
                self.tmp = (self.opcode.and(0x0F00u).toInt() ushr 8)
                self.registers[self.tmp] =
                    (self.registers[self.tmp].or(self.registers[(self.opcode.and(0x00F0u).toInt() ushr 4)]))

                self.incrementPc(self)
            }

            0x8002u -> {
                self.tmp = (self.opcode.and(0x0F00u).toInt() ushr 8)
                self.registers[self.tmp] =
                    (self.registers[self.tmp].and(self.registers[self.opcode.and(0x00F0u).toInt() ushr 4]))

                self.incrementPc(self)
            }

            0x8003u -> {
                self.tmp = (self.opcode.and(0x0F00u).toInt() ushr 8)
                self.registers[self.tmp] =
                    (self.registers[self.tmp].xor(self.registers[(self.opcode.and(0x00F0u).toInt() ushr 4)]))

                self.incrementPc(self)
            }

            0x8004u -> {
                self.tmp = (self.opcode.and(0x0F00u).toInt() ushr 8)
                val sum: UInt = self.registers[self.tmp] + self.registers[(self.opcode.and(0x00F0u).toInt() ushr 4)]
                if (sum > 0xFFu) {
                    self.registers[0xF] = 1u
                } else {
                    self.registers[self.tmp] = (sum.and(0xFFu).toUByte())
                }

                self.incrementPc(self)
            }

            0x8005u -> {
                self.tmp = (self.opcode.and(0x0F00u).toInt() ushr 8)

                if (self.registers[(self.opcode.and(0x00F0u).toInt()) ushr 4] > self.registers[self.tmp]) {
                    self.registers[0xF] = 0u
                } else {
                    self.registers[0xF] = 1u
                }

                self.registers[self.tmp] =
                    ((self.registers[self.tmp] - self.registers[(self.opcode.and(((0x00F0u).toInt() ushr 4).toUShort())
                        .toInt())].and(0xFFu)).toUByte())

                self.incrementPc(self)
            }

            0x8006u -> {
                self.tmp = (self.opcode.and(0x0F00u).toInt() ushr 8)
                if (self.registers[self.tmp].and(0x1u).toInt() == 1) {
                    self.registers[0xF] = 1u
                } else {
                    self.registers[0xF] = 1u
                }

                self.incrementPc(self)
            }

            0x8007u -> {
                self.tmp = (self.opcode.and(0x0F00u).toInt() ushr 8)

                if (self.registers[(self.opcode.and(0x0F00u).toInt() ushr 4)] > self.registers[self.tmp]) {
                    self.registers[0xF] = 1u
                } else {
                    self.registers[0xF] = 0u
                }

                self.registers[self.tmp] =
                    ((self.registers[(self.opcode.and(0x00F0u).toInt() ushr 4)] - self.registers[self.tmp]).and(0xFFu)
                        .toUByte())

                self.incrementPc(self)
            }

            0x800Eu -> {
                self.tmp = (self.opcode.and(0x0F00u).toInt() ushr 8)
                if ((self.registers[self.tmp].toInt() ushr 7) == 0x1) {
                    self.registers[0x1] = 1u
                } else {
                    self.registers[0x1] = 0u
                }

                self.registers[self.tmp] = (((self.registers[self.tmp].toInt() shl 1).toUByte()).and(0xFFu))
                self.incrementPc(self)
            }

            0x9000u -> {
                self.tmp = (self.opcode.and(0x0F00u).toInt() ushr 8)
                if (self.registers[self.tmp] != self.registers[(self.opcode.and(0x00F0u).toInt()) ushr 4]) {
                    self.incrementPc(self, 4u)
                } else {
                    self.incrementPc(self)
                }
            }
        }
        when (self.opcode.and(0xF000u).toUInt()) {
            0xA000u -> {
                self.registerIndex = (self.opcode.and(0x0FFFu))
                self.incrementPc(self)
            }

            0xB000u -> {
                self.programCounter = ((self.opcode.and(0x0FFFu)) + self.registers[0]).toUShort()
            }

            0xC000u -> {
                self.tmp = (self.opcode.and(0x0F00u).toInt() ushr 8)
                self.registers[self.tmp] = randomUInt().and(self.opcode.and(0x00FFu).toUInt()).toUByte()

                self.incrementPc(self)
            }

            0xD000u -> {
                // do display things
            }
        }

        when (self.opcode.and(0xF0FFu).toUInt()) {
            0xE09Eu -> {
                
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