/* SPDX-FileCopyrightText: 2017-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.cpu.testsuite;

import net.emustudio.cpu.testsuite.verifiers.FlagsVerifier;
import net.emustudio.cpu.testsuite.verifiers.MemoryByteVerifier;
import net.emustudio.cpu.testsuite.verifiers.MemoryWordVerifier;
import net.emustudio.cpu.testsuite.injectors.MemoryAddress;
import net.emustudio.cpu.testsuite.injectors.MemoryByte;
import net.emustudio.cpu.testsuite.injectors.MemoryWord;
import net.emustudio.cpu.testsuite.injectors.NoOperInstr;
import net.emustudio.cpu.testsuite.injectors.OneOperInstr;
import net.emustudio.cpu.testsuite.injectors.TwoOperInstr;
import net.jcip.annotations.NotThreadSafe;

import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Abstract base class for building and configuring CPU instruction tests.
 * <p>
 * TestBuilder provides a fluent API for setting up test scenarios including:
 * <ul>
 *   <li>Configuring operands and their sources (memory, registers, immediate values)</li>
 *   <li>Setting up pre-conditions (flags, register values, memory contents)</li>
 *   <li>Executing instructions with various operand combinations</li>
 *   <li>Verifying post-conditions (flags, memory, register values)</li>
 * </ul>
 * <p>
 * The class uses method chaining to allow readable test construction. Concrete implementations
 * should extend this class and provide CPU-specific instruction execution methods.
 * <p>
 * <b>Usage example:</b>
 * <pre>{@code
 * // Create a test builder for an 8-bit ADD instruction
 * testBuilder
 *     .firstIsMemoryByteAt(0x1000)     // First operand from memory[0x1000]
 *     .secondIsMemoryByteAt(0x1001)     // Second operand from memory[0x1001]
 *     .verifyFlags(flags -> flags       // Verify CPU flags
 *         .expectCarry()
 *         .expectZero())
 *     .verifyByte(0x2000)               // Verify result at memory[0x2000]
 *     .runWithBothOperands(0x80, 0x05); // Execute instruction with opcodes
 * 
 * // Run the test with all 8-bit value combinations
 * Generator.forAll8bitBinary((first, second) -> {
 *     testBuilder.accept(first, second);
 * });
 * }</pre>
 *
 * @param <TOperand> the type of operands (Byte or Integer)
 * @param <TTestBuilder> the concrete builder type for method chaining
 * @param <TCpuRunner> the CPU runner type
 * @param <TCpuVerifier> the CPU verifier type
 */
