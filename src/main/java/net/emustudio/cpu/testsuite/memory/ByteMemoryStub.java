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
import net.emustudio.emulib.runtime.helpers.NumberUtils;

import java.util.Arrays;

import static net.emustudio.emulib.runtime.helpers.NumberUtils.nativeShortsToBytes;

@SuppressWarnings("unused")
public class ByteMemoryStub implements MemoryStub<Byte> {
    private final int wordReadingStrategy;

    protected Byte[] memory = new Byte[1000];

    public ByteMemoryStub(int wordReadingStrategy) {
        Arrays.fill(memory, (byte) 0);
        this.wordReadingStrategy = wordReadingStrategy;
    }

    public void setMemory(byte[] memory) {
        this.memory = NumberUtils.nativeBytesToBytes(memory);
    }

    public void setMemory(Byte[] memory) {
        this.memory = memory;
    }

    @Override
    public Byte read(int memoryPosition) {
        return memory[memoryPosition];
    }

    @Override
    public Byte[] read(int memoryPosition, int count) {
        Byte[] result = new Byte[count];
        System.arraycopy(memory, memoryPosition, result, 0, count);
        return result;
    }

    @Override
    public void write(int memoryPosition, Byte value) {
        memory[memoryPosition] = value;
    }

    @Override
    public void write(int memoryPosition, Byte[] cells, int count) {
        System.arraycopy(cells, 0, memory, memoryPosition, count);
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
    public void setMemory(short[] memory) {
        this.memory = nativeShortsToBytes(memory);
    }

    @Override
    public int getWordReadingStrategy() {
        return wordReadingStrategy;
    }
}
