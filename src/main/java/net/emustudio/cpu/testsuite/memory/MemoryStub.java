/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.memory;

import net.emustudio.emulib.plugins.memory.MemoryContext;

/**
 * Mutable in-memory {@link MemoryContext} used by CPU tests.
 *
 * @param <T> memory cell type
 */
@SuppressWarnings("unused")
public interface MemoryStub<T extends Number> extends MemoryContext<T> {
    /**
     * Replaces the complete memory contents.
     *
     * @param memory new cells represented as unsigned byte values in shorts
     */
    void setMemory(short[] memory);

    /**
     * Returns the emuLib word-reading strategy configured for this memory.
     *
     * @return configured word-reading strategy
     */
    int getWordReadingStrategy();
}
