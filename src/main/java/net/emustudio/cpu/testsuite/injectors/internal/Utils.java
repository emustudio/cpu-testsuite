// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.cpu.testsuite.injectors.internal;

import java.util.Arrays;

/**
 * INTERNAL CLASS. DO NOT USE DIRECTLY.
 *
 */
public class Utils {

    @SafeVarargs
    public static <T> String toHexString(T... array) {
        String[] result = new String[array.length];

        for (int i = 0; i < array.length; i++) {
            result[i] = String.format("%02x", array[i]);
        }
        return Arrays.toString(result);
    }
}
