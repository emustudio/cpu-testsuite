// SPDX-License-Identifier: GPL-3.0-or-later
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

import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings({"unused", "unchecked"})
public abstract class TestBuilder<TOperand extends Number, TTestBuilder extends TestBuilder,
        TCpuRunner extends CpuRunner<?>, TCpuVerifier extends CpuVerifier> {
    protected final TCpuRunner cpuRunner;
    protected final TCpuVerifier cpuVerifier;
    protected final TestRunner<TCpuRunner, TOperand> runner;

    protected Function<RunnerContext<TOperand>, Integer> lastOperation;

    protected TestBuilder(TCpuRunner cpuRunner, TCpuVerifier cpuVerifier) {
        this.cpuRunner = Objects.requireNonNull(cpuRunner);
        this.cpuVerifier = Objects.requireNonNull(cpuVerifier);
        this.runner = new TestRunner<>(cpuRunner);
    }

    public TTestBuilder clearAllVerifiers() {
        runner.clearAllVerifiers();
        return (TTestBuilder)this;
    }

    public TTestBuilder verifyAll(Consumer<RunnerContext<TOperand>>... verifiers) {
        runner.verifyAfterTest(verifiers);
        return (TTestBuilder)this;
    }

    public TTestBuilder registerIsRandom(int register, int maxValue) {
        Random random = new Random();
        runner.injectFirst((tmpRunner, argument) -> cpuRunner.setRegister(register, random.nextInt(maxValue + 1)));
        return (TTestBuilder)this;
    }

    public TTestBuilder printRegister(int register) {
        runner.injectTwoOperands((runner, first, second) ->
                        System.out.println(String.format("REG_%d=%x", register, runner.getRegisters().get(register)))
        );
        return (TTestBuilder)this;
    }

    public TTestBuilder printOperands() {
        runner.injectTwoOperands((runner, first, second) ->
                        System.out.println(String.format("first=%x, second=%x", first, second))
        );
        return (TTestBuilder)this;
    }

    public TTestBuilder printInjectingProcess() {
        runner.printInjectingProcess();
        return (TTestBuilder)this;
    }

    public TTestBuilder verifyFlags(FlagsCheck<TOperand, ?> flagsCheck, Function<RunnerContext<TOperand>, Integer> operator) {
        lastOperation = operator;
        return verifyFlagsOfLastOp(flagsCheck);
    }

    public TTestBuilder verifyFlagsOfLastOp(FlagsCheck<TOperand, ?> flagsCheck) {
        if (lastOperation == null) {
            throw new IllegalStateException("Last operation is not set!");
        }
        Function<RunnerContext<TOperand>, Integer> operation = lastOperation;
        runner.verifyAfterTest(new FlagsVerifier<>(cpuVerifier, operation, flagsCheck));
        return (TTestBuilder)this;
    }

    public TTestBuilder verifyByte(int address, Function<RunnerContext<TOperand>, Integer> operator) {
        lastOperation = operator;
        return verifyByte(address);
    }

    public TTestBuilder verifyWord(Function<RunnerContext<TOperand>, Integer> addressOperator,
                                   Function<RunnerContext<TOperand>, Integer> operator) {
        lastOperation = operator;
        runner.verifyAfterTest(new MemoryWordVerifier<TOperand>(cpuVerifier, operator, addressOperator));
        return (TTestBuilder)this;
    }

    public TTestBuilder verifyByte(int address) {
        return verifyByte(context -> address);
    }

    public TTestBuilder verifyByte(Function<RunnerContext<TOperand>, Integer> addressOperator,
                                   Function<RunnerContext<TOperand>, Integer> operator) {
        lastOperation = operator;
        return verifyByte(addressOperator);
    }

    public TTestBuilder verifyByte(Function<RunnerContext<TOperand>, Integer> addressOperator) {
        if (lastOperation == null) {
            throw new IllegalStateException("Last operation is not set!");
        }
        runner.verifyAfterTest(new MemoryByteVerifier<>(cpuVerifier, lastOperation, addressOperator));
        return (TTestBuilder)this;
    }

    public TTestBuilder keepCurrentInjectorsAfterRun() {
        runner.keepCurrentInjectorsAfterClear();
        return (TTestBuilder)this;
    }

    public TTestBuilder clearOtherVerifiersAfterRun() {
        runner.keepCurrentVerifiersAfterClear();
        return (TTestBuilder)this;
    }

    public TTestBuilder firstIsMemoryByteAt(int address) {
        runner.injectFirst(new MemoryByte<>(address));
        return (TTestBuilder)this;
    }

    public TTestBuilder secondIsMemoryByteAt(int address) {
        runner.injectSecond(new MemoryByte<>(address));
        return (TTestBuilder)this;
    }

    public TTestBuilder firstIsMemoryWordAt(int address) {
        runner.injectFirst(new MemoryWord<>(address));
        return (TTestBuilder)this;
    }

    @SuppressWarnings("unused")
    public TTestBuilder secondIsMemoryWordAt(int address) {
        runner.injectSecond(new MemoryWord<>(address));
        return (TTestBuilder)this;
    }

    public TTestBuilder firstIsMemoryAddressByte(int value) {
        runner.injectFirst(new MemoryAddress<>((byte)value));
        return (TTestBuilder)this;
    }

    public TTestBuilder secondIsMemoryAddressByte(int value) {
        runner.injectSecond(new MemoryAddress<>((byte) value));
        return (TTestBuilder)this;
    }

    public TTestBuilder firstIsMemoryAddressWord(int value) {
        runner.injectFirst(new MemoryAddress<>(value));
        return (TTestBuilder)this;
    }

    public TTestBuilder secondIsMemoryAddressWord(int value) {
        runner.injectSecond(new MemoryAddress<>(value));
        return (TTestBuilder)this;
    }

    public TTestBuilder firstIsAddressAndSecondIsMemoryWord() {
        runner.injectTwoOperands((runner, first, second) -> {
            runner.ensureProgramSize(first.intValue() + 4);
            runner.setByte(first.intValue(), second.intValue() & 0xFF);
            runner.setByte(first.intValue() + 1, (second.intValue() >>> 8) & 0xFF);
        });
        return (TTestBuilder)this;
    }

    public TTestBuilder secondIsAddressAndFirstIsMemoryWord() {
        runner.injectTwoOperands((runner, first, second) -> {
            runner.ensureProgramSize(second.intValue() + 4);
            runner.setByte(second.intValue(), first.intValue() & 0xFF);
            runner.setByte(second.intValue() + 1, (first.intValue() >>> 8) & 0xFF);
        });
        return (TTestBuilder)this;
    }

    public TTestBuilder firstIsAddressAndSecondIsMemoryByte() {
        runner.injectTwoOperands((runner, first, second) -> {
            runner.ensureProgramSize(first.intValue() + 4);
            runner.setByte(first.intValue(), second.intValue() & 0xFF);
        });
        return (TTestBuilder)this;
    }

    public TTestBuilder secondIsAddressAndFirstIsMemoryByte() {
        runner.injectTwoOperands((runner, first, second) -> {
            runner.ensureProgramSize(second.intValue() + 4);
            runner.setByte(second.intValue(), first.intValue() & 0xFF);
        });
        return (TTestBuilder)this;
    }

    public TTestBuilder setFlags(int flags) {
        runner.injectFirst((tmpRunner, argument) -> tmpRunner.setFlags(flags));
        return (TTestBuilder)this;
    }

    public TTestBuilder expandMemory(Function<Number, Integer> address) {
        runner.injectFirst((tmpRunner, argument) -> tmpRunner.ensureProgramSize(address.apply(argument)));
        return (TTestBuilder)this;
    }

    public TestRunner<TCpuRunner, TOperand> run(int... instruction) {
        return prepareTest().injectNoOperand(new NoOperInstr<>(instruction));
    }

    public TestRunner<TCpuRunner, TOperand> runWithFirstOperand(int... instruction) {
        return prepareTest().injectFirst(new OneOperInstr<>(instruction));
    }

    public TestRunner<TCpuRunner, TOperand> runWithSecondOperand(int... instruction) {
        return prepareTest().injectSecond(new OneOperInstr<>(instruction));
    }

    public TestRunner<TCpuRunner, TOperand> runWithFirst8bitOperandWithOpcodeAfter(int opcodeAfterOperand, int... instruction) {
        return prepareTest().injectFirst((tmpRunner, first) ->
            new OneOperInstr<TCpuRunner, Byte>(instruction)
                .placeOpcodesAfterOperand(opcodeAfterOperand)
                .accept(cpuRunner, first.byteValue())
        );
    }

    public TestRunner<TCpuRunner, TOperand> runWithFirst8bitOperand(int... instruction) {
        return prepareTest().injectFirst((tmpRunner, first) ->
            new OneOperInstr<TCpuRunner, Byte>(instruction).accept(tmpRunner, first.byteValue())
        );
    }

    public TestRunner<TCpuRunner, TOperand> runWithFirst8bitOperandTwoTimes(int... instruction) {
        return prepareTest().injectFirst((tmpRunner, first) ->
            new TwoOperInstr<TCpuRunner, Byte>(instruction)
                .inject(tmpRunner, first.byteValue(), first.byteValue())
        );
    }

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
