/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.injectors;

import net.emustudio.cpu.testsuite.CpuRunner;
import net.jcip.annotations.Immutable;

import java.util.function.BiConsumer;

/**
 * Program memory expander.
 * <p>
 * Used as an injector for TestRunner.
 * <p>
 * Ensures that memory has at least specified size. The size is injected from TestRunner.
 *
 * @param <TCpuRunner> the CPU runner type
 */
@SuppressWarnings("unused")
@Immutable
public class MemoryExpand<TCpuRunner extends CpuRunner<?>> implements BiConsumer<TCpuRunner, Integer> {

    /**
     * Creates a new memory expander.
     */
    public MemoryExpand() {
    }

    /**
     * Expands the program memory to ensure it can accommodate the specified address plus 4 bytes.
     *
     * @param cpuRunner the CPU runner instance
     * @param address the address that must be accommodated
     */
    @Override
    public void accept(TCpuRunner cpuRunner, Integer address) {
        cpuRunner.ensureProgramSize(address + 4);
    }

    /**
     * Returns a string representation of this memory expander.
     *
     * @return "memoryExpander"
     */
    @Override
    public String toString() {
        return "memoryExpander";
    }
}
