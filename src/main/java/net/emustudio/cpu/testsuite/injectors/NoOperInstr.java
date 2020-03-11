// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.cpu.testsuite.injectors;

import net.emustudio.cpu.testsuite.CpuRunner;
import net.emustudio.cpu.testsuite.injectors.internal.DefaultProgramGenerator;

import java.util.function.Consumer;

/**
 * Instruction without operands.
 *
 * It is used as an injector for the test runner.
 *
 * Can have 1 or more opcodes.
 */
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

    @Override
    public void accept(TCpuRunner cpuRunner) {
        cpuRunner.setProgram(strategy.generate());
        strategy.clearOperands();
    }

    @Override
    public String toString() {
        return strategy.toString();
    }

}
