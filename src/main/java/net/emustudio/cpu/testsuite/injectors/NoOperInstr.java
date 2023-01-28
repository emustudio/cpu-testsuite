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
