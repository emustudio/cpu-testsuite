# CPU Test Suite
![cpu-testsuite](https://github.com/emustudio/cpu-testsuite/workflows/cpu-testsuite/badge.svg?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.emustudio/cpu-testsuite_11.4/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.emustudio/cpu-testsuite_11.4)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

General unit-testing framework intended for testing emuStudio CPU plug-ins. More specifically, it allows to test
correctness of the implementation of CPU instructions. Tests are specified in a declarative way; specific test cases
are generated based on the declarative specification.

The idea of generating test cases was inspired by ["QuickCheck"](https://github.com/pholser/junit-quickcheck) project.

### Features

- Generates test cases for values (8-bit, 16-bit, unary, binary) 
- Tests are specified using Builder pattern
- Automatic environment setup (e.g. fill memory with a program, set up initial CPU flags, or set up registers)

## Usage

For Maven, use:

```
    <dependency>
      <groupId>net.emustudio</groupId>
      <artifactId>cpu-testsuite_11.4</artifactId>
      <version>1.1.0</version>
      <scope>test</scope>
    </dependency>
```

For Gradle, use:

```
    testImplementation 'net.emustudio:cpu-testsuite_11.4:1.1.0'
```

NOTE: Artifact name ends with major.minor emuLib version (currently `_11.4`).

## When to use CPU Test Suite

The suite can be used for testing CPU plugins for emuStudio only if the following requirements are fulfiled:

- operating memory, used by the CPU, is a collection of linearly ordered cells
- operating memory cell type is `Short` or `Byte`
- CPU is using little endian
- CPU has a program counter register (or "instruction pointer") or similar
- CPU has a stack pointer register
- Instruction operands are either `Byte` (8-bit) or `Integer` (16-bit)

# Getting started

In order to help you get started, this section shows and describes some code snippets. 

CPU instructions operate either with a CPU state or with some data (registers or memory). When we say
we are "testing an instruction", we mean by this a verification of the correctness of instruction evaluation.
The testing process is as follows:

1. Setup the initial CPU state and environment (register values, memory cell values, flags) - usually generate it
   according to some rules
2. Run the instruction
3. Check the output - CPU state, register(s), flags, or memory.
4. Repeat for another test case

Each CPU is different. Therefore, the test suite needs specific implementations of several interfaces (or abstract classes):

- custom implementation of `CpuRunner` abstract class (the virtual computer)
- custom implementation of `CpuVerifier` abstract class

Those should be instantiated before each test (tests will make them "dirty"). Then, within the JUnit test, the next
step is to create a "test builder" instance (provided by this framework), which uses the `CpuRunner` and `CpuVerifier`
objects. 

Optionally, to be able to verify CPU flags, the plugin might want to implement `FlagsCheck` abstract class, which will
compute flags based on the result and the `RunnerContext` object, which represents the previous environment state
(first operand, second operand, flags, PC, SP, and possibly other registers).

Let's use 8080 CPU for now, and let's test the `SUB` instruction. The test might look as follows:

```java
import static net.emustudio.cpu.testsuite.Generator.*;
import net.emustudio.cpu.testsuite.memory.ShortMemoryStub;
import net.emustudio.intel8080.impl.suite.CpuRunnerImpl;
import net.emustudio.intel8080.impl.suite.CpuVerifierImpl;
import org.junit.After;
import org.junit.Before;


public class CpuTest {
    private CpuRunnerImpl cpuRunnerImpl;
    private CpuVerifierImpl cpuVerifierImpl;
    private CpuImpl cpu;

    @Before
    public void setUp() throws PluginInitializationException {
        ShortMemoryStub memoryStub = new ShortMemoryStub(NumberUtils.Strategy.LITTLE_ENDIAN);

        cpu = new CpuImpl(...);
        // simulate emuStudio boot
        cpu.initialize(...);

        cpuRunnerImpl = new CpuRunnerImpl(cpu, memoryStub);
        cpuVerifierImpl = new CpuVerifierImpl(cpu, memoryStub);

        Generator.setRandomTestsCount(10); // How many test cases should be generated
    }
    
    @After
    public void tearDown() {
        cpu.destroy();
    }
    
    @Test
    public void testSUB() {
        // ByteTestBuilder specifies that instruction operands are bytes 
        ByteTestBuilder test = new ByteTestBuilder(cpuRunnerImpl, cpuVerifierImpl)
                .firstIsRegister(REG_A)
                .verifyRegister(REG_A, context -> (context.first & 0xFF) - (context.second & 0xFF))
                .verifyFlagsOfLastOp(new FlagsBuilderImpl().sign().zero().carry().auxCarry().parity())
                .keepCurrentInjectorsAfterRun();
        
        forSome8bitBinaryWhichEqual(
                test.run(0x97)
        );
        forSome8bitBinary(
                test.secondIsRegister(REG_B).run(0x90),
                test.secondIsRegister(REG_C).run(0x91),
                test.secondIsRegister(REG_D).run(0x92),
                test.secondIsRegister(REG_E).run(0x93),
                test.secondIsRegister(REG_H).run(0x94),
                test.secondIsRegister(REG_L).run(0x95),
                test.setPair(REG_PAIR_HL, 1).secondIsMemoryByteAt(1).run(0x96)
        );
    }
}
```

It might seem complex, but all makes sense. At first, we need to know, if we operate with bytes or integers (words).
Therefore, we create new `ByteTestBuilder`. There exists also `IntegerTestBuilder` class for operating with 16-bit values.

Instruction `SUB` takes 1 argument - the register, e.g. `SUB B`, which substracts register `B` from register `A`.
In other words:

```
SUB B = A - B
```

Generally, instruction `SUB` will always be evaluated as `A - register`. Therefore we know, that first operand is always
register `A`:

```java
   .firstIsRegister(REG_A)
```

NOTE: Constant `REG_A` is defined in our 8080 CPU.

That's it for preparing the environment. Now, we want to verify, that after performing the "subtract" operation,
we get result in register `A` with the correct value:

```java
    .verifyRegister(REG_A, context -> (context.first & 0xFF) - (context.second & 0xFF))
```

We supply the computation based on the two values, which will be *generated* later. The values are accessible from
`context` object, as member values `context.first` and `context.second`. What you see above is a lambda (feature from
Java 8), taking the testing `context` object, and performing the subtract operation with given values.
 
NOTE: Here, you must be very careful; if you write the computation wrongly, the test will expect wrong results.
    
Also, the instruction is affecting flags in CPU. It is enough to specify that with the following statement:
 
```java
    .verifyFlagsOfLastOp(new FlagsBuilderImpl().sign().zero().carry().auxCarry().parity())
```
 
Here, we are saying: verify flags of the last operation (taken from the previous line - the subtract), and we supply
the flags using `FlagsBuilderImpl` class - sign, zero, carry, auxiliary carry and parity. The class however must be
implemented manually, in order to preserve the generality of the Test Suite. Each CPU has different flags with
different semantics. But don't worry, it is not difficult.

And we're almost done with the test specification. Now, we must say that after we create a test, we want to keep
the environment we set up before (in our case setting that the first operand will be stored in register `A` - before
the operation). We do this with line:
 
```java
    .keepCurrentInjectorsAfterRun();
```

And now, we can 'generate' tests for various random-generated combinations of operands. This is the strongest feature
of the suite, and frees us from creating manual examples of the instruction input and output data. It saves a lot of
time. We just say:

```java
Generator.forSome8bitBinaryWhichEqual(
        test.run(0x97)
);
```

And the generator will generate some 8-bit pair of values, which equal. And we run the test for all the generated values
on a `SUB A` instruction (which has opcode `0x97`). Here, is the trick. In this statement, we test instruction `SUB A`,
which means:

```
SUB A = A - A
```

So in order to have valid test, and we have binary values from generator (we need to have both `context.first` and
`context.second`), we need to have them *equal*, because they represent the same value - in register `A`.

The final part of the test is much more obvious:

```java
Generator.forSome8bitBinary(
        test.secondIsRegister(REG_B).run(0x90),
        test.secondIsRegister(REG_C).run(0x91),
        test.secondIsRegister(REG_D).run(0x92),
        test.secondIsRegister(REG_E).run(0x93),
        test.secondIsRegister(REG_H).run(0x94),
        test.secondIsRegister(REG_L).run(0x95),
        test.setPair(REG_PAIR_HL, 1).secondIsMemoryByteAt(1).run(0x96)
);
```

Here we want to run 7 tests, for each `SUB` variation - for registers `B`, `C`, `D`, etc. So for the specific test we
must say, that the second generated operand will be stored in the given register, before we actually 'run' the test.
Since we did not specify `keepCurrentInjectorsAfterRun()` after this step, the next step will not remember the previous
setting for the second operand. Only the first operand, for register `A` will be remembered for all tests.

The last line is interesting, with preparing register pair `HL=1` and second operand to the memory at address `1`, we
can safely run `SUB M` with opcode `0x96`, which actually does the following:

```
SUB M = A - [HL]
```

For more information, see Javadoc of the project, and real usage in available emuStudio CPU plug-ins.
