/*
 * This file is part of cpu-testsuite.
 *
 * Copyright (C) 2017-2020  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
