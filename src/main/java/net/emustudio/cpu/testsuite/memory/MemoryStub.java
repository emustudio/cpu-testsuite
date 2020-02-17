/*
 * Generic test suite for comfortable CPU unit testing.
 *
 *     Copyright (C) 2015-2020  Peter Jakubčo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.emustudio.cpu.testsuite.memory;


import net.emustudio.emulib.plugins.memory.MemoryContext;

@SuppressWarnings("unused")
public interface MemoryStub<T extends Number> extends MemoryContext<T> {

    void setWordCellsCount(int count);

    void setMemory(short[] memory);

    int getWordReadingStrategy();

    default Byte[] numbersToBytes(Number[] read) {
        Byte[] word = new Byte[read.length];
        for (int i = 0; i < read.length; i++) {
            word[i] = read[i].byteValue();
        }
        return word;
    }

    default byte[] numbersToNativeBytes(Number[] read) {
        byte[] word = new byte[read.length];
        for (int i = 0; i < read.length; i++) {
            word[i] = read[i].byteValue();
        }
        return word;
    }

    default Byte[] shortsToBytes(Short[] read) {
        Byte[] word = new Byte[read.length];
        for (int i = 0; i < read.length; i++) {
            word[i] = read[i].byteValue();
        }
        return word;
    }

    default byte[] shortsToNativeBytes(Short[] read) {
        byte[] word = new byte[read.length];
        for (int i = 0; i < read.length; i++) {
            word[i] = read[i].byteValue();
        }
        return word;
    }

    default byte[] nativeShortsToNativeBytes(short[] read) {
        byte[] word = new byte[read.length];
        for (int i = 0; i < read.length; i++) {
            word[i] = (byte)(read[i] & 0xFF);
        }
        return word;
    }
}
