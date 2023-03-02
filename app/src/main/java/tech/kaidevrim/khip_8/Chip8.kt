@file:OptIn(ExperimentalUnsignedTypes::class)

package tech.kaidevrim.khip_8
class Chip8 {
    private var opcode: UShort? = null
    private var memory: UByteArray = UByteArray(4096)
    private var graphics: UByteArray = UByteArray(64 * 32)
    private var registers: UByteArray = UByteArray(16)
    private var index: UShort? = null
    private var programCounter: UShort? = null

    private var delayTimer: UByte? = null
    private var soundTimer: UByte? = null

    private var stack: UShortArray = UShortArray(16)
    private var sp: UShort? = null

    private var keys: UByteArray = UByteArray(16)
    // fun randomUInt() = Random(Clock.System.now().epochSeconds).nextUInt()
    fun init(self: Chip8) {
        self.init(this@Chip8)
        self.programCounter = 512u
        self.opcode = 0u
        self.index = 0u
        self.sp = 0u

        self.memory.forEachIndexed { index, _ -> self.memory[index] = 0u }
        self.graphics.forEachIndexed { index, _ -> self.graphics[index] = 0u }
        self.registers.forEachIndexed { index, _ -> self.registers[index] = 0u }
        self.stack.forEachIndexed { index, _ -> self.stack[index] = 0u }
        self.keys.forEachIndexed { index, _ -> self.keys[index] = 0u }

        self.delayTimer = 0u
        self.soundTimer = 0u
    }
}
