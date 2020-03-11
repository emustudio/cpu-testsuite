// SPDX-License-Identifier: GPL-3.0-or-later
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
