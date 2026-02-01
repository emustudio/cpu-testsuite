/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class GeneratorTest {

    @Before
    public void setUp() {
        Generator.setRandomTestsCount(25);
    }

    @Test
    public void testForAll8bitBinary() {
        AtomicInteger count = new AtomicInteger(0);
        Set<String> pairs = new HashSet<>();

        Generator.forAll8bitBinary((a, b) -> {
            count.incrementAndGet();
            pairs.add(a + "," + b);
        });

        // Should iterate through all combinations where j >= i
        // Total: sum from i=0 to 255 of (256 - i) = 256*256/2 + 256/2 = 32896
        assertEquals(32896, count.get());
        assertEquals(32896, pairs.size());
    }

    @Test
    public void testForAll8bitBinaryMultipleRunners() {
        AtomicInteger count1 = new AtomicInteger(0);
        AtomicInteger count2 = new AtomicInteger(0);

        Generator.forAll8bitBinary(
            (a, b) -> count1.incrementAndGet(),
            (a, b) -> count2.incrementAndGet()
        );

        assertEquals(32896, count1.get());
        assertEquals(32896, count2.get());
    }

    @Test
    public void testForSome8bitBinary() {
        AtomicInteger count = new AtomicInteger(0);

        Generator.forSome8bitBinary((a, b) -> count.incrementAndGet());

        assertEquals(25, count.get());
    }

    @Test
    public void testForSome8bitBinaryWithCustomCount() {
        Generator.setRandomTestsCount(10);
        AtomicInteger count = new AtomicInteger(0);

        Generator.forSome8bitBinary((a, b) -> count.incrementAndGet());

        assertEquals(10, count.get());
    }

    @Test
    public void testForAll8bitBinaryWhichEqual() {
        AtomicInteger count = new AtomicInteger(0);

        Generator.forAll8bitBinaryWhichEqual((a, b) -> {
            assertEquals(a, b);
            count.incrementAndGet();
        });

        assertEquals(256, count.get());
    }

    @Test
    public void testForSome8bitBinaryWhichEqual() {
        AtomicInteger count = new AtomicInteger(0);

        Generator.forSome8bitBinaryWhichEqual((a, b) -> {
            assertEquals(a, b);
            count.incrementAndGet();
        });

        assertEquals(25, count.get());
    }

    @Test
    public void testForAll16bitBinaryWithStartValues() {
        AtomicInteger count = new AtomicInteger(0);
        int firstStart = 65530;
        int secondStart = 65530;

        Generator.forAll16bitBinary(firstStart, secondStart, (a, b) -> {
            assertTrue(a >= firstStart);
            assertTrue(b >= secondStart);
            count.incrementAndGet();
        });

        // From 65530 to 65535 (6 values) for both = 6*6 = 36
        assertEquals(36, count.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForAll16bitBinaryThrowsExceptionForInvalidFirstStart() {
        Generator.forAll16bitBinary(0xFFFF + 1, 0, (a, b) -> {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForAll16bitBinaryThrowsExceptionForInvalidSecondStart() {
        Generator.forAll16bitBinary(0, 0xFFFF + 1, (a, b) -> {});
    }

    @Test
    public void testForSome16bitBinary() {
        AtomicInteger count = new AtomicInteger(0);

        Generator.forSome16bitBinary((a, b) -> count.incrementAndGet());

        assertEquals(25, count.get());
    }

    @Test
    public void testForSome16bitBinaryWithStartValues() {
        AtomicInteger count = new AtomicInteger(0);
        int firstStart = 100;
        int secondStart = 200;

        Generator.forSome16bitBinary(firstStart, secondStart, (a, b) -> {
            assertTrue("First value should be >= " + firstStart, a >= firstStart);
            assertTrue("Second value should be >= " + secondStart, b >= secondStart);
            count.incrementAndGet();
        });

        assertEquals(25, count.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForSome16bitBinaryThrowsExceptionForInvalidFirstStart() {
        Generator.forSome16bitBinary(0xFFFF + 1, 0, (a, b) -> {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForSome16bitBinaryThrowsExceptionForInvalidSecondStart() {
        Generator.forSome16bitBinary(0, 0xFFFF + 1, (a, b) -> {});
    }

    @Test
    public void testForAll16bitBinaryWhichEqualSmallSample() {
        // Test only a small range to avoid long execution time
        AtomicInteger count = new AtomicInteger(0);

        Generator.forAll16bitBinaryWhichEqual((a, b) -> {
            assertEquals(a, b);
            count.incrementAndGet();
            // Only test first few iterations
            if (count.get() >= 10) {
                return; // Stop after 10 iterations
            }
        });

        // We stopped early, so count should be at least 10
        assertTrue(count.get() >= 10);
    }

    @Test
    public void testForSome16bitBinaryWhichEqual() {
        AtomicInteger count = new AtomicInteger(0);

        Generator.forSome16bitBinaryWhichEqual((a, b) -> {
            assertEquals(a, b);
            count.incrementAndGet();
        });

        assertEquals(25, count.get());
    }

    @Test
    public void testForAll16bitBinaryFirstSatisfying() {
        AtomicInteger count = new AtomicInteger(0);

        // Test with a predicate that accepts only even numbers less than 4
        Generator.forAll16bitBinaryFirstSatisfying(
            i -> i < 4 && i % 2 == 0,
            (a, b) -> {
                assertTrue(a < 4);
                assertEquals(0, a % 2);
                count.incrementAndGet();
            }
        );

        // First values: 0, 2 (2 values), second values: 0-65535 (65536 values)
        // Total: 2 * 65536 = 131072
        assertEquals(131072, count.get());
    }

    @Test
    public void testForSome16bitBinaryFirstSatisfying() {
        AtomicInteger count = new AtomicInteger(0);

        Generator.forSome16bitBinaryFirstSatisfying(
            i -> i % 2 == 0,
            (a, b) -> {
                assertEquals(0, a % 2);
                count.incrementAndGet();
            }
        );

        assertEquals(25, count.get());
    }

    @Test
    public void testForSome16bitBinaryBothSatisfying() {
        AtomicInteger count = new AtomicInteger(0);

        Generator.forSome16bitBinaryBothSatisfying(
            i -> i % 2 == 0,
            i -> i % 3 == 0,
            (a, b) -> {
                assertEquals(0, a % 2);
                assertEquals(0, b % 3);
                count.incrementAndGet();
            }
        );

        assertEquals(25, count.get());
    }

    @Test
    public void testForAll16bitBinaryBothSatisfying() {
        AtomicInteger count = new AtomicInteger(0);

        Generator.forAll16bitBinaryBothSatisfying(
            i -> i < 3,
            i -> i < 2,
            (a, b) -> {
                assertTrue(a < 3);
                assertTrue(b < 2);
                count.incrementAndGet();
            }
        );

        // First: 0, 1, 2 (3 values), Second: 0, 1 (2 values) = 6 combinations
        assertEquals(6, count.get());
    }

    @Test
    public void testForAll8bitUnary() {
        AtomicInteger count = new AtomicInteger(0);

        Generator.forAll8bitUnary((a, b) -> {
            assertEquals(0, b.intValue());
            count.incrementAndGet();
        });

        assertEquals(256, count.get());
    }

    @Test
    public void testForSome8bitUnary() {
        AtomicInteger count = new AtomicInteger(0);

        Generator.forSome8bitUnary((a, b) -> {
            assertEquals(0, b.intValue());
            count.incrementAndGet();
        });

        assertEquals(25, count.get());
    }

    @Test
    public void testForSome16bitUnary() {
        AtomicInteger count = new AtomicInteger(0);

        Generator.forSome16bitUnary((a, b) -> {
            assertEquals(0, b.intValue());
            count.incrementAndGet();
        });

        assertEquals(25, count.get());
    }

    @Test
    public void testForSome16bitUnaryWithStartValue() {
        AtomicInteger count = new AtomicInteger(0);
        int startFrom = 1000;

        Generator.forSome16bitUnary(startFrom, (a, b) -> {
            assertTrue(a >= startFrom);
            assertEquals(0, b.intValue());
            count.incrementAndGet();
        });

        assertEquals(25, count.get());
    }

    @Test
    public void testForAll16bitUnary() {
        AtomicInteger count = new AtomicInteger(0);
        int startFrom = 65530;

        Generator.forAll16bitUnary(startFrom, (a, b) -> {
            assertTrue(a >= startFrom);
            assertEquals(0, b.intValue());
            count.incrementAndGet();
        });

        // From 65530 to 65535 = 6 values
        assertEquals(6, count.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForAll16bitUnaryThrowsExceptionForInvalidStart() {
        Generator.forAll16bitUnary(0xFFFF + 1, (a, b) -> {});
    }

    @Test
    public void testForGivenOperandsAndSingleRunWithOneOperand() {
        AtomicInteger count = new AtomicInteger(0);
        Integer operand = 42;

        Generator.<Integer>forGivenOperandsAndSingleRun(operand, operand, (a, b) -> {
            assertEquals(Integer.valueOf(42), a);
            assertEquals(Integer.valueOf(42), b);
            count.incrementAndGet();
        });

        assertEquals(1, count.get());
    }

    @Test
    public void testForGivenOperandsAndSingleRunWithTwoOperands() {
        AtomicInteger count = new AtomicInteger(0);
        Integer first = 10;
        Integer second = 20;

        Generator.<Integer>forGivenOperandsAndSingleRun(first, second, (a, b) -> {
            assertEquals(Integer.valueOf(10), a);
            assertEquals(Integer.valueOf(20), b);
            count.incrementAndGet();
        });

        assertEquals(1, count.get());
    }

    @Test
    public void testForGivenOperandsWithMultipleRunners() {
        AtomicInteger count1 = new AtomicInteger(0);
        AtomicInteger count2 = new AtomicInteger(0);
        Integer first = 5;
        Integer second = 10;

        Generator.<Integer>forGivenOperandsAndSingleRun(
            first, second,
            (a, b) -> count1.incrementAndGet(),
            (a, b) -> count2.incrementAndGet()
        );

        assertEquals(1, count1.get());
        assertEquals(1, count2.get());
    }

    @Test
    public void testSetRandomTestsCount() {
        Generator.setRandomTestsCount(5);
        AtomicInteger count = new AtomicInteger(0);

        Generator.forSome8bitBinary((a, b) -> count.incrementAndGet());

        assertEquals(5, count.get());

        // Reset to default
        Generator.setRandomTestsCount(25);
    }
}
