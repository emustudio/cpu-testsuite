/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.injectors;

import net.emustudio.cpu.testsuite.CpuRunner;
import net.emustudio.cpu.testsuite.injectors.internal.DefaultProgramGenerator;
import net.jcip.annotations.NotThreadSafe;

/**
 * Instruction with two operands.
 * <p>
 * It is used as an injector for the test runner.
 * <p>
 * The order of bytes is as follows:
 * <ol>
 *   <li>Initial opcodes (1 or more)</li>
 *   <li>First Operand</li>
 *   <li>Second Operand</li>
 *   <li>Possibly more opcodes (0 or more)</li>
 * </ol>
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * // Create an instruction with opcode 0x80 (ADD B in Intel 8080)
 * TwoOperInstr<MyCpuRunner, Byte> instr = new TwoOperInstr<>(0x80);
 * 
 * // Inject and execute with two operands
 * instr.inject(cpuRunner, (byte) 10, (byte) 20);
 * // This generates: [0x80, 0x0A, 0x14] in memory
 * 
 * // With opcodes after operands
 * TwoOperInstr<MyCpuRunner, Integer> instrWithSuffix = new TwoOperInstr<>(0xC4)
 *     .placeOpcodesAfterOperands(0x00, 0x10);  // Complex instruction
 * }</pre>
 *
 * @param <TCpuRunner> type of CpuRunner
 * @param <TOperand>   type of the operand (Byte or Integer)
 */
@NotThreadSafe
public class TwoOperInstr<TCpuRunner extends CpuRunner<?>, TOperand extends Number> implements TwoOperInjector<TCpuRunner, TOperand> {
    private final DefaultProgramGenerator<TOperand> strategy = new DefaultProgramGenerator<>();

    /**
     * Create Instruction with two opcodes injector.
     *
     * @param opcodes 1 or more opcode(s) of the instruction. Each opcode must be a byte (don't get confused by int).
     */
    public TwoOperInstr(int... opcodes) {
        strategy.addOpcodes(opcodes);
    }

    /**
     * Will place more opcodes after the instruction operands.
     * <p>
     * NOTE: size of operands is given by OperandType parameter (Byte = 8 bits, Integer = 16 bits)
     *
     * @param opcodes opcode(s). Each opcode must be a byte (don't get confused by int).
     * @return this
     */
    public TwoOperInstr<TCpuRunner, TOperand> placeOpcodesAfterOperands(int... opcodes) {
        strategy.addOpcodesAfterOperands(opcodes);
        return this;
    }

    @Override
    public void inject(TCpuRunner cpuRunner, TOperand first, TOperand second) {
        strategy.setOperands(first, second);

        int tmpFirst = first.intValue() & 0xFFFF;
        int tmpSecond = second.intValue() & 0xFFFF;

        cpuRunner.setProgram(strategy.generate());
        cpuRunner.ensureProgramSize(Math.max(tmpFirst + 2, tmpSecond + 2));

        strategy.clearOperands();
    }

    @Override
    public String toString() {
        return strategy.toString();
    }

}