@SuppressWarnings({"unused", "unchecked"})
@NotThreadSafe
public abstract class TestBuilder<TOperand extends Number,
        TTestBuilder extends TestBuilder<TOperand, TTestBuilder, TCpuRunner, TCpuVerifier>,
        TCpuRunner extends CpuRunner<?>, TCpuVerifier extends CpuVerifier> {
    /**
     * The CPU runner used for manipulating CPU state during test execution.
     */
    protected final TCpuRunner cpuRunner;
    /**
     * The CPU verifier used for checking CPU state after test execution.
     */
    protected final TCpuVerifier cpuVerifier;
    /**
     * The test runner responsible for executing tests with injectors and verifiers.
     */
    protected final TestRunner<TCpuRunner, TOperand> runner;

    /**
     * The last operation set for verification purposes.
     * Used by verification methods to check memory or flags against operation results.
     */
    protected Function<RunnerContext<TOperand>, Integer> lastOperation;

    /**
     * Creates a new test builder with the specified CPU runner and verifier.
     *
     * @param cpuRunner the CPU runner for manipulating CPU state
     * @param cpuVerifier the CPU verifier for checking results
     */
    protected TestBuilder(TCpuRunner cpuRunner, TCpuVerifier cpuVerifier) {
        this.cpuRunner = Objects.requireNonNull(cpuRunner);
        this.cpuVerifier = Objects.requireNonNull(cpuVerifier);
        this.runner = new TestRunner<>(cpuRunner);
    }

    /**
     * Clears all verifiers from the test runner.
     *
     * @return this test builder instance for method chaining
     */
    public TTestBuilder clearAllVerifiers() {
        runner.clearAllVerifiers();
        return (TTestBuilder) this;
    }

    /**
     * Adds custom verifiers to be executed after the test runs.
     *
     * @param verifiers the verifier consumers to execute after test completion
     * @return this test builder instance for method chaining
     */
    public TTestBuilder verifyAll(Consumer<RunnerContext<TOperand>>... verifiers) {
        runner.verifyAfterTest(verifiers);
        return (TTestBuilder) this;
    }

    /**
     * Sets the specified register to a random value between 0 and maxValue (inclusive).
     *
     * @param register the register index to set
     * @param maxValue the maximum random value (inclusive)
     * @return this test builder instance for method chaining
     */
    public TTestBuilder registerIsRandom(int register, int maxValue) {
        Random random = new Random();
        runner.injectFirst((tmpRunner, argument) -> cpuRunner.setRegister(register, random.nextInt(maxValue + 1)));
        return (TTestBuilder) this;
    }

    /**
     * Prints the value of the specified register during test execution.
     *
     * @param register the register index to print
     * @return this test builder instance for method chaining
     */
    public TTestBuilder printRegister(int register) {
        runner.injectTwoOperands((runner, first, second) ->
                System.out.printf("REG_%d=%x%n", register, runner.getRegisters().get(register))
        );
        return (TTestBuilder) this;
    }

    /**
     * Prints the first and second operand values during test execution.
     *
     * @return this test builder instance for method chaining
     */
    public TTestBuilder printOperands() {
        runner.injectTwoOperands((runner, first, second) ->
                System.out.printf("first=%x, second=%x%n", first, second)
        );
        return (TTestBuilder) this;
    }

    /**
     * Enables printing of the injecting process during test execution for debugging purposes.
     *
     * @return this test builder instance for method chaining
     */
    public TTestBuilder printInjectingProcess() {
        runner.printInjectingProcess();
        return (TTestBuilder) this;
    }

    /**
     * Verifies CPU flags after executing the specified operation.
     *
     * @param flagsCheck the flags checker to verify expected flag states
     * @param operator the operation whose result flags should be verified
     * @return this test builder instance for method chaining
     */
    public TTestBuilder verifyFlags(FlagsCheck<TOperand, ?> flagsCheck, Function<RunnerContext<TOperand>, Integer> operator) {
        lastOperation = operator;
        return verifyFlagsOfLastOp(flagsCheck);
    }

    /**
     * Verifies CPU flags after executing the last operation that was set.
     *
     * @param flagsCheck the flags checker to verify expected flag states
     * @return this test builder instance for method chaining
     * @throws IllegalStateException if no last operation has been set
     */
    public TTestBuilder verifyFlagsOfLastOp(FlagsCheck<TOperand, ?> flagsCheck) {
        if (lastOperation == null) {
            throw new IllegalStateException("Last operation is not set!");
        }
        Function<RunnerContext<TOperand>, Integer> operation = lastOperation;
        runner.verifyAfterTest(new FlagsVerifier<>(cpuVerifier, operation, flagsCheck));
        return (TTestBuilder) this;
    }

    /**
     * Verifies that a byte at the specified memory address matches the result of the given operation.
     *
     * @param address the memory address to verify
     * @param operator the operation whose result should match the memory byte
     * @return this test builder instance for method chaining
     */
    public TTestBuilder verifyByte(int address, Function<RunnerContext<TOperand>, Integer> operator) {
        lastOperation = operator;
        return verifyByte(address);
    }

    /**
     * Verifies that a word at the memory address computed by addressOperator matches the result of the given operation.
     *
     * @param addressOperator the function to compute the memory address
     * @param operator the operation whose result should match the memory word
     * @return this test builder instance for method chaining
     */
    public TTestBuilder verifyWord(Function<RunnerContext<TOperand>, Integer> addressOperator,
                                   Function<RunnerContext<TOperand>, Integer> operator) {
        lastOperation = operator;
        runner.verifyAfterTest(new MemoryWordVerifier<TOperand>(cpuVerifier, operator, addressOperator));
        return (TTestBuilder) this;
    }

    /**
     * Verifies that a byte at the specified memory address matches the result of the last operation.
     *
     * @param address the memory address to verify
     * @return this test builder instance for method chaining
     */
    public TTestBuilder verifyByte(int address) {
        return verifyByte(context -> address);
    }

    /**
     * Verifies that a byte at the memory address computed by addressOperator matches the result of the given operation.
     *
     * @param addressOperator the function to compute the memory address
     * @param operator the operation whose result should match the memory byte
     * @return this test builder instance for method chaining
     */
    public TTestBuilder verifyByte(Function<RunnerContext<TOperand>, Integer> addressOperator,
                                   Function<RunnerContext<TOperand>, Integer> operator) {
        lastOperation = operator;
        return verifyByte(addressOperator);
    }

    /**
     * Verifies that a byte at the memory address computed by addressOperator matches the result of the last operation.
     *
     * @param addressOperator the function to compute the memory address
     * @return this test builder instance for method chaining
     * @throws IllegalStateException if no last operation has been set
     */
    public TTestBuilder verifyByte(Function<RunnerContext<TOperand>, Integer> addressOperator) {
        if (lastOperation == null) {
            throw new IllegalStateException("Last operation is not set!");
        }
        runner.verifyAfterTest(new MemoryByteVerifier<>(cpuVerifier, lastOperation, addressOperator));
        return (TTestBuilder) this;
    }

    /**
     * Keeps the current injectors active after the test run completes.
     * Normally injectors are cleared after each test.
     *
     * @return this test builder instance for method chaining
     */
    public TTestBuilder keepCurrentInjectorsAfterRun() {
        runner.keepCurrentInjectorsAfterClear();
        return (TTestBuilder) this;
    }

    /**
     * Keeps the current verifiers active and clears other verifiers after the test run completes.
     * Normally verifiers are cleared after each test.
     *
     * @return this test builder instance for method chaining
     */
    public TTestBuilder clearOtherVerifiersAfterRun() {
        runner.keepCurrentVerifiersAfterClear();
        return (TTestBuilder) this;
    }

    /**
     * Sets the first operand to the byte value stored at the specified memory address.
     *
     * @param address the memory address to read the byte from
     * @return this test builder instance for method chaining
     */
    public TTestBuilder firstIsMemoryByteAt(int address) {
        runner.injectFirst(new MemoryByte<>(address));
        return (TTestBuilder) this;
    }

    /**
     * Sets the second operand to the byte value stored at the specified memory address.
     *
     * @param address the memory address to read the byte from
     * @return this test builder instance for method chaining
     */
    public TTestBuilder secondIsMemoryByteAt(int address) {
        runner.injectSecond(new MemoryByte<>(address));
        return (TTestBuilder) this;
    }

    /**
     * Sets the first operand to the word (16-bit) value stored at the specified memory address.
     *
     * @param address the memory address to read the word from
     * @return this test builder instance for method chaining
     */
    public TTestBuilder firstIsMemoryWordAt(int address) {
        runner.injectFirst(new MemoryWord<>(address));
        return (TTestBuilder) this;
    }

    /**
     * Sets the second operand to the word (16-bit) value stored at the specified memory address.
     *
     * @param address the memory address to read the word from
     * @return this test builder instance for method chaining
     */
    @SuppressWarnings("unused")
    public TTestBuilder secondIsMemoryWordAt(int address) {
        runner.injectSecond(new MemoryWord<>(address));
        return (TTestBuilder) this;
    }

    /**
     * Sets the first operand to a memory address specified as a byte value.
     *
     * @param value the memory address as a byte value
     * @return this test builder instance for method chaining
     */
    public TTestBuilder firstIsMemoryAddressByte(int value) {
        runner.injectFirst(new MemoryAddress<>((byte) value));
        return (TTestBuilder) this;
    }

    /**
     * Sets the second operand to a memory address specified as a byte value.
     *
     * @param value the memory address as a byte value
     * @return this test builder instance for method chaining
     */
    public TTestBuilder secondIsMemoryAddressByte(int value) {
        runner.injectSecond(new MemoryAddress<>((byte) value));
        return (TTestBuilder) this;
    }

    /**
     * Sets the first operand to a memory address specified as a word (16-bit) value.
     *
     * @param value the memory address as a word value
     * @return this test builder instance for method chaining
     */
    public TTestBuilder firstIsMemoryAddressWord(int value) {
        runner.injectFirst(new MemoryAddress<>(value));
        return (TTestBuilder) this;
    }

    /**
     * Sets the second operand to a memory address specified as a word (16-bit) value.
     *
     * @param value the memory address as a word value
     * @return this test builder instance for method chaining
     */
    public TTestBuilder secondIsMemoryAddressWord(int value) {
        runner.injectSecond(new MemoryAddress<>(value));
        return (TTestBuilder) this;
    }

    /**
     * Writes the second operand as a word value to the memory address specified by the first operand.
     *
     * @return this test builder instance for method chaining
     */
    public TTestBuilder firstIsAddressAndSecondIsMemoryWord() {
        runner.injectTwoOperands((runner, first, second) ->
            writeWordToAddress(runner, first.intValue(), second.intValue()));
        return (TTestBuilder) this;
    }

    /**
     * Writes the first operand as a word value to the memory address specified by the second operand.
     *
     * @return this test builder instance for method chaining
     */
    public TTestBuilder secondIsAddressAndFirstIsMemoryWord() {
        runner.injectTwoOperands((runner, first, second) ->
            writeWordToAddress(runner, second.intValue(), first.intValue()));
        return (TTestBuilder) this;
    }

    /**
     * Writes the second operand as a byte value to the memory address specified by the first operand.
     *
     * @return this test builder instance for method chaining
     */
    public TTestBuilder firstIsAddressAndSecondIsMemoryByte() {
        runner.injectTwoOperands((runner, first, second) ->
            writeByteToAddress(runner, first.intValue(), second.intValue()));
        return (TTestBuilder) this;
    }

    /**
     * Writes the first operand as a byte value to the memory address specified by the second operand.
     *
     * @return this test builder instance for method chaining
     */
    public TTestBuilder secondIsAddressAndFirstIsMemoryByte() {
        runner.injectTwoOperands((runner, first, second) ->
            writeByteToAddress(runner, second.intValue(), first.intValue()));
        return (TTestBuilder) this;
    }

    private void writeWordToAddress(TCpuRunner runner, int address, int value) {
        runner.ensureProgramSize(address + 4);
        runner.setByte(address, value & 0xFF);
        runner.setByte(address + 1, (value >>> 8) & 0xFF);
    }

    private void writeByteToAddress(TCpuRunner runner, int address, int value) {
        runner.ensureProgramSize(address + 4);
        runner.setByte(address, value & 0xFF);
    }

    /**
     * Sets the CPU flags to the specified value before test execution.
     *
     * @param flags the flags value to set
     * @return this test builder instance for method chaining
     */
    public TTestBuilder setFlags(int flags) {
        runner.injectFirst((tmpRunner, argument) -> tmpRunner.setFlags(flags));
        return (TTestBuilder) this;
    }

    /**
     * Expands the program memory to ensure it can accommodate the address computed by the given function.
     *
     * @param address the function to compute the required memory address
     * @return this test builder instance for method chaining
     */
    public TTestBuilder expandMemory(Function<Number, Integer> address) {
        runner.injectFirst((tmpRunner, argument) -> tmpRunner.ensureProgramSize(address.apply(argument)));
        return (TTestBuilder) this;
    }

    /**
     * Executes an instruction with no operands.
     *
     * @param instruction the instruction bytes to execute
     * @return the test runner for this execution
     */
    public TestRunner<TCpuRunner, TOperand> run(int... instruction) {
        return prepareTest().injectNoOperand(new NoOperInstr<>(instruction));
    }

    /**
     * Executes an instruction with the first operand.
     *
     * @param instruction the instruction bytes to execute
     * @return the test runner for this execution
     */
    public TestRunner<TCpuRunner, TOperand> runWithFirstOperand(int... instruction) {
        return prepareTest().injectFirst(new OneOperInstr<>(instruction));
    }

    /**
     * Executes an instruction with the second operand.
     *
     * @param instruction the instruction bytes to execute
     * @return the test runner for this execution
     */
    public TestRunner<TCpuRunner, TOperand> runWithSecondOperand(int... instruction) {
        return prepareTest().injectSecond(new OneOperInstr<>(instruction));
    }

    /**
     * Executes an instruction with the first 8-bit operand, placing an additional opcode after the operand.
     *
     * @param opcodeAfterOperand the opcode byte to place after the operand
     * @param instruction the instruction bytes to execute
     * @return the test runner for this execution
     */
    public TestRunner<TCpuRunner, TOperand> runWithFirst8bitOperandWithOpcodeAfter(int opcodeAfterOperand, int... instruction) {
        return prepareTest().injectFirst((tmpRunner, first) ->
                new OneOperInstr<TCpuRunner, Byte>(instruction)
                        .placeOpcodesAfterOperand(opcodeAfterOperand)
                        .accept(cpuRunner, first.byteValue())
        );
    }

    /**
     * Executes an instruction with the first operand as an 8-bit value.
     *
     * @param instruction the instruction bytes to execute
     * @return the test runner for this execution
     */
    public TestRunner<TCpuRunner, TOperand> runWithFirst8bitOperand(int... instruction) {
        return prepareTest().injectFirst((tmpRunner, first) ->
                new OneOperInstr<TCpuRunner, Byte>(instruction).accept(tmpRunner, first.byteValue())
        );
    }

    /**
     * Executes an instruction with the first 8-bit operand used twice (for both operand positions).
     *
     * @param instruction the instruction bytes to execute
     * @return the test runner for this execution
     */
    public TestRunner<TCpuRunner, TOperand> runWithFirst8bitOperandTwoTimes(int... instruction) {
        return prepareTest().injectFirst((tmpRunner, first) ->
                new TwoOperInstr<TCpuRunner, Byte>(instruction)
                        .inject(tmpRunner, first.byteValue(), first.byteValue())
        );
    }

    /**
     * Executes an instruction with both operands, placing an additional opcode after the operands.
     *
     * @param opcodeAfter the opcode byte to place after the operands
     * @param instruction the instruction bytes to execute
     * @return the test runner for this execution
     */
    public TestRunner<TCpuRunner, TOperand> runWithBothOperandsWithOpcodeAfter(int opcodeAfter, int... instruction) {
        return prepareTest().injectTwoOperands(
                new TwoOperInstr<TCpuRunner, TOperand>(instruction).placeOpcodesAfterOperands(opcodeAfter)
        );
    }

    private TestRunner<TCpuRunner, TOperand> prepareTest() {
        TestRunner<TCpuRunner, TOperand> tmpRunner = runner.clone();

        runner.clearInjectors();
        runner.clearVerifiers();
        return tmpRunner;
    }
}
