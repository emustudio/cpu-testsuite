/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.memory;

import net.jcip.annotations.NotThreadSafe;

/** Short-cell memory context for CPU tests. */
@SuppressWarnings("unused")
@NotThreadSafe
public class ShortMemoryStub extends AbstractMemoryStub<Short> {
    /**
     * Creates zero-filled short memory with the supplied word-reading strategy.
     *
     * @param wordReadingStrategy emuLib word-reading strategy
     */
    public ShortMemoryStub(int wordReadingStrategy) {
        super(wordReadingStrategy, new Short[1000], (short) 0);
    }

    @Override
    public void setMemory(short[] memory) {
        Short[] cells = new Short[memory.length];
        for (int i = 0; i < memory.length; i++) {
            cells[i] = memory[i];
        }
        setMemoryCells(cells);
    }

    @Override
    public Class<Short> getCellTypeClass() {
        return Short.class;
    }
}
