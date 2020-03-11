// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.cpu.testsuite.memory;

import net.emustudio.emulib.plugins.memory.Memory;

import java.util.Arrays;

@SuppressWarnings("unused")
public class ByteMemoryStub implements MemoryStub<Byte> {
    private final int wordReadingStrategy;

    protected byte[] memory = new byte[1000];
    private int wordCellsCount = 2;

    public ByteMemoryStub(int wordReadingStrategy) {
        this.wordReadingStrategy = wordReadingStrategy;
    }

    public void setMemory(byte[] memory) {
        this.memory = memory;
    }

    @Override
    public Byte read(int memoryPosition) {
        return memory[memoryPosition];
    }

    @Override
    public Byte[] readWord(int memoryPosition) {
        Byte[] word = new Byte[wordCellsCount];
        for (int i = 0; i < wordCellsCount; i++) {
            word[i] = memory[memoryPosition + i];
        }

        return word;
    }

    @Override
    public void write(int memoryPosition, Byte value) {
        memory[memoryPosition] = value;
    }

    @Override
    public void writeWord(int memoryPosition, Byte[] cells) {
        for (int i = 0; i < wordCellsCount; i++) {
            memory[memoryPosition + i] = cells[i];
        }
    }

    @Override
    public Class<Byte> getDataType() {
        return Byte.class;
    }

    @Override
    public void clear() {
        Arrays.fill(memory, (byte) 0);
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
    public void setMemory(short[] memory) {
        this.memory = nativeShortsToNativeBytes(memory);
    }

    @Override
    public int getWordReadingStrategy() {
        return wordReadingStrategy;
    }
}
