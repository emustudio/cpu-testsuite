// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.cpu.testsuite.memory;

import net.emustudio.emulib.plugins.memory.Memory;

import java.util.Arrays;

@SuppressWarnings("unused")
public class ShortMemoryStub implements MemoryStub<Short> {
    private final int wordReadingStrategy;

    protected short[] memory = new short[1000];
    private int wordCellsCount = 2;

    public ShortMemoryStub(int wordReadingStrategy) {
        this.wordReadingStrategy = wordReadingStrategy;
    }

    @Override
    public void setMemory(short[] memory) {
        this.memory = memory;
    }

    @Override
    public Short read(int memoryPosition) {
        return memory[memoryPosition];
    }

    @Override
    public Short[] readWord(int memoryPosition) {
        Short[] word = new Short[wordCellsCount];
        for (int i = 0; i < wordCellsCount; i++) {
            word[i] = memory[memoryPosition + i];
        }

        return word;
    }

    @Override
    public void write(int memoryPosition, Short value) {
        memory[memoryPosition] = value;
    }

    @Override
    public void writeWord(int memoryPosition, Short[] cells) {
        for (int i = 0; i < wordCellsCount; i++) {
            memory[memoryPosition + i] = cells[i];
        }
    }

    @Override
    public Class<Short> getDataType() {
        return Short.class;
    }

    @Override
    public void clear() {
        Arrays.fill(memory, (short) 0);
    }

    @Override
    public void addMemoryListener(Memory.MemoryListener listener) {

    }

    @Override
    public void removeMemoryListener(Memory.MemoryListener listener) {

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
    public void setMemoryNotificationsEnabled(boolean enabled) {

    }

    @Override
    public void setWordCellsCount(int count) {
        this.wordCellsCount = count;
    }

    @Override
    public int getWordReadingStrategy() {
        return wordReadingStrategy;
    }
}
