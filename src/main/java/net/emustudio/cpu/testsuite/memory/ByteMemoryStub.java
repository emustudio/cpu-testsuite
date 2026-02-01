/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.memory;

import net.emustudio.emulib.plugins.memory.annotations.MemoryContextAnnotations;
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
        int to = Math.min(memory.length, memoryPosition + count);
        return Arrays.copyOfRange(memory, memoryPosition, to);
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
    public Class<Byte> getCellTypeClass() {
        return Byte.class;
    }

    @Override
    public void clear() {
        Arrays.fill(memory, (byte) 0);
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
    public void setMemory(short[] memory) {
        this.memory = nativeShortsToBytes(memory);
    }

    @Override
    public int getWordReadingStrategy() {
        return wordReadingStrategy;
    }
}
