/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.injectors;

import net.emustudio.cpu.testsuite.CpuRunner;
import net.jcip.annotations.Immutable;

import java.util.function.BiConsumer;

/**
 * Injector of a byte value at specified memory address.
 * <p>
 * Given memory address, test runner will inject an 8-bit value there.
 * Higher than 8-bit value will be truncated.
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * // Create an injector that reads from memory address 0x1000
 * MemoryByte<MyCpuRunner, Byte> injector = new MemoryByte<>(0x1000);
 * 
 * // Use in a test - the value at memory[0x1000] will be used as the operand
 * testBuilder
 *     .firstIsMemoryByteAt(0x1000)  // First operand from memory[0x1000]
 *     .runWithFirstOperand(0xA0);   // Execute instruction with this operand
 * }</pre>
 *
 * @param <TCpuRunner> the CPU runner type
 * @param <TOperand> the operand type (typically Byte or Integer)
 */
@Immutable
public class MemoryByte<TCpuRunner extends CpuRunner<?>, TOperand extends Number> implements BiConsumer<TCpuRunner, TOperand> {
    private final int address;

    /**
     * Creates new memory byte injector
     *
     * @param address memory address where the byte will be injected
     */
    public MemoryByte(int address) {
        if (address < 0) {
            throw new IllegalArgumentException("Address must be non-negative (was " + address + ")");
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
