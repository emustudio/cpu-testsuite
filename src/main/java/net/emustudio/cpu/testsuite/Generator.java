/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite;

import net.jcip.annotations.NotThreadSafe;

import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Test data generator for comprehensive CPU instruction testing.
 * <p>
 * This utility class provides methods to generate test data for binary and unary operations
 * across all possible value combinations or random subsets. It supports both 8-bit (Byte)
 * and 16-bit (Integer) operands.
 * <p>
 * The class offers two main categories of methods:
 * <ul>
 *   <li><b>forAll*</b> methods - Execute tests for all possible value combinations (exhaustive testing)</li>
 *   <li><b>forSome*</b> methods - Execute tests for a random subset of values (configurable via setRandomTestsCount)</li>
 * </ul>
 * <p>
 * <b>Usage examples:</b>
 * <pre>{@code
 * // Test an 8-bit ADD instruction with all possible combinations
 * Generator.forAll8bitBinary((first, second) -> {
 *     testBuilder.accept(first, second);
 * });
 * 
 * // Test with random subset (faster, still comprehensive)
 * Generator.setRandomTestsCount(100);
 * Generator.forSome16bitBinary((first, second) -> {
 *     testBuilder.accept(first, second);
 * });
 * 
 * // Test with conditional values
 * Generator.forAll16bitBinaryFirstSatisfying(
 *     value -> value > 0x8000,  // Only test with first operand > 0x8000
 *     (first, second) -> testBuilder.accept(first, second)
 * );
 * 
 * // Test unary operations (second operand is 0)
 * Generator.forAll8bitUnary((value, ignored) -> {
 *     testBuilder.accept(value, (byte) 0);
 * });
 * }</pre>
 */
@SuppressWarnings("unused")
@NotThreadSafe
public class Generator {
    private static final int MAX_16BIT_VALUE = 0xFFFF;
    private static int randomTests = 25;

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Generator() {
        throw new AssertionError("Utility class, do not instantiate");
    }

    /**
     * Sets the number of random test iterations to be used by 'forSome' methods.
     *
     * @param randomTests the non-negative number of random test iterations
     * @throws IllegalArgumentException if the count is negative
     */
    public static void setRandomTestsCount(int randomTests) {
        if (randomTests < 0) {
            throw new IllegalArgumentException("Random tests count must be >= 0");
        }
        Generator.randomTests = randomTests;
    }

    private static void validate16BitStart(int start, String operandName) {
        if (start < 0 || start > MAX_16BIT_VALUE) {
            throw new IllegalArgumentException(operandName + " start must be between 0 and " + MAX_16BIT_VALUE);
        }
    }

    private static int random16BitFrom(Random random, int start) {
        return start + random.nextInt(MAX_16BIT_VALUE - start + 1);
    }

    private static int[] matching16BitValues(Predicate<Integer> predicate, String operandName) {
        int[] values = IntStream.rangeClosed(0, MAX_16BIT_VALUE).filter(predicate::test).toArray();
        if (values.length == 0) {
            throw new IllegalArgumentException(operandName + " predicate does not match a 16-bit value");
        }
        return values;
    }

    /**
     * Executes the given runners for all possible combinations of two 8-bit values.
     * The second value starts from the first value to avoid duplicate combinations.
     *
     * @param runners the consumers to execute with each pair of byte values
     */
    @SafeVarargs
    public static void forAll8bitBinary(BiConsumer<Byte, Byte>... runners) {
        for (int i = 0; i < 256; i++) {
            for (int j = i; j < 256; j++) {
                for (BiConsumer<Byte, Byte> runner : runners) {
                    runner.accept((byte) i, (byte) j);
                }
            }
        }
    }

    /**
     * Executes the given runners for a random subset of 8-bit binary value combinations.
     * The number of iterations is determined by the random tests count.
     *
     * @param runners the consumers to execute with each pair of byte values
     */
    @SafeVarargs
    public static void forSome8bitBinary(BiConsumer<Byte, Byte>... runners) {
        Random random = new Random();
        for (int k = 0; k < randomTests; k++) {
            for (BiConsumer<Byte, Byte> runner : runners) {
                runner.accept((byte) random.nextInt(256), (byte) random.nextInt(256));
            }
        }
    }

