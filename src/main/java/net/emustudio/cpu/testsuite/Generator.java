// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.cpu.testsuite;

import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class Generator {
    // Contract: Memory size is 16-bit at max
    private static final int MAX_OPERAND_SIZE = 0xFFFF;
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
                runner.accept((byte)i, (byte)i);
            }
        }
    }

    @SafeVarargs
    public static void forSome8bitBinaryWhichEqual(BiConsumer<Byte, Byte>... runners) {
        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Byte, Byte> runner : runners) {
                int k = random.nextInt(256);
                runner.accept((byte)k, (byte)k);
            }
        }
    }

    @SafeVarargs
    public static void forSome16bitBinary(int firstStartFrom, int secondStartFrom, BiConsumer<Integer, Integer>... runners) {
        if (firstStartFrom > MAX_OPERAND_SIZE) {
            throw new IllegalArgumentException("First start from must be <= " + MAX_OPERAND_SIZE);
        }
        if (secondStartFrom > MAX_OPERAND_SIZE) {
            throw new IllegalArgumentException("Second start from must be <= " + MAX_OPERAND_SIZE);
        }

        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                int first = random.nextInt(MAX_OPERAND_SIZE);
                if (first < firstStartFrom) {
                    first = firstStartFrom;
                }
                int second = random.nextInt(MAX_OPERAND_SIZE);
                if (second < secondStartFrom) {
                    second = secondStartFrom;
                }
                runner.accept(first, second);
            }
        }
    }

    @SafeVarargs
    public static void forSome16bitBinary(int firstStartFrom, BiConsumer<Integer, Integer>... runners) {
        forSome16bitBinary(firstStartFrom, 0, runners);
    }

    @SafeVarargs
    public static void forSome16bitBinaryFirstSatisfying(Predicate<Integer> predicate,
                                                         BiConsumer<Integer, Integer>... runners) {
        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                int first = random.nextInt(MAX_OPERAND_SIZE);
                while(!predicate.test(first)) {
                    first = random.nextInt(MAX_OPERAND_SIZE);
                }
                runner.accept(first, random.nextInt(MAX_OPERAND_SIZE));
            }
        }
    }

    @SafeVarargs
    public static void forSome16bitBinaryBothSatisfying(Predicate<Integer> firstP, Predicate<Integer> secondP,
                                                        BiConsumer<Integer, Integer>... runners) {
        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                int first = random.nextInt(MAX_OPERAND_SIZE);
                while(!firstP.test(first)) {
                    first = random.nextInt(MAX_OPERAND_SIZE);
                }
                int second = random.nextInt(MAX_OPERAND_SIZE);
                while (!secondP.test(second)) {
                    second = random.nextInt(MAX_OPERAND_SIZE);
                }
                runner.accept(first, second);
            }
        }
    }

    @SafeVarargs
    public static void forSome16bitBinary(BiConsumer<Integer, Integer>... runners) {
        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                runner.accept(random.nextInt(MAX_OPERAND_SIZE), random.nextInt(MAX_OPERAND_SIZE));
            }
        }
    }

    @SafeVarargs
    public static void forSome16bitBinaryWhichEqual(BiConsumer<Integer, Integer>... runners) {
        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                int k = random.nextInt(MAX_OPERAND_SIZE);
                runner.accept(k, k);
            }
        }
    }

    @SafeVarargs
    public static void forAll8bitUnary(BiConsumer<Byte, Byte>... runners) {
        for (int i = 0; i < 256; i++) {
            for (BiConsumer<Byte, Byte> runner : runners) {
                runner.accept((byte)i, (byte)0);
            }
        }
    }

    @SafeVarargs
    public static void forSome8bitUnary(BiConsumer<Byte, Byte>... runners) {
        Random random = new Random();
        for (int i = 0; i < randomTests; i++) {
            for (BiConsumer<Byte, Byte> runner : runners) {
                int k = random.nextInt(256);
                runner.accept((byte)k, (byte)0);
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
                int first = random.nextInt(0xFFFF);
                if (first < firstStartFrom) {
                    first += firstStartFrom;
                }
                if (first > 0xFFFF) {
                    first = 0xFFFF;
                }
                runner.accept(first, 0);
            }
        }
    }

    @SafeVarargs
    public static void forAll16bitUnary(int firstStartFrom, BiConsumer<Integer, Integer>... runners) {
        if (firstStartFrom > MAX_OPERAND_SIZE) {
            throw new IllegalArgumentException("First start from must be <=" + MAX_OPERAND_SIZE);
        }

        for (int i = firstStartFrom; i < 0xffff; i++) {
            for (BiConsumer<Integer, Integer> runner : runners) {
                runner.accept(i, 0);
            }
        }
    }

    @SafeVarargs
    public static <T extends Number> void forGivenOperandsAndSingleRun(T operand, BiConsumer<T, T>... runners) {
        for (BiConsumer<T,T> runner : runners) {
            runner.accept(operand, operand);
        }
    }

    @SafeVarargs
    public static <T extends Number> void forGivenOperandsAndSingleRun(T first, T second, BiConsumer<T, T>... runners) {
        for (BiConsumer<T,T> runner : runners) {
            runner.accept(first, second);
        }
    }
}
