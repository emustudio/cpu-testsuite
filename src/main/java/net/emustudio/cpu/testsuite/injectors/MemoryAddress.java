// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.cpu.testsuite.injectors;

import net.emustudio.cpu.testsuite.CpuRunner;

import java.util.function.BiConsumer;

/**
 * Injector of specific value at injected memory address.
 *
 * Used for placing given value at memory address injected by TestRunner.
 * Based on the used constructor, it places either Byte or Integer at the injected address.
 */
public class MemoryAddress<TCpuRunner extends CpuRunner<?>, TOperand extends Number> implements BiConsumer<TCpuRunner, TOperand> {
    private final int value;
    private final boolean word;

    /**
     * Creates a byte memory value injector.
     *
     * @param value byte value
     */
    public MemoryAddress(Byte value) {
        this.value = value & 0xFF;
        word = false;
    }

    /**
     * Creates a Integer (word) value injector. Value will be truncated to 16 bits.
     *
     * @param value integer value
     */
    public MemoryAddress(Integer value) {
        this.value = value & 0xFFFF;
        word = true;
    }

    @Override
    public void accept(TCpuRunner cpuRunner, TOperand address) {
        int tmp = address.intValue();

        cpuRunner.setByte(tmp, value & 0xFF);
        if (word) {
            cpuRunner.setByte(tmp + 1, (value >>> 8) & 0xFF);
        }
    }

    @Override
    public String toString() {
        return String.format("memory[address] = %04x (word=%s)", value, word);
    }

}
