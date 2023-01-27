/*
 * This file is part of cpu-testsuite.
 *
 * Copyright (C) 2017-2023  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.cpu.testsuite;

import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class Generator {
    private static final int MAX_16BIT_VALUE = 0xFFFF;
    private static int randomTests = 25;

    public static void setRandomTestsCount(int randomTests) {
        Generator.randomTests = randomTests;
    }

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

    @SafeVarargs
    public static void forSome8bitBinary(BiConsumer<Byte, Byte>... runners) {
        Random random = new Random();
        for (int k = 0; k < randomTests; k++) {
            for (BiConsumer<Byte, Byte> runner : runners) {
                runner.accept((byte) random.nextInt(256), (byte) random.nextInt(256));
            }
        }
    }

    @SafeVarargs
    public static void forAll8bitBinaryWhichEqual(BiConsumer<Byte, Byte>... runners) {
        for (int i = 0; i < 256; i++) {
            for (BiConsumer<Byte, Byte> runner : runners) {
                runner.accept((byte) i, (byte) i);
            }
        }
    }

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

    @SafeVarargs
    public static void forAll16bitBinary(int firstStartFrom, int secondStartFrom, BiConsumer<Integer, Integer>... runners) {
        if (firstStartFrom > MAX_16BIT_VALUE) {
            throw new IllegalArgumentException("First start from must be <= " + MAX_16BIT_VALUE);
        }
        if (secondStartFrom > MAX_16BIT_VALUE) {
            throw new IllegalArgumentException("Second start from must be <= " + MAX_16BIT_VALUE);
        }

        for (int i = firstStartFrom; i < 65536; i++) {
            for (int j = secondStartFrom; j < 65536; j++) {
                for (BiConsumer<Integer, Integer> runner : runners) {
                    runner.accept(i, j);
                }
            }
        }
    }

    @SafeVarargs
    public static void forSome16bitBinary(int firstStartFrom, int secondStartFrom, BiConsumer<Integer, Integer>... runners) {
        if (firstStartFrom > MAX_16BIT_VALUE) {
            throw new IllegalArgumentException("First start from must be <= " + MAX_16BIT_VALUE);
        }
        if (secondStartFrom > MAX_16BIT_VALUE) {
            throw new IllegalArgumentException("Second start from must be <= " + MAX_16BIT_VALUE);
        }

        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                int first = random.nextInt(MAX_16BIT_VALUE);
                if (first < firstStartFrom) {
                    first = firstStartFrom;
                }
                int second = random.nextInt(MAX_16BIT_VALUE);
                if (second < secondStartFrom) {
                    second = secondStartFrom;
                }
                runner.accept(first, second);
            }
        }
    }

    @SafeVarargs
    public static void forAll16bitBinary(int firstStartFrom, BiConsumer<Integer, Integer>... runners) {
        forAll16bitBinary(firstStartFrom, 0, runners);
    }

    @SafeVarargs
    public static void forSome16bitBinary(int firstStartFrom, BiConsumer<Integer, Integer>... runners) {
        forSome16bitBinary(firstStartFrom, 0, runners);
    }

    @SafeVarargs
    public static void forAll16bitBinary(BiConsumer<Integer, Integer>... runners) {
        forAll16bitBinary(0, 0, runners);
    }

    @SafeVarargs
    public static void forSome16bitBinary(BiConsumer<Integer, Integer>... runners) {
        forSome16bitBinary(0, 0, runners);
    }

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

    @SafeVarargs
    public static void forSome16bitBinaryFirstSatisfying(Predicate<Integer> predicate,
                                                         BiConsumer<Integer, Integer>... runners) {
        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                int first = random.nextInt(MAX_16BIT_VALUE);
                while (!predicate.test(first)) {
                    first = random.nextInt(MAX_16BIT_VALUE);
                }
                runner.accept(first, random.nextInt(MAX_16BIT_VALUE));
            }
        }
    }

    @SafeVarargs
    public static void forSome16bitBinaryBothSatisfying(Predicate<Integer> firstP, Predicate<Integer> secondP,
                                                        BiConsumer<Integer, Integer>... runners) {
        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                int first = random.nextInt(MAX_16BIT_VALUE);
                while (!firstP.test(first)) {
                    first = random.nextInt(MAX_16BIT_VALUE);
                }
                int second = random.nextInt(MAX_16BIT_VALUE);
                while (!secondP.test(second)) {
                    second = random.nextInt(MAX_16BIT_VALUE);
                }
                runner.accept(first, second);
            }
        }
    }

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

    @SafeVarargs
    public static void forAll16bitBinaryWhichEqual(BiConsumer<Integer, Integer>... runners) {
        for (int i = 0; i < 65536; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                runner.accept(i, i);
            }
        }
    }

    @SafeVarargs
    public static void forSome16bitBinaryWhichEqual(BiConsumer<Integer, Integer>... runners) {
        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                int k = random.nextInt(MAX_16BIT_VALUE);
                runner.accept(k, k);
            }
        }
    }

    @SafeVarargs
    public static void forAll8bitUnary(BiConsumer<Byte, Byte>... runners) {
        for (int i = 0; i < 256; i++) {
            for (BiConsumer<Byte, Byte> runner : runners) {
                runner.accept((byte) i, (byte) 0);
            }
        }
    }

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

    @SafeVarargs
    public static void forSome16bitUnary(BiConsumer<Integer, Integer>... runners) {
        forSome16bitUnary(0, runners);
    }

    @SafeVarargs
    public static void forSome16bitUnary(int firstStartFrom, BiConsumer<Integer, Integer>... runners) {
        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                int first = random.nextInt(MAX_16BIT_VALUE + 1);
                if (first < firstStartFrom) {
                    first += firstStartFrom;
                }
                runner.accept(first, 0);
            }
        }
    }

    @SafeVarargs
    public static void forAll16bitUnary(int firstStartFrom, BiConsumer<Integer, Integer>... runners) {
        if (firstStartFrom > MAX_16BIT_VALUE) {
            throw new IllegalArgumentException("First start from must be <=" + MAX_16BIT_VALUE);
        }

        for (int i = firstStartFrom; i <= MAX_16BIT_VALUE; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                runner.accept(i, 0);
            }
        }
    }

    @SafeVarargs
    public static <T extends Number> void forGivenOperandsAndSingleRun(T operand, BiConsumer<T, T>... runners) {
        for (BiConsumer<T, T> runner : runners) {
            runner.accept(operand, operand);
        }
    }

    @SafeVarargs
    public static <T extends Number> void forGivenOperandsAndSingleRun(T first, T second, BiConsumer<T, T>... runners) {
        for (BiConsumer<T, T> runner : runners) {
            runner.accept(first, second);
        }
    }
}
