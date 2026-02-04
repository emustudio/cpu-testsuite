/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.memory;

import net.emustudio.emulib.plugins.memory.annotations.MemoryContextAnnotations;
import net.emustudio.emulib.runtime.helpers.NumberUtils;
import net.jcip.annotations.NotThreadSafe;

import java.util.Arrays;

/**
 * Memory stub implementation using Short as the cell type.
 * <p>
 * This class provides a simple in-memory implementation of MemoryStub for testing purposes.
 * It stores memory as an array of Short values and supports various read/write operations.
 * This is particularly useful for testing CPUs that work with 16-bit memory cells.
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * // Create a memory stub with little-endian word reading strategy
 * ShortMemoryStub memory = new ShortMemoryStub(MemoryContext.LITTLE_ENDIAN);
 * 
 * // Write some values
 * memory.write(0, (short) 0x1234);
 * memory.write(1, (short) 0x5678);
 * 
 * // Read values
 * Short value = memory.read(0);
 * Short[] values = memory.read(0, 2);
 * 
 * // Clear all memory
 * memory.clear();
 * }</pre>
 */
@SuppressWarnings("unused")
@NotThreadSafe
public class ShortMemoryStub implements MemoryStub<Short> {
    private final int wordReadingStrategy;

    /**
     * The underlying memory array storing Short values.
     */
    protected Short[] memory = new Short[1000];

    /**
     * Creates a new short memory stub with the specified word reading strategy.
     *
     * @param wordReadingStrategy the strategy for reading words from memory (e.g., little-endian or big-endian)
     */
    public ShortMemoryStub(int wordReadingStrategy) {
        Arrays.fill(memory, (short) 0);
        this.wordReadingStrategy = wordReadingStrategy;
    }

    /**
     * Sets the entire memory content from a native short array.
     *
     * @param memory the short array to copy into memory
     */
    @Override
    public void setMemory(short[] memory) {
        this.memory = NumberUtils.nativeShortsToShorts(memory);
    }

    /**
     * Reads a Short value from the specified memory position.
     *
     * @param memoryPosition the memory address to read from
     * @return the Short value at the specified position
     */
    @Override
    public Short read(int memoryPosition) {
        return memory[memoryPosition];
    }

    /**
     * Reads multiple Short values starting from the specified memory position.
     *
     * @param memoryPosition the starting memory address
     * @param count the number of values to read
     * @return an array of Short values
     */
    @Override
    public Short[] read(int memoryPosition, int count) {
        int to = Math.min(memory.length, memoryPosition + count);
        return Arrays.copyOfRange(memory, memoryPosition, to);
    }

    /**
     * Writes a Short value to the specified memory position.
     *
     * @param memoryPosition the memory address to write to
     * @param value the Short value to write
     */
    @Override
    public void write(int memoryPosition, Short value) {
        memory[memoryPosition] = value;
    }

    /**
     * Writes multiple Short values to memory starting at the specified position.
     *
     * @param memoryPosition the starting memory address
     * @param cells the array of Short values to write
     * @param count the number of values to write
     */
    @Override
    public void write(int memoryPosition, Short[] cells, int count) {
        System.arraycopy(cells, 0, memory, memoryPosition, count);
    }

    /**
     * Returns the class type of memory cells.
     *
     * @return Short.class
     */
    @Override
    public Class<Short> getCellTypeClass() {
        return Short.class;
    }

    /**
     * Clears all memory by filling it with zeros.
     */
    @Override
    public void clear() {
        Arrays.fill(memory, (short) 0);
    }

    /**
     * Adds a memory listener (stub implementation - does nothing).
     *
     * @param listener the memory listener to add
     */
    @Override
    public void addMemoryListener(MemoryListener listener) {

    }

    /**
     * Removes a memory listener (stub implementation - does nothing).
     *
     * @param listener the memory listener to remove
     */
    @Override
    public void removeMemoryListener(MemoryListener listener) {

    }

    /**
     * Returns the size of the memory array.
     *
     * @return the memory size
     */
    @Override
    public int getSize() {
        return memory.length;
    }

    /**
     * Returns whether memory notifications are enabled (always false in this stub).
     *
     * @return false
     */
    @Override
    public boolean areMemoryNotificationsEnabled() {
        return false;
    }

    /**
     * Returns memory context annotations (always null in this stub).
     *
     * @return null
     */
    @Override
    public MemoryContextAnnotations annotations() {
        return null;
    }

    /**
     * Sets whether memory notifications are enabled (stub implementation - does nothing).
     *
     * @param enabled true to enable, false to disable
     */
    @Override
    public void setMemoryNotificationsEnabled(boolean enabled) {

    }

    /**
     * Returns the word reading strategy used by this memory stub.
     *
     * @return the word reading strategy
     */
    @Override
    public int getWordReadingStrategy() {
        return wordReadingStrategy;
    }
}
