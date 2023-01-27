/*
 * This file is part of cpu-testsuite.
 *
 * Copyright (C) 2017-2023  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.cpu.testsuite.injectors;

import net.emustudio.cpu.testsuite.CpuRunner;
import net.emustudio.cpu.testsuite.injectors.internal.DefaultProgramGenerator;

import java.util.function.BiConsumer;

/**
 * Instruction with single operand.
 *
 * It is used as an injector for the test runner.
 *
 * The order of bytes is as follows:
 *
 * 1. Initial opcodes (1 or more)
 * 2. Operand
 * 3. Possibly more opcodes (0 or more)
 *
 * @param <TCpuRunner> type of the CpuRunner
 * @param <TOperand> type of operand (Byte or Integer)
 */
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
     *
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
