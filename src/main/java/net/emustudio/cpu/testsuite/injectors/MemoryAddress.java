/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.injectors;

import net.emustudio.cpu.testsuite.CpuRunner;
import net.jcip.annotations.Immutable;

import java.util.function.BiConsumer;

/**
 * Injector of specific value at injected memory address.
 * <p>
 * Used for placing given value at memory address injected by TestRunner.
 * Based on the used constructor, it places either Byte or Integer at the injected address.
 *
 * @param <TCpuRunner> the CPU runner type
 * @param <TOperand> the operand type (Byte or Integer)
 */
@Immutable
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

    /**
     * Injects the configured value at the memory address specified by the operand.
     * If this is a word injector, writes two bytes; otherwise writes one byte.
     *
     * @param cpuRunner the CPU runner instance
     * @param address the memory address where the value should be written
     */
    @Override
    public void accept(TCpuRunner cpuRunner, TOperand address) {
        int tmp = address.intValue();

        cpuRunner.setByte(tmp, value & 0xFF);
        if (word) {
            cpuRunner.setByte(tmp + 1, (value >>> 8) & 0xFF);
        }
    }

    /**
     * Returns a string representation of this memory address injector.
     *
     * @return a string showing the value and whether it's a word
     */
    @Override
    public String toString() {
        return String.format("memory[address] = %04x (word=%s)", value, word);
    }

}
