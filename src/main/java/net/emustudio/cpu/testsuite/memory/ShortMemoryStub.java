/*
 * This file is part of cpu-testsuite.
 *
 * Copyright (C) 2017-2023  Peter Jakubčo
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