    /**
     * Executes the given runners for all 8-bit values where both operands are equal.
     *
     * @param runners the consumers to execute with each pair of equal byte values
     */
    @SafeVarargs
    public static void forAll8bitBinaryWhichEqual(BiConsumer<Byte, Byte>... runners) {
        for (int i = 0; i < 256; i++) {
            for (BiConsumer<Byte, Byte> runner : runners) {
                runner.accept((byte) i, (byte) i);
            }
        }
    }

    /**
     * Executes the given runners for a random subset of 8-bit values where both operands are equal.
     * The number of iterations is determined by the random tests count.
     *
     * @param runners the consumers to execute with each pair of equal byte values
     */
    @SafeVarargs
    public static void forSome8bitBinaryWhichEqual(BiConsumer<Byte, Byte>... runners) {
        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Byte, Byte> runner : runners) {
                int k = random.nextInt(256);
                runner.accept((byte) k, (byte) k);
            }
        }
    }

    /**
     * Executes the given runners for all combinations of two 16-bit values starting from specified positions.
     *
     * @param firstStartFrom the starting value for the first operand (0-65535)
     * @param secondStartFrom the starting value for the second operand (0-65535)
     * @param runners the consumers to execute with each pair of integer values
     * @throws IllegalArgumentException if start values are outside 0..0xFFFF
     */
    @SafeVarargs
    public static void forAll16bitBinary(int firstStartFrom, int secondStartFrom, BiConsumer<Integer, Integer>... runners) {
        validate16BitStart(firstStartFrom, "First");
        validate16BitStart(secondStartFrom, "Second");

        for (int i = firstStartFrom; i < 65536; i++) {
            for (int j = secondStartFrom; j < 65536; j++) {
                for (BiConsumer<Integer, Integer> runner : runners) {
                    runner.accept(i, j);
                }
            }
        }
    }

    /**
     * Executes the given runners for a random subset of 16-bit binary value combinations starting from specified positions.
     * The number of iterations is determined by the random tests count.
     *
     * @param firstStartFrom the minimum value for the first operand (0-65535)
     * @param secondStartFrom the minimum value for the second operand (0-65535)
     * @param runners the consumers to execute with each pair of integer values
     * @throws IllegalArgumentException if start values are outside 0..0xFFFF
     */
    @SafeVarargs
    public static void forSome16bitBinary(int firstStartFrom, int secondStartFrom, BiConsumer<Integer, Integer>... runners) {
        validate16BitStart(firstStartFrom, "First");
        validate16BitStart(secondStartFrom, "Second");

        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                int first = random16BitFrom(random, firstStartFrom);
                int second = random16BitFrom(random, secondStartFrom);
                runner.accept(first, second);
            }
        }
    }

    /**
     * Executes the given runners for all combinations of two 16-bit values starting from the specified first position.
     * The second operand starts from 0.
     *
     * @param firstStartFrom the starting value for the first operand (0-65535)
     * @param runners the consumers to execute with each pair of integer values
     */
    @SafeVarargs
    public static void forAll16bitBinary(int firstStartFrom, BiConsumer<Integer, Integer>... runners) {
        forAll16bitBinary(firstStartFrom, 0, runners);
    }

    /**
     * Executes the given runners for a random subset of 16-bit binary value combinations starting from the specified first position.
     * The second operand starts from 0. The number of iterations is determined by the random tests count.
     *
     * @param firstStartFrom the minimum value for the first operand (0-65535)
     * @param runners the consumers to execute with each pair of integer values
     */
    @SafeVarargs
    public static void forSome16bitBinary(int firstStartFrom, BiConsumer<Integer, Integer>... runners) {
        forSome16bitBinary(firstStartFrom, 0, runners);
    }

    /**
     * Executes the given runners for all possible combinations of two 16-bit values.
     *
     * @param runners the consumers to execute with each pair of integer values
     */
    @SafeVarargs
    public static void forAll16bitBinary(BiConsumer<Integer, Integer>... runners) {
        forAll16bitBinary(0, 0, runners);
    }

    /**
     * Executes the given runners for a random subset of 16-bit binary value combinations.
     * The number of iterations is determined by the random tests count.
     *
     * @param runners the consumers to execute with each pair of integer values
     */
    @SafeVarargs
    public static void forSome16bitBinary(BiConsumer<Integer, Integer>... runners) {
        forSome16bitBinary(0, 0, runners);
    }

    /**
     * Executes the given runners for all combinations where the first 16-bit operand satisfies the given predicate.
     *
     * @param predicate the condition that the first operand must satisfy
     * @param runners the consumers to execute with each pair of integer values
     * @throws IllegalArgumentException if the predicate matches no 16-bit value
     */
    @SafeVarargs
    public static void forAll16bitBinaryFirstSatisfying(Predicate<Integer> predicate,
                                                        BiConsumer<Integer, Integer>... runners) {
        for (int i = 0; i < 65536; i++) {
            if (predicate.test(i)) {
                for (int j = 0; j < 65536; j++) {
                    for (BiConsumer<Integer, Integer> runner : runners) {
                        runner.accept(i, j);
                    }
                }
            }
        }
    }

    /**
     * Executes the given runners for a random subset where the first 16-bit operand satisfies the given predicate.
     * The number of iterations is determined by the random tests count.
     *
     * @param predicate the condition that the first operand must satisfy
     * @param runners the consumers to execute with each pair of integer values
     */
    @SafeVarargs
    public static void forSome16bitBinaryFirstSatisfying(Predicate<Integer> predicate,
                                                         BiConsumer<Integer, Integer>... runners) {
        if (randomTests == 0) {
            return;
        }
        int[] matchingValues = matching16BitValues(predicate, "First");
        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                int first = matchingValues[random.nextInt(matchingValues.length)];
                runner.accept(first, random.nextInt(MAX_16BIT_VALUE + 1));
            }
        }
    }

    /**
     * Executes the given runners for a random subset where both 16-bit operands satisfy their respective predicates.
     * The number of iterations is determined by the random tests count.
     *
     * @param firstP the condition that the first operand must satisfy
     * @param secondP the condition that the second operand must satisfy
     * @param runners the consumers to execute with each pair of integer values
     * @throws IllegalArgumentException if either predicate matches no 16-bit value
     */
    @SafeVarargs
    public static void forSome16bitBinaryBothSatisfying(Predicate<Integer> firstP, Predicate<Integer> secondP,
                                                        BiConsumer<Integer, Integer>... runners) {
        if (randomTests == 0) {
            return;
        }
        int[] firstValues = matching16BitValues(firstP, "First");
        int[] secondValues = matching16BitValues(secondP, "Second");
        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                int first = firstValues[random.nextInt(firstValues.length)];
                int second = secondValues[random.nextInt(secondValues.length)];
                runner.accept(first, second);
            }
        }
    }

    /**
     * Executes the given runners for all combinations where both 16-bit operands satisfy their respective predicates.
     *
     * @param firstP the condition that the first operand must satisfy
     * @param secondP the condition that the second operand must satisfy
     * @param runners the consumers to execute with each pair of integer values
     */
    @SafeVarargs
    public static void forAll16bitBinaryBothSatisfying(Predicate<Integer> firstP, Predicate<Integer> secondP,
                                                       BiConsumer<Integer, Integer>... runners) {
        for (int i = 0; i < 65536; i++) {
            if (firstP.test(i)) {
                for (int j = 0; j < 65536; j++) {
                    if (secondP.test(j)) {
                        for (BiConsumer<Integer, Integer> runner : runners) {
                            runner.accept(i, j);
                        }
                    }
                }
            }
        }
    }

    /**
     * Executes the given runners for all 16-bit values where both operands are equal.
     *
     * @param runners the consumers to execute with each pair of equal integer values
     */
    @SafeVarargs
    public static void forAll16bitBinaryWhichEqual(BiConsumer<Integer, Integer>... runners) {
        for (int i = 0; i < 65536; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                runner.accept(i, i);
            }
        }
    }

    /**
     * Executes the given runners for a random subset of 16-bit values where both operands are equal.
     * The number of iterations is determined by the random tests count.
     *
     * @param runners the consumers to execute with each pair of equal integer values
     */
    @SafeVarargs
    public static void forSome16bitBinaryWhichEqual(BiConsumer<Integer, Integer>... runners) {
        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                int k = random.nextInt(MAX_16BIT_VALUE + 1);
                runner.accept(k, k);
            }
        }
    }

    /**
     * Executes the given runners for all 8-bit values with a unary operation (second operand is always 0).
     *
     * @param runners the consumers to execute with each byte value and 0
     */
    @SafeVarargs
    public static void forAll8bitUnary(BiConsumer<Byte, Byte>... runners) {
        for (int i = 0; i < 256; i++) {
            for (BiConsumer<Byte, Byte> runner : runners) {
                runner.accept((byte) i, (byte) 0);
            }
        }
    }

    /**
     * Executes the given runners for a random subset of 8-bit values with a unary operation (second operand is always 0).
     * The number of iterations is determined by the random tests count.
     *
     * @param runners the consumers to execute with each byte value and 0
     */
    @SafeVarargs
    public static void forSome8bitUnary(BiConsumer<Byte, Byte>... runners) {
        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Byte, Byte> runner : runners) {
                int k = random.nextInt(256);
                runner.accept((byte) k, (byte) 0);
            }
        }
    }

    /**
     * Executes the given runners for a random subset of 16-bit values with a unary operation (second operand is always 0).
     * The number of iterations is determined by the random tests count.
     *
     * @param runners the consumers to execute with each integer value and 0
     */
    @SafeVarargs
    public static void forSome16bitUnary(BiConsumer<Integer, Integer>... runners) {
        forSome16bitUnary(0, runners);
    }

    /**
     * Executes the given runners for a random subset of 16-bit values with a unary operation starting from the specified position.
     * The second operand is always 0. The number of iterations is determined by the random tests count.
     *
     * @param firstStartFrom the minimum value for the first operand (0-65535)
     * @param runners the consumers to execute with each integer value and 0
     * @throws IllegalArgumentException if the start value is outside 0..0xFFFF
     */
    @SafeVarargs
    public static void forSome16bitUnary(int firstStartFrom, BiConsumer<Integer, Integer>... runners) {
        validate16BitStart(firstStartFrom, "First");
        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                int first = random16BitFrom(random, firstStartFrom);
                runner.accept(first, 0);
            }
        }
    }

    /**
     * Executes the given runners for all 16-bit values with a unary operation starting from the specified position.
     * The second operand is always 0.
     *
     * @param firstStartFrom the starting value for the first operand (0-65535)
     * @param runners the consumers to execute with each integer value and 0
     * @throws IllegalArgumentException if the start value is outside 0..0xFFFF
     */
    @SafeVarargs
    public static void forAll16bitUnary(int firstStartFrom, BiConsumer<Integer, Integer>... runners) {
        validate16BitStart(firstStartFrom, "First");

        for (int i = firstStartFrom; i <= MAX_16BIT_VALUE; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                runner.accept(i, 0);
            }
        }
    }

    /**
     * Executes the given runners once with the same operand for both first and second parameters.
     *
     * @param <T> the number type of the operand
     * @param operand the operand value to use for both parameters
     * @param runners the consumers to execute with the given operand
     */
    @SafeVarargs
    public static <T extends Number> void forGivenOperandsAndSingleRun(T operand, BiConsumer<T, T>... runners) {
        for (BiConsumer<T, T> runner : runners) {
            runner.accept(operand, operand);
        }
    }

    /**
     * Executes the given runners once with the specified first and second operands.
     *
     * @param <T> the number type of the operands
     * @param first the first operand value
     * @param second the second operand value
     * @param runners the consumers to execute with the given operands
     */
    @SafeVarargs
    public static <T extends Number> void forGivenOperandsAndSingleRun(T first, T second, BiConsumer<T, T>... runners) {
        for (BiConsumer<T, T> runner : runners) {
            runner.accept(first, second);
        }
    }
}
