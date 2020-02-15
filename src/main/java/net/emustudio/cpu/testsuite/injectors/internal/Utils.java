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
package net.emustudio.cpu.testsuite.injectors.internal;

import java.util.Arrays;

/**
 * INTERNAL CLASS. DO NOT USE DIRECTLY.
 *
 */
public class Utils {

    public static <T> String toHexString(T... array) {
        String[] result = new String[array.length];

        for (int i = 0; i < array.length; i++) {
            result[i] = String.format("%02x", array[i]);
        }
        return Arrays.toString(result);
    }
}
