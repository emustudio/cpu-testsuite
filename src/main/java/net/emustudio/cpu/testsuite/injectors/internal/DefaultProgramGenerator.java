/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.injectors.internal;

import net.emustudio.cpu.testsuite.internal.WordLayout;
import net.jcip.annotations.NotThreadSafe;

import java.util.ArrayList;
import java.util.List;

/**
 * Default program generator for creating instruction byte sequences.
 * <p>
 * This class assembles a complete program from opcodes, operands, and optional opcodes-after-operands.
 * It handles both Byte and Integer operand types, converting them appropriately to the instruction stream.
 *
 * @param <TOperand> the type of operands (Byte or Integer)
 */
@NotThreadSafe
public class DefaultProgramGenerator<TOperand extends Number> {
    private final List<Short> opcodes = new ArrayList<>();
    private final List<Short> opcodesAfterOperand = new ArrayList<>();

    /**
     * Creates a new program generator.
     */
    public DefaultProgramGenerator() {
    }

    /**
     * Adds opcodes to the instruction.
     *
     * @param opcodes the opcode bytes to add
     */
    public void addOpcodes(int... opcodes) {
        this.opcodes.addAll(intArrayToList(opcodes));
    }

    /**
     * Adds opcodes to be placed after the operands.
     *
     * @param opcodes the opcode bytes to add after operands
     */
    public void addOpcodesAfterOperands(int... opcodes) {
        this.opcodesAfterOperand.addAll(intArrayToList(opcodes));
    }

    private static List<Short> intArrayToList(int... things) {
        List<Short> tmpList = new ArrayList<>();
        for (int thing : things) {
            tmpList.add((short)thing);
        }
        return tmpList;
    }

    /**
     * Generates the complete program by combining opcodes, operands, and opcodes-after-operands.
     *
     * @param wordReadingStrategy memory stub strategy for encoding Integer operands
     * @param operands the operand values (Byte or Integer) for this program
     * @return a list of Short values representing the generated program
     * @throws IllegalStateException if operand type is not Byte or Integer
     */
    @SafeVarargs
    public final List<Short> generate(int wordReadingStrategy, TOperand... operands) {
        List<Short> program = new ArrayList<>(opcodes);
        for (TOperand operand : operands) {
            if (operand instanceof Byte) {
                program.add((short)(operand.byteValue() & 0xFF));
            } else if (operand instanceof Integer) {
                for (short value : WordLayout.encode(operand.intValue(), wordReadingStrategy)) {
                    program.add(value);
                }
            } else {
                throw new IllegalStateException("Operand type can be either Byte or Integer");
            }
        }
        program.addAll(opcodesAfterOperand);

        return program;
    }

    /**
     * Returns the memory length needed when an operand addresses a two-byte value.
     * Byte addresses are unsigned; Integer addresses use their low 16 bits.
     *
     * @param operand the operand used as an address
     * @return minimum memory length covering both addressed bytes
     */
    public static int requiredMemorySize(Number operand) {
        int mask = operand instanceof Byte ? 0xFF : 0xFFFF;
        return (operand.intValue() & mask) + 2;
    }

    /**
     * Returns a string representation of the configured opcodes before and after operands.
     *
     * @return a string representation of the instruction
     */
    @Override
    public String toString() {
        return String.format("instruction: %s%s",
            Utils.toHexString(opcodes.toArray()),
            Utils.toHexString(opcodesAfterOperand.toArray()));
    }
}
