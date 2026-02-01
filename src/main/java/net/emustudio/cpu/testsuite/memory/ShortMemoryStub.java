/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.memory;

import net.emustudio.emulib.plugins.memory.annotations.MemoryContextAnnotations;
import net.emustudio.emulib.runtime.helpers.NumberUtils;

import java.util.Arrays;

@SuppressWarnings("unused")
public class ShortMemoryStub implements MemoryStub<Short> {
    private final int wordReadingStrategy;

    protected Short[] memory = new Short[1000];

    public ShortMemoryStub(int wordReadingStrategy) {
        Arrays.fill(memory, (short) 0);
        this.wordReadingStrategy = wordReadingStrategy;
    }

    @Override
    public void setMemory(short[] memory) {
        this.memory = NumberUtils.nativeShortsToShorts(memory);
    }

    @Override
    public Short read(int memoryPosition) {
        return memory[memoryPosition];
    }

    @Override
    public Short[] read(int memoryPosition, int count) {
        int to = Math.min(memory.length, memoryPosition + count);
        return Arrays.copyOfRange(memory, memoryPosition, to);
    }

    @Override
    public void write(int memoryPosition, Short value) {
        memory[memoryPosition] = value;
    }

    @Override
    public void write(int memoryPosition, Short[] cells, int count) {
        System.arraycopy(cells, 0, memory, memoryPosition, count);
    }

    @Override
    public Class<Short> getCellTypeClass() {
        return Short.class;
    }

    @Override
    public void clear() {
        Arrays.fill(memory, (short) 0);
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
