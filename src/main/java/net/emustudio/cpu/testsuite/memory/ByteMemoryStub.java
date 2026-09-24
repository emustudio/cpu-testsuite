/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.memory;

import net.emustudio.emulib.runtime.helpers.NumberUtils;
import net.jcip.annotations.NotThreadSafe;

import static net.emustudio.emulib.runtime.helpers.NumberUtils.nativeShortsToBytes;

/** Byte-cell memory context for CPU tests. */
@SuppressWarnings("unused")
@NotThreadSafe
public class ByteMemoryStub extends AbstractMemoryStub<Byte> {
    /**
     * Creates zero-filled byte memory with the supplied word-reading strategy.
     *
     * @param wordReadingStrategy emuLib word-reading strategy
     */
    public ByteMemoryStub(int wordReadingStrategy) {
        super(wordReadingStrategy, new Byte[1000], (byte) 0);
    }

    /**
     * Replaces memory with primitive byte cells.
     *
     * @param memory new memory cells
     */
    public void setMemory(byte[] memory) {
        setMemoryCells(NumberUtils.nativeBytesToBytes(memory));
    }

    /**
     * Replaces memory with boxed byte cells.
     *
     * @param memory new memory cells
     */
    public void setMemory(Byte[] memory) {
        setMemoryCells(memory);
    }

    @Override
    public void setMemory(short[] memory) {
        setMemoryCells(nativeShortsToBytes(memory));
    }

    @Override
    public Class<Byte> getCellTypeClass() {
        return Byte.class;
    }
}
