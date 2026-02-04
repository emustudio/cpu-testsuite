/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite;

import net.emustudio.cpu.testsuite.internal.RunStateListenerStub;
import net.emustudio.cpu.testsuite.memory.MemoryStub;
import net.emustudio.emulib.plugins.cpu.CPU;
import net.jcip.annotations.NotThreadSafe;

import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * CPU Runner.
 * <p>
 * This class is a wrapper around CPU and memory, and contains the environment for test execution.
 *
 * @param <TCpu> CPU type
 */
@SuppressWarnings("unused")
@NotThreadSafe
public abstract class CpuRunner<TCpu extends CPU> {
    /**
     * Minimum memory size in bytes. Set to 64KB to accommodate most CPU architectures
     * that use 16-bit addressing.
     */
    public static final int MIN_MEMORY_SIZE = 65536;

    private final RunStateListenerStub runStateListener = new RunStateListenerStub();
    /**
     * The CPU instance being tested.
     */
    protected final TCpu cpu;
    /**
     * The memory stub used for reading and writing memory during tests.
     */
    protected final MemoryStub<?> memoryStub;

    private short[] program = new short[1];
    private CPU.RunState expectedRunState = CPU.RunState.STATE_STOPPED_BREAK;

    /**
     * Creates a new CPU runner with the specified CPU and memory stub.
     *
     * @param cpu the CPU instance to test
     * @param memoryStub the memory stub for test execution
     */
    public CpuRunner(TCpu cpu, MemoryStub<?> memoryStub) {
        this.cpu = Objects.requireNonNull(cpu);
        this.memoryStub = Objects.requireNonNull(memoryStub);
        cpu.addCPUListener(runStateListener);
    }

    /**
     * Ensures the program memory is at least the specified size.
     * If the current program size is smaller, it expands the program array while preserving existing content.
     *
     * @param length the minimum required program size in bytes
     */
    public void ensureProgramSize(int length) {
        length = Math.max(length, MIN_MEMORY_SIZE);
        if (program.length < length) {
            // Preserve existing memory content
            short[] newProgram = new short[length];
            int memSize = Math.min(length, memoryStub.getSize());
            for (int i = 0; i < memSize; i++) {
                Number cell = memoryStub.read(i);
                newProgram[i] = cell.shortValue();
            }
            this.program = newProgram;
            resetProgram();
        }
    }

    /**
     * Sets the program from an integer array and resets the program in memory.
     *
     * @param program the program bytes as integers
     */
    public void setProgram(int... program) {
        ensureProgramSize(program.length);
        for (int i = 0; i < program.length; i++) {
            this.program[i] = (short) program[i];
        }
        resetProgram();
    }

    /**
     * Sets the program from a list of numbers and resets the program in memory.
     *
     * @param program the program bytes as a list of numbers
     */
    public void setProgram(List<? extends Number> program) {
        int[] array = new int[program.size()];

        int i = 0;
        for (Number n : program) {
            array[i++] = n.intValue();
        }
        setProgram(array);
    }

    /**
     * Sets the program from a short array and resets the program in memory.
     *
     * @param program the program bytes as shorts
     */
    public void setProgram(short... program) {
        ensureProgramSize(program.length);
        System.arraycopy(program, 0, this.program, 0, program.length);
        resetProgram();
    }

    /**
     * Replaces the current program with a new one and resets it in memory.
     *
     * @param program the new program bytes as shorts
     */
    public void resetProgram(short... program) {
        this.program = program;
        resetProgram();
    }

    /**
     * Sets a byte value at the specified memory address.
     * Ensures program size is sufficient and updates both program array and memory stub.
     *
     * @param address the memory address to write to
     * @param value the byte value to write (truncated to 8 bits)
     */
    @SuppressWarnings("unchecked") // Safe: type checked via getCellTypeClass()
    public void setByte(int address, int value) {
        ensureProgramSize(address + 1);
        program[address] = (short) (value & 0xFF);
        if (memoryStub.getCellTypeClass() == Byte.class) {
            ((MemoryStub<Byte>) memoryStub).write(address, (byte) (value & 0xFF));
        } else if (memoryStub.getCellTypeClass() == Short.class) {
            ((MemoryStub<Short>) memoryStub).write(address, (short) (value & 0xFF));
        }
    }

    private void resetProgram() {
        memoryStub.setMemory(program);
    }

    /**
     * Resets the CPU to its initial state.
     */
    public void reset() {
        cpu.reset();
    }

    /**
     * Sets the expected run state that should be reached after the next step execution.
     *
     * @param runState the expected CPU run state
     */
    public void expectRunState(CPU.RunState runState) {
        this.expectedRunState = Objects.requireNonNull(runState);
    }

    /**
     * Executes a single CPU instruction step and verifies the run state matches the expected state.
     *
     * @throws AssertionError if the actual run state doesn't match the expected state
     */
    public void step() {
        cpu.step();
        System.out.flush();
        assertEquals("PC=" + getPC(), expectedRunState, runStateListener.runState);
    }

    /**
     * Returns the current value of the program counter (instruction pointer).
     *
     * @return the program counter value
     */
    public abstract int getPC();

    /**
     * Returns the current value of the stack pointer.
     *
     * @return the stack pointer value
     */
    public abstract int getSP();

    /**
     * Returns the values of all CPU registers.
     *
     * @return a list of register values
     */
    public abstract List<Integer> getRegisters();

    /**
     * Sets the value of a specific CPU register.
     *
     * @param register the register index
     * @param value the value to set
     */
    public abstract void setRegister(int register, int value);

    /**
     * Sets the CPU flags to the specified mask.
     *
     * @param mask the flags mask to set
     */
    public abstract void setFlags(int mask);

    /**
     * Returns the current CPU flags value.
     *
     * @return the flags mask
     */
    public abstract int getFlags();

}
