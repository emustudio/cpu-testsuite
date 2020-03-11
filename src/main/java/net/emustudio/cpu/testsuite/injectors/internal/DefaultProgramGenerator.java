// SPDX-License-Identifier: GPL-3.0-or-later
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
