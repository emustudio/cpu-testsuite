/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.memory;

import net.emustudio.emulib.plugins.memory.annotations.MemoryContextAnnotations;
import net.emustudio.emulib.runtime.helpers.NumberUtils;
import net.jcip.annotations.NotThreadSafe;

import java.util.Arrays;

import static net.emustudio.emulib.runtime.helpers.NumberUtils.nativeShortsToBytes;

/**
 * Memory stub implementation using Byte as the cell type.
 * <p>
 * This class provides a simple in-memory implementation of MemoryStub for testing purposes.
 * It stores memory as an array of Byte values and supports various read/write operations.
 */
@SuppressWarnings("unused")
@NotThreadSafe
public class ByteMemoryStub implements MemoryStub<Byte> {
    private final int wordReadingStrategy;

    /**
     * The underlying memory array storing Byte values.
     */
    protected Byte[] memory = new Byte[1000];

    /**
     * Creates a new byte memory stub with the specified word reading strategy.
     *
     * @param wordReadingStrategy the strategy for reading words from memory (e.g., little-endian or big-endian)
     */
    public ByteMemoryStub(int wordReadingStrategy) {
        Arrays.fill(memory, (byte) 0);
        this.wordReadingStrategy = wordReadingStrategy;
    }

    /**
     * Sets the entire memory content from a native byte array.
     *
     * @param memory the byte array to copy into memory
     */
    public void setMemory(byte[] memory) {
        this.memory = NumberUtils.nativeBytesToBytes(memory);
    }

    /**
     * Sets the entire memory content from a Byte array.
     *
     * @param memory the Byte array to copy into memory
     */
    public void setMemory(Byte[] memory) {
        this.memory = memory;
    }

    /**
     * Reads a Byte value from the specified memory position.
     *
     * @param memoryPosition the memory address to read from
     * @return the Byte value at the specified position
     */
    @Override
    public Byte read(int memoryPosition) {
        return memory[memoryPosition];
    }

    /**
     * Reads multiple Byte values starting from the specified memory position.
     *
     * @param memoryPosition the starting memory address
     * @param count the number of values to read
     * @return an array of Byte values
     */
    @Override
    public Byte[] read(int memoryPosition, int count) {
        int to = Math.min(memory.length, memoryPosition + count);
        return Arrays.copyOfRange(memory, memoryPosition, to);
    }

    /**
     * Writes a Byte value to the specified memory position.
     *
     * @param memoryPosition the memory address to write to
     * @param value the Byte value to write
     */
    @Override
    public void write(int memoryPosition, Byte value) {
        memory[memoryPosition] = value;
    }

    /**
     * Writes multiple Byte values to memory starting at the specified position.
     *
     * @param memoryPosition the starting memory address
     * @param cells the array of Byte values to write
     * @param count the number of values to write
     */
    @Override
    public void write(int memoryPosition, Byte[] cells, int count) {
        System.arraycopy(cells, 0, memory, memoryPosition, count);
    }

    /**
     * Returns the class type of memory cells.
     *
     * @return Byte.class
     */
    @Override
    public Class<Byte> getCellTypeClass() {
        return Byte.class;
    }

    /**
     * Clears all memory by filling it with zeros.
     */
    @Override
    public void clear() {
        Arrays.fill(memory, (byte) 0);
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
     * Sets the entire memory content from a short array.
     *
     * @param memory the short array to convert and copy into memory
     */
    @Override
    public void setMemory(short[] memory) {
        this.memory = nativeShortsToBytes(memory);
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
