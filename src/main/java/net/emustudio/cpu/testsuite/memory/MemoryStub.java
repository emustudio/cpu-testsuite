/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.memory;


import net.emustudio.emulib.plugins.memory.MemoryContext;

/**
 * Memory stub interface for testing purposes.
 * <p>
 * This interface extends MemoryContext and adds methods specific to test scenarios,
 * such as setting memory from short arrays and specifying word reading strategies.
 *
 * @param <T> the type of memory cells (Byte or Short)
 */
@SuppressWarnings("unused")
public interface MemoryStub<T extends Number> extends MemoryContext<T> {

    /**
     * Sets the entire memory content from a short array.
     *
     * @param memory the short array to copy into memory
     */
    void setMemory(short[] memory);

    /**
     * Returns the word reading strategy (e.g., little-endian or big-endian).
     *
     * @return the word reading strategy constant
     */
    int getWordReadingStrategy();
}
