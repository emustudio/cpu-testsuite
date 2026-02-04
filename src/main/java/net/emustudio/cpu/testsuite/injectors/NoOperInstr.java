/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.injectors;

import net.emustudio.cpu.testsuite.CpuRunner;
import net.emustudio.cpu.testsuite.injectors.internal.DefaultProgramGenerator;
import net.jcip.annotations.NotThreadSafe;

import java.util.function.Consumer;

/**
 * Instruction without operands.
 * <p>
 * It is used as an injector for the test runner.
 * <p>
 * Can have 1 or more opcodes.
 *
 * @param <TCpuRunner> the CPU runner type
 */
@NotThreadSafe
public class NoOperInstr<TCpuRunner extends CpuRunner<?>> implements Consumer<TCpuRunner> {
    private final DefaultProgramGenerator<?> strategy = new DefaultProgramGenerator<>();

    /**
     * Creates instruction with no operands injector.
     *
     * @param opcodes 1 or more opcode(s) of the instruction. Each opcode must be a byte (don't get confused by int).
     */
    public NoOperInstr(int... opcodes) {
        strategy.addOpcodes(opcodes);
    }

    /**
     * Injects the instruction (opcodes only, no operands) into the CPU runner.
     *
     * @param cpuRunner the CPU runner instance
     */
    @Override
    public void accept(TCpuRunner cpuRunner) {
        cpuRunner.setProgram(strategy.generate());
        strategy.clearOperands();
    }

    /**
     * Returns a string representation of this instruction.
     *
     * @return a string showing the instruction opcodes
     */
    @Override
    public String toString() {
        return strategy.toString();
    }

}
