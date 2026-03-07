/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.memory;

import net.emustudio.emulib.runtime.helpers.NumberUtils;
import net.jcip.annotations.NotThreadSafe;

@SuppressWarnings("unused")
@NotThreadSafe
public class ShortMemoryStub extends AbstractMemoryStub<Short> {
    public ShortMemoryStub(int wordReadingStrategy) {
        super(wordReadingStrategy, new Short[1000], (short) 0);
    }

    @Override
    public void setMemory(short[] memory) {
        setMemoryCells(NumberUtils.nativeShortsToShorts(memory));
    }

    @Override
    public Class<Short> getCellTypeClass() {
        return Short.class;
    }
}
