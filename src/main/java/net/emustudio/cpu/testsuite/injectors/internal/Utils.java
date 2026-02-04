/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.injectors.internal;

import java.util.Arrays;

/**
 * INTERNAL CLASS. DO NOT USE DIRECTLY.
 *
 */
public class Utils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Utils() {
        throw new AssertionError("Utility class, do not instantiate");
    }

    /**
     * Converts an array of values to a hexadecimal string representation.
     *
     * @param <T> the type of array elements
     * @param array the array to convert
     * @return a string representation with hexadecimal values
     */
    @SafeVarargs
    public static <T> String toHexString(T... array) {
        String[] result = new String[array.length];

        for (int i = 0; i < array.length; i++) {
            result[i] = String.format("%02x", array[i]);
        }
        return Arrays.toString(result);
    }
}
