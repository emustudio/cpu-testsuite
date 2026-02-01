/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.injectors;

import net.emustudio.cpu.testsuite.CpuRunner;

import java.util.function.BiConsumer;

/**
 * Program memory expander.
 * <p>
 * Used as an injector for TestRunner.
 * <p>
 * Ensures that memory has at least specified size. The size is injected from TestRunner.
 *
 */
@SuppressWarnings("unused")
public class MemoryExpand<TCpuRunner extends CpuRunner<?>> implements BiConsumer<TCpuRunner, Integer> {

    @Override
    public void accept(TCpuRunner cpuRunner, Integer address) {
        cpuRunner.ensureProgramSize(address + 4);
    }

    @Override
    public String toString() {
        return "memoryExpander";
    }
}
