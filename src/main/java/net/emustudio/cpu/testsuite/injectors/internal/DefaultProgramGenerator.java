/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite.injectors.internal;

import net.jcip.annotations.NotThreadSafe;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final List<TOperand> operands  = new ArrayList<>();
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
     * Sets the operands for the instruction.
     *
     * @param operands the operand values (Byte or Integer)
     */
    @SafeVarargs
    public final void setOperands(TOperand... operands) {
        this.operands.addAll(Arrays.asList(operands));
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
     * @return a list of Short values representing the generated program
     * @throws IllegalStateException if operand type is not Byte or Integer
     */
    public List<Short> generate() {

        List<Short> program = new ArrayList<>(opcodes);
        for (TOperand operand : operands) {
            if (operand instanceof Byte) {
                program.add((short)(operand.byteValue() & 0xFF));
            } else if (operand instanceof Integer) {
                program.add((short)(operand.shortValue() & 0xFF));
                program.add((short)((operand.shortValue() >>> 8) & 0xFF));
            } else {
                throw new IllegalStateException("Operand type can be either Byte or Integer");
            }
        }
        program.addAll(opcodesAfterOperand);

        return program;
    }

    /**
     * Clears all operands from the program generator.
     */
    public void clearOperands() {
        operands.clear();
    }

    /**
     * Returns a string representation of the instruction showing opcodes, operands, and opcodes-after-operands.
     *
     * @return a string representation of the instruction
     */
    @Override
    public String toString() {
        return String.format("instruction: %s%s%s",
            Utils.toHexString(opcodes.toArray()),
            Utils.toHexString(operands.toArray()),
            Utils.toHexString(opcodesAfterOperand.toArray()));
    }
}
