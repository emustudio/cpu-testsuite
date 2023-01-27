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
 * Injector of a byte value at specified memory address.
 *
 * Given memory address, test runner will inject a 8-bit value there.
 * Higher than 8-bit value will be truncated.
 */
public class MemoryByte<TCpuRunner extends CpuRunner<?>, TOperand extends Number> implements BiConsumer<TCpuRunner, TOperand> {
    private final int address;

    /**
     * Creates new memory byte injector
     *
     * @param address memory address where the byte will be injected
     */
    public MemoryByte(int address) {
        if (address <= 0) {
            throw new IllegalArgumentException("Address can be only > 0! (was " + address + ")");
        }

        this.address = address;
    }

    @Override
    public void accept(TCpuRunner cpuRunner, TOperand value) {
        cpuRunner.setByte(address, value.byteValue());
    }

    @Override
    public String toString() {
        return String.format("memoryByte[%04x]", address);
    }

}
