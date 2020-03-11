// SPDX-License-Identifier: GPL-3.0-or-later
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
