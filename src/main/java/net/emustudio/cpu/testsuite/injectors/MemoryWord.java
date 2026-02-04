/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.injectors;

import net.emustudio.cpu.testsuite.CpuRunner;
import net.jcip.annotations.Immutable;

import java.util.function.BiConsumer;

/**
 * Injector of a integer (2 bytes) value at specified memory address.
 * <p>
 * Given memory address, test runner will inject a 16-bit value there.
 * Higher than 16-bit value will be truncated.
 *
 * @param <TCpuRunner> the CPU runner type
 * @param <TOperand> the operand type (Byte or Integer)
 */
@Immutable
public class MemoryWord<TCpuRunner extends CpuRunner<?>, TOperand extends Number> implements BiConsumer<TCpuRunner, TOperand> {
    private final int address;

    /**
     * Creates an integer memory value injector.
     *
     * @param address address at which the test runner will inject a value
     */
    public MemoryWord(int address) {
        if (address < 0) {
            throw new IllegalArgumentException("Address must be non-negative (was " + address + ")");
        }
        this.address = address;
    }

    /**
     * Injects a 16-bit word value into memory at the configured address.
     * The value is written as two bytes in little-endian order.
     *
     * @param cpuRunner the CPU runner instance
     * @param value the word value to inject (higher than 16-bit values are truncated)
     */
    @Override
    public void accept(TCpuRunner cpuRunner, TOperand value) {
        int tmp = value.intValue();
        cpuRunner.setByte(address, tmp & 0xFF);
        cpuRunner.setByte(address + 1, (tmp >>> 8) & 0xFF);
    }

    /**
     * Returns a string representation of this memory word injector.
     *
     * @return a string showing the memory address in hexadecimal format
     */
    @Override
    public String toString() {
        return String.format("memoryWord[%04x]", address);
    }

}
