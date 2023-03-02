@file:OptIn(ExperimentalUnsignedTypes::class)

package tech.kaidevrim.khip_8

import java.lang.Exception

class Chip8 {
    private var opcode: UShort? = null
    private var memory: UByteArray = UByteArray(4096)
    private var graphics: UByteArray = UByteArray(64 * 32)
    private var registers: UByteArray = UByteArray(16)
    private var rIndex: UShort? = null
    private var programCounter: UShort = 0u

    private var delayTimer: UByte? = null
    private var soundTimer: UByte? = null

    private var stack: UShortArray = UShortArray(16)
    private var sp: UShort? = null

    private val chip8Fontset = intArrayOf(
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
    // fun randomUInt() = Random(Clock.System.now().epochSeconds).nextUInt()
    fun init(self: Chip8) {
        self.programCounter = 512u
        self.opcode = 0u
        self.rIndex = 0u
        self.sp = 0u

        self.memory.forEachIndexed { index, _ -> self.memory[index] = 0u }
        self.graphics.forEachIndexed { index, _ -> self.graphics[index] = 0u }
        self.registers.forEachIndexed { index, _ -> self.registers[index] = 0u }
        self.stack.forEachIndexed { index, _ -> self.stack[index] = 0u }
        self.keys.forEachIndexed { index, _ -> self.keys[index] = 0u }

        self.delayTimer = 0u
        self.soundTimer = 0u

        self.chip8Fontset.forEachIndexed {index, element -> self.memory[index] = element.toUByte() }
    }

    private fun increment_pc(self: Chip8) {
        self.programCounter = (self.programCounter + 2u).toUShort()
    }

    fun cycle(self: Chip8) {
        if (self.programCounter > 4095u) {
            throw Exception("OPcode out of range! Your program has an error!")
        }
    }
}
