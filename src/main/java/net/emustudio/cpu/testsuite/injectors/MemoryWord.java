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

import java.util.function.BiConsumer;

/**
 * Injector of a integer (2 bytes) value at specified memory address.
 *
 * Given memory address, test runner will inject a 16-bit value there.
 * Higher than 16-bit value will be truncated.
 *
 */
public class MemoryWord<TCpuRunner extends CpuRunner<?>, TOperand extends Number> implements BiConsumer<TCpuRunner, TOperand> {
    private final int address;

    /**
     * Creates an integer memory value injector.
     *
     * @param address address at which the test runner will inject a value
     */
    public MemoryWord(int address) {
        if (address <= 0) {
            throw new IllegalArgumentException("Address can be only > 0!");
        }

        this.address = address;
    }

    @Override
    public void accept(TCpuRunner cpuRunner, TOperand value) {
        int tmp = value.intValue();
        cpuRunner.setByte(address, tmp & 0xFF);
        cpuRunner.setByte(address + 1, (tmp >>> 8) & 0xFF);
    }

    @Override
    public String toString() {
        return String.format("memoryWord[%04x]", address);
    }

}
