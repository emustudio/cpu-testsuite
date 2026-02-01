/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.injectors.internal;

import org.junit.Test;

import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void testToHexStringWithIntegers() {
        String result = Utils.toHexString(0x10, 0x20, 0x30);
        assertNotNull(result);
        assertTrue(result.contains("10"));
        assertTrue(result.contains("20"));
        assertTrue(result.contains("30"));
    }

    @Test
    public void testToHexStringWithBytes() {
        String result = Utils.toHexString((byte) 0xAA, (byte) 0xBB, (byte) 0xCC);
        assertNotNull(result);
        // Bytes should be formatted as hex
        assertTrue(result.contains("aa") || result.contains("AA"));
        assertTrue(result.contains("bb") || result.contains("BB"));
        assertTrue(result.contains("cc") || result.contains("CC"));
    }

    @Test
    public void testToHexStringWithEmptyArray() {
        String result = Utils.toHexString();
        assertNotNull(result);
        assertEquals("[]", result);
    }

    @Test
    public void testToHexStringWithSingleValue() {
        String result = Utils.toHexString(0xFF);
        assertNotNull(result);
        assertTrue(result.contains("ff") || result.contains("FF"));
    }

    @Test
    public void testToHexStringWithZero() {
        String result = Utils.toHexString(0);
        assertNotNull(result);
        assertTrue(result.contains("00"));
    }

    @Test
    public void testToHexStringWithNegativeNumber() {
        String result = Utils.toHexString(-1);
        assertNotNull(result);
        // -1 in two's complement
        assertTrue(result.contains("ff") || result.contains("ffffffff"));
    }

    @Test
    public void testToHexStringWithLargeNumber() {
        String result = Utils.toHexString(0xABCDEF);
        assertNotNull(result);
        assertTrue(result.contains("abcdef") || result.contains("ABCDEF"));
    }

    @Test
    public void testToHexStringWithShorts() {
        String result = Utils.toHexString((short) 0x1234, (short) 0x5678);
        assertNotNull(result);
        assertTrue(result.contains("1234") || result.contains("34"));
        assertTrue(result.contains("5678") || result.contains("78"));
    }

    @Test
    public void testToHexStringReturnsArrayFormat() {
        String result = Utils.toHexString(1, 2, 3);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
    }

    @Test
    public void testToHexStringWithMixedValues() {
        String result = Utils.toHexString(0x00, 0xFF, 0x10, 0xF0);
        assertNotNull(result);
        assertTrue(result.contains("00"));
    }

    @Test
    public void testToHexStringWithMaxByteValue() {
        String result = Utils.toHexString((byte) 0xFF);
        assertNotNull(result);
        assertTrue(result.contains("ff") || result.contains("FF"));
    }

    @Test
    public void testToHexStringWithMinByteValue() {
        String result = Utils.toHexString((byte) 0x00);
        assertNotNull(result);
        assertTrue(result.contains("00"));
    }

    @Test
    public void testToHexStringPadding() {
        String result = Utils.toHexString(1);
        // Should be padded to at least 2 digits
        assertTrue(result.contains("01"));
    }

    @Test
    public void testToHexStringMultipleZeros() {
        String result = Utils.toHexString(0, 0, 0);
        assertNotNull(result);
        // Should contain multiple "00" entries
        int count = result.length() - result.replace("00", "").length();
        assertTrue(count >= 6); // At least 3 occurrences of "00"
    }

    @Test
    public void testToHexStringSequence() {
        String result = Utils.toHexString(0x01, 0x02, 0x03, 0x04, 0x05);
        assertNotNull(result);
        assertTrue(result.contains("01"));
        assertTrue(result.contains("02"));
        assertTrue(result.contains("03"));
        assertTrue(result.contains("04"));
        assertTrue(result.contains("05"));
    }
}
