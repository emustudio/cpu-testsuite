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
package net.emustudio.cpu.testsuite.injectors.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultProgramGenerator<TOperand extends Number> {
    private final List<Short> opcodes = new ArrayList<>();
    private final List<TOperand> operands  = new ArrayList<>();
    private final List<Short> opcodesAfterOperand = new ArrayList<>();

    public void addOpcodes(int... opcodes) {
        this.opcodes.addAll(intArrayToList(opcodes));
    }

    @SafeVarargs
    public final void setOperands(TOperand... operands) {
        this.operands.addAll(Arrays.asList(operands));
    }

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

    public void clearOperands() {
        operands.clear();
    }

    @Override
    public String toString() {
        return String.format("instruction: %s%s%s",
            Utils.toHexString(opcodes.toArray()),
            Utils.toHexString(operands.toArray()),
            Utils.toHexString(opcodesAfterOperand.toArray()));
    }
}
