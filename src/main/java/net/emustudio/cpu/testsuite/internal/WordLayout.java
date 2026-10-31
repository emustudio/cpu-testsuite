/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.internal;

import net.emustudio.emulib.runtime.helpers.NumberUtils;

/** Internal encoding of 16-bit values using the memory stub's word-reading strategy. */
public final class WordLayout {
    private WordLayout() {
    }

    /**
     * Encodes the low 16 bits into two unsigned bytes.
     *
     * @param value word value
     * @param strategy emuLib byte order and optional per-byte bit reversal
     * @return encoded bytes in memory order
     */
    public static short[] encode(int value, int strategy) {
        int first = value & 0xFF;
        int second = (value >>> 8) & 0xFF;
        if ((strategy & NumberUtils.Strategy.REVERSE_BITS) != 0) {
            first = Integer.reverse(first) >>> 24;
            second = Integer.reverse(second) >>> 24;
        }
        if ((strategy & NumberUtils.Strategy.BIG_ENDIAN) != 0) {
            return new short[] {(short) second, (short) first};
        }
        return new short[] {(short) first, (short) second};
    }
}
