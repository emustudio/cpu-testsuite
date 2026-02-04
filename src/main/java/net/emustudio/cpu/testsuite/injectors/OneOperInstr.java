/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.injectors;

import net.emustudio.cpu.testsuite.CpuRunner;
import net.emustudio.cpu.testsuite.injectors.internal.DefaultProgramGenerator;
import net.jcip.annotations.NotThreadSafe;

import java.util.function.BiConsumer;

/**
 * Instruction with single operand.
 * <p>
 * It is used as an injector for the test runner.
 * <p>
 * The order of bytes is as follows:
 * <ol>
 *   <li>Initial opcodes (1 or more)</li>
 *   <li>Operand</li>
 *   <li>Possibly more opcodes (0 or more)</li>
 * </ol>
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * // Create an instruction with opcode 0x3E (MVI A in Intel 8080)
 * OneOperInstr<MyCpuRunner, Byte> instr = new OneOperInstr<>(0x3E);
 * 
 * // Inject and execute with operand value 42
 * instr.accept(cpuRunner, (byte) 42);
 * // This generates: [0x3E, 0x2A] in memory
 * 
 * // With opcodes after operand
 * OneOperInstr<MyCpuRunner, Integer> instrWithSuffix = new OneOperInstr<>(0xCD)
 *     .placeOpcodesAfterOperand(0x00);  // CALL instruction
 * }</pre>
 *
 * @param <TCpuRunner> type of the CpuRunner
 * @param <TOperand>   type of operand (Byte or Integer)
 */
@NotThreadSafe
public class OneOperInstr<TCpuRunner extends CpuRunner<?>, TOperand extends Number> implements BiConsumer<TCpuRunner, TOperand> {

    private final DefaultProgramGenerator<TOperand> strategy = new DefaultProgramGenerator<>();

    /**
     * Create instruction with single operand injector.
     *
     * @param opcodes 1 or more opcode(s) of the instruction. Each opcode must be a byte (don't get confused by int).
     */
    public OneOperInstr(int... opcodes) {
        strategy.addOpcodes(opcodes);
    }

    /**
     * Inserts opcodes after operand.
     * <p>
     * NOTE: size of operands is given by OperandType parameter (Byte = 8 bits, Integer = 16 bits)
     *
     * @param opcodes opcode(s). Each opcode must be a byte (don't get confused by int).
     * @return this
     */
    public OneOperInstr<TCpuRunner, TOperand> placeOpcodesAfterOperand(int... opcodes) {
        strategy.addOpcodesAfterOperands(opcodes);
        return this;
    }

    @Override
    public void accept(TCpuRunner cpuRunner, TOperand operand) {
        strategy.setOperands(operand);

        cpuRunner.setProgram(strategy.generate());
        cpuRunner.ensureProgramSize(operand.intValue() & 0xFFFF + 2);

        strategy.clearOperands();
    }

    @Override
    public String toString() {
        return strategy.toString();
    }

}
