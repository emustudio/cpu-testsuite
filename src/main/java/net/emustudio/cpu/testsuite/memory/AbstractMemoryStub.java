/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.memory;

import net.emustudio.emulib.plugins.memory.annotations.MemoryContextAnnotations;

import java.util.Arrays;
import java.util.Objects;

abstract class AbstractMemoryStub<T extends Number> implements MemoryStub<T> {
    private final int wordReadingStrategy;
    private final T zero;

    protected T[] memory;

    protected AbstractMemoryStub(int wordReadingStrategy, T[] memory, T zero) {
        this.wordReadingStrategy = wordReadingStrategy;
        this.memory = Objects.requireNonNull(memory);
        this.zero = Objects.requireNonNull(zero);
        clear();
    }

    protected final void setMemoryCells(T[] memory) {
        this.memory = Objects.requireNonNull(memory);
    }

    @Override
    public T read(int memoryPosition) {
        return memory[memoryPosition];
    }

    @Override
    public T[] read(int memoryPosition, int count) {
        int to = Math.min(memory.length, memoryPosition + count);
        return Arrays.copyOfRange(memory, memoryPosition, to);
    }

    @Override
    public void write(int memoryPosition, T value) {
        memory[memoryPosition] = value;
    }

    @Override
    public void write(int memoryPosition, T[] cells, int count) {
        System.arraycopy(cells, 0, memory, memoryPosition, count);
    }

    @Override
    public void clear() {
        Arrays.fill(memory, zero);
    }

    @Override
    public void addMemoryListener(MemoryListener listener) {
    }

    @Override
    public void removeMemoryListener(MemoryListener listener) {
    }

    @Override
    public int getSize() {
        return memory.length;
    }

    @Override
    public boolean areMemoryNotificationsEnabled() {
        return false;
    }

    @Override
    public MemoryContextAnnotations annotations() {
        return null;
    }

    @Override
    public void setMemoryNotificationsEnabled(boolean enabled) {
    }

    @Override
    public int getWordReadingStrategy() {
        return wordReadingStrategy;
    }
}
