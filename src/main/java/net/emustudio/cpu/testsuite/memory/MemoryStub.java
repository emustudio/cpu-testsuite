// SPDX-License-Identifier: GPL-3.0-or-later
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
