// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package org.objectweb.asm.commons;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.objectweb.asm.commons.GeneratorAdapter.EQ;
import static org.objectweb.asm.commons.GeneratorAdapter.GE;
import static org.objectweb.asm.commons.GeneratorAdapter.GT;
import static org.objectweb.asm.commons.GeneratorAdapter.LE;
import static org.objectweb.asm.commons.GeneratorAdapter.LT;
import static org.objectweb.asm.commons.GeneratorAdapter.NE;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * GeneratorAdapter tests.
 *
 * @author Eric Bruneton
 */
public class GeneratorAdapterTest {

  @Test
  public void testConstructor1() {
    GeneratorAdapter generatorAdapter =
        new GeneratorAdapter(new MethodNode(), Opcodes.ACC_PUBLIC, "name", "()V");
    assertEquals(Opcodes.ACC_PUBLIC, generatorAdapter.getAccess());
    assertEquals("name", generatorAdapter.getName());
    assertEquals(Type.VOID_TYPE, generatorAdapter.getReturnType());
    assertArrayEquals(new Type[0], generatorAdapter.getArgumentTypes());
  }

  @Test
  public void testConstructor2() {
    assertThrows(
        IllegalStateException.class,
        () -> new GeneratorAdapter(new MethodNode(), Opcodes.ACC_PUBLIC, "name", "()V") {});
  }

  @Test
  public void testConstructor3() {
    GeneratorAdapter generatorAdapter =
        new GeneratorAdapter(new MethodNode(), Opcodes.ACC_PRIVATE, "m", "(I)F");
    assertEquals(Opcodes.ACC_PRIVATE, generatorAdapter.getAccess());
    assertEquals("m", generatorAdapter.getName());
    assertEquals(Type.FLOAT_TYPE, generatorAdapter.getReturnType());
    assertArrayEquals(new Type[] {Type.INT_TYPE}, generatorAdapter.getArgumentTypes());
  }

  @Test
  public void testConstructor4() {
    new GeneratorAdapter(Opcodes.ACC_PUBLIC, new Method("name", "()V"), new MethodNode());
    ClassNode classNode = new ClassNode();
    GeneratorAdapter generatorAdapter =
        new GeneratorAdapter(
            Opcodes.ACC_PUBLIC,
            new Method("name", "()V"),
            "()V",
            new Type[] {Type.getObjectType("java/lang/Exception")},
            classNode);
    assertEquals(Opcodes.ACC_PUBLIC, generatorAdapter.getAccess());
    assertEquals("name", generatorAdapter.getName());
    assertEquals(Type.VOID_TYPE, generatorAdapter.getReturnType());
    assertArrayEquals(new Type[0], generatorAdapter.getArgumentTypes());

    MethodNode methodNode = classNode.methods.get(0);
    assertEquals(Opcodes.ACC_PUBLIC, methodNode.access);
    assertEquals("name", methodNode.name);
    assertEquals("()V", methodNode.desc);
    assertEquals(Arrays.asList("java/lang/Exception"), methodNode.exceptions);
  }

  @Test
  public void testConstructor4WithoutExceptions() {
    new GeneratorAdapter(Opcodes.ACC_PUBLIC, new Method("name", "()V"), new MethodNode());
    ClassNode classNode = new ClassNode();
    GeneratorAdapter generatorAdapter =
        new GeneratorAdapter(Opcodes.ACC_PUBLIC, new Method("name", "()V"), "()V", null, classNode);
    assertEquals(Opcodes.ACC_PUBLIC, generatorAdapter.getAccess());
    assertEquals("name", generatorAdapter.getName());
    assertEquals(Type.VOID_TYPE, generatorAdapter.getReturnType());
    assertArrayEquals(new Type[0], generatorAdapter.getArgumentTypes());

    MethodNode methodNode = classNode.methods.get(0);
    assertEquals(Opcodes.ACC_PUBLIC, methodNode.access);
    assertEquals("name", methodNode.name);
    assertEquals("()V", methodNode.desc);
    assertEquals(Arrays.asList(), methodNode.exceptions);
  }

  @Test
  public void testPushBoolean() {
    assertEquals("ICONST_0", new Generator().push(false));
    assertEquals("ICONST_1", new Generator().push(true));
  }

  @Test
  public void testPushInt() {
    assertEquals("LDC -32769", new Generator().push(-32769));
    assertEquals("SIPUSH -32768", new Generator().push(-32768));
    assertEquals("BIPUSH -128", new Generator().push(-128));
    assertEquals("ICONST_M1", new Generator().push(-1));
    assertEquals("ICONST_0", new Generator().push(0));
    assertEquals("ICONST_1", new Generator().push(1));
    assertEquals("ICONST_2", new Generator().push(2));
    assertEquals("ICONST_3", new Generator().push(3));
    assertEquals("ICONST_4", new Generator().push(4));
    assertEquals("ICONST_5", new Generator().push(5));
    assertEquals("BIPUSH 6", new Generator().push(6));
    assertEquals("BIPUSH 127", new Generator().push(127));
    assertEquals("SIPUSH 128", new Generator().push(128));
    assertEquals("SIPUSH 32767", new Generator().push(32767));
    assertEquals("LDC 32768", new Generator().push(32768));
  }

  @Test
  public void testPushLong() {
    assertEquals("LCONST_0", new Generator().push(0L));
    assertEquals("LCONST_1", new Generator().push(1L));
    assertEquals("LDC 2", new Generator().push(2L));
  }

  @Test
  public void testPushFloat() {
    assertEquals("FCONST_0", new Generator().push(0.0f));
    assertEquals("FCONST_1", new Generator().push(1.0f));
    assertEquals("FCONST_2", new Generator().push(2.0f));
    assertEquals("LDC 3.0", new Generator().push(3.0f));
  }

  @Test
  public void testPushDouble() {
    assertEquals("DCONST_0", new Generator().push(0.0));
    assertEquals("DCONST_1", new Generator().push(1.0));
    assertEquals("LDC 2.0", new Generator().push(2.0));
  }

  @Test
  public void testPushString() {
    assertEquals("ACONST_NULL", new Generator().push((String) null));
    assertEquals("LDC \"string\"", new Generator().push("string"));
  }

  @Test
  public void testPushType() {
    assertEquals("ACONST_NULL", new Generator().push((Type) null));
    assertEquals(
        "GETSTATIC java/lang/Boolean.TYPE : Ljava/lang/Class;",
        new Generator().push(Type.BOOLEAN_TYPE));
    assertEquals(
        "GETSTATIC java/lang/Character.TYPE : Ljava/lang/Class;",
        new Generator().push(Type.CHAR_TYPE));
    assertEquals(
        "GETSTATIC java/lang/Byte.TYPE : Ljava/lang/Class;", new Generator().push(Type.BYTE_TYPE));
    assertEquals(
        "GETSTATIC java/lang/Short.TYPE : Ljava/lang/Class;",
        new Generator().push(Type.SHORT_TYPE));
    assertEquals(
        "GETSTATIC java/lang/Integer.TYPE : Ljava/lang/Class;",
        new Generator().push(Type.INT_TYPE));
    assertEquals(
        "GETSTATIC java/lang/Float.TYPE : Ljava/lang/Class;",
        new Generator().push(Type.FLOAT_TYPE));
    assertEquals(
        "GETSTATIC java/lang/Long.TYPE : Ljava/lang/Class;", new Generator().push(Type.LONG_TYPE));
    assertEquals(
        "GETSTATIC java/lang/Double.TYPE : Ljava/lang/Class;",
        new Generator().push(Type.DOUBLE_TYPE));
    assertEquals(
        "LDC Ljava/lang/Object;.class",
        new Generator().push(Type.getObjectType("java/lang/Object")));
    assertEquals("LDC [I.class", new Generator().push(Type.getObjectType("[I")));
  }

  @Test
  public void testPushHandle() {
    assertEquals("ACONST_NULL", new Generator().push((Handle) null));
    assertEquals(
        "LDC pkg/Owner.nameI (2)",
        new Generator().push(new Handle(Opcodes.H_GETSTATIC, "pkg/Owner", "name", "I", false)));
  }

  @Test
  public void testLoadThis() {
    assertEquals("ALOAD 0", new Generator().loadThis());
    assertThrows(
        IllegalStateException.class,
        () -> new Generator(Opcodes.ACC_STATIC, "m", "()V").loadThis());
  }

  @Test
  public void testLoadArg() {
    assertEquals("ILOAD 1", new Generator(Opcodes.ACC_PUBLIC, "m", "(I)V").loadArg(0));
    assertEquals("LLOAD 0", new Generator(Opcodes.ACC_STATIC, "m", "(J)V").loadArg(0));
    assertEquals("FLOAD 2", new Generator(Opcodes.ACC_STATIC, "m", "(JF)V").loadArg(1));
  }

  @Test
  public void testLoadArgs() {
    assertEquals("LLOAD 2", new Generator(Opcodes.ACC_PUBLIC, "m", "(IJFD)V").loadArgs(1, 1));
    assertEquals(
        "ILOAD 0 LLOAD 1 FLOAD 3 DLOAD 4",
        new Generator(Opcodes.ACC_STATIC, "m", "(IJFD)V").loadArgs());
  }

  @Test
  public void testLoadArgArray() {
    assertEquals(
        "BIPUSH 9 ANEWARRAY java/lang/Object "
            + "DUP ICONST_0 ILOAD 1 NEW java/lang/Boolean DUP_X1 SWAP"
            + " INVOKESPECIAL java/lang/Boolean.<init> (Z)V AASTORE "
            + "DUP ICONST_1 ILOAD 2 NEW java/lang/Byte DUP_X1 SWAP"
            + " INVOKESPECIAL java/lang/Byte.<init> (B)V AASTORE "
            + "DUP ICONST_2 ILOAD 3 NEW java/lang/Character DUP_X1 SWAP"
            + " INVOKESPECIAL java/lang/Character.<init> (C)V AASTORE "
            + "DUP ICONST_3 ILOAD 4 NEW java/lang/Short DUP_X1 SWAP"
            + " INVOKESPECIAL java/lang/Short.<init> (S)V AASTORE "
            + "DUP ICONST_4 ILOAD 5 NEW java/lang/Integer DUP_X1 SWAP"
            + " INVOKESPECIAL java/lang/Integer.<init> (I)V AASTORE "
            + "DUP ICONST_5 LLOAD 6 NEW java/lang/Long DUP_X2 DUP_X2 POP"
            + " INVOKESPECIAL java/lang/Long.<init> (J)V AASTORE "
            + "DUP BIPUSH 6 FLOAD 8 NEW java/lang/Float DUP_X1 SWAP"
            + " INVOKESPECIAL java/lang/Float.<init> (F)V AASTORE "
            + "DUP BIPUSH 7 DLOAD 9 NEW java/lang/Double DUP_X2 DUP_X2 POP"
            + " INVOKESPECIAL java/lang/Double.<init> (D)V AASTORE "
            + "DUP BIPUSH 8 ALOAD 11 AASTORE",
        new Generator(Opcodes.ACC_PUBLIC, "m", "(ZBCSIJFDLjava/lang/Object;)V").loadArgArray());
  }

  @Test
  public void testStoreArg() {
    assertEquals("ISTORE 1", new Generator(Opcodes.ACC_PUBLIC, "m", "(I)V").storeArg(0));
    assertEquals("LSTORE 0", new Generator(Opcodes.ACC_STATIC, "m", "(J)V").storeArg(0));
    assertEquals("FSTORE 2", new Generator(Opcodes.ACC_STATIC, "m", "(JF)V").storeArg(1));
  }

  @Test
  public void testLoadLocal() {
    Generator generator = new Generator();
    int local = generator.newLocal(Type.FLOAT_TYPE);
    assertEquals(Type.FLOAT_TYPE, generator.getLocalType(local));
    assertEquals("FLOAD 1", generator.loadLocal(local));
    assertEquals("ILOAD 1", generator.loadLocal(local, Type.INT_TYPE));
    assertEquals(Type.INT_TYPE, generator.getLocalType(local));
    assertEquals("ILOAD 1", generator.loadLocal(local));
  }

  @Test
  public void testStoreLocal() {
    Generator generator = new Generator();
    int local = generator.newLocal(Type.FLOAT_TYPE);
    assertEquals(Type.FLOAT_TYPE, generator.getLocalType(local));
    assertEquals("FSTORE 1", generator.storeLocal(local));
    assertEquals("ISTORE 1", generator.storeLocal(local, Type.INT_TYPE));
    assertEquals(Type.INT_TYPE, generator.getLocalType(local));
    assertEquals("ISTORE 1", generator.storeLocal(local));
  }

  @Test
  public void testArrayLoad() {
    assertEquals("IALOAD", new Generator().arrayLoad(Type.INT_TYPE));
    assertEquals("LALOAD", new Generator().arrayLoad(Type.LONG_TYPE));
  }

  @Test
  public void testArrayStore() {
    assertEquals("IASTORE", new Generator().arrayStore(Type.INT_TYPE));
    assertEquals("LASTORE", new Generator().arrayStore(Type.LONG_TYPE));
  }

  @Test
  public void testPop() {
    assertEquals("POP", new Generator().pop());
  }

  @Test
  public void testPop2() {
    assertEquals("POP2", new Generator().pop2());
  }

  @Test
  public void testDup() {
    assertEquals("DUP", new Generator().dup());
  }

  @Test
  public void testDup2() {
    assertEquals("DUP2", new Generator().dup2());
  }

  @Test
  public void testDupX1() {
    assertEquals("DUP_X1", new Generator().dupX1());
  }

  @Test
  public void testDupX2() {
    assertEquals("DUP_X2", new Generator().dupX2());
  }

  @Test
  public void testDup2X1() {
    assertEquals("DUP2_X1", new Generator().dup2X1());
  }

  @Test
  public void testDup2X2() {
    assertEquals("DUP2_X2", new Generator().dup2X2());
  }

  @Test
  public void testSwap() {
    assertEquals("SWAP", new Generator().swap());
    assertEquals("SWAP", new Generator().swap(Type.INT_TYPE, Type.INT_TYPE));
    assertEquals("DUP_X2 POP", new Generator().swap(Type.LONG_TYPE, Type.INT_TYPE));
    assertEquals("DUP2_X1 POP2", new Generator().swap(Type.INT_TYPE, Type.LONG_TYPE));
    assertEquals("DUP2_X2 POP2", new Generator().swap(Type.LONG_TYPE, Type.LONG_TYPE));
  }

  @Test
  public void testMath() {
    assertEquals("IADD", new Generator().math(GeneratorAdapter.ADD, Type.INT_TYPE));
    assertEquals("FSUB", new Generator().math(GeneratorAdapter.SUB, Type.FLOAT_TYPE));
    assertEquals("LMUL", new Generator().math(GeneratorAdapter.MUL, Type.LONG_TYPE));
    assertEquals("DDIV", new Generator().math(GeneratorAdapter.DIV, Type.DOUBLE_TYPE));
    assertEquals("IREM", new Generator().math(GeneratorAdapter.REM, Type.INT_TYPE));
    assertEquals("LNEG", new Generator().math(GeneratorAdapter.NEG, Type.LONG_TYPE));
    assertEquals("ISHL", new Generator().math(GeneratorAdapter.SHL, Type.INT_TYPE));
    assertEquals("LSHR", new Generator().math(GeneratorAdapter.SHR, Type.LONG_TYPE));
    assertEquals("IUSHR", new Generator().math(GeneratorAdapter.USHR, Type.INT_TYPE));
    assertEquals("LAND", new Generator().math(GeneratorAdapter.AND, Type.LONG_TYPE));
    assertEquals("IOR", new Generator().math(GeneratorAdapter.OR, Type.INT_TYPE));
    assertEquals("LXOR", new Generator().math(GeneratorAdapter.XOR, Type.LONG_TYPE));
  }

  @Test
  public void testNot() {
    assertEquals("ICONST_1 IXOR", new Generator().not());
  }

  @Test
  public void testIinc() {
    assertEquals("IINC 3 5", new Generator().iinc(3, 5));
  }

  @Test
  public void testCast() {
    assertEquals("", new Generator().cast(Type.DOUBLE_TYPE, Type.DOUBLE_TYPE));
    assertEquals("D2F", new Generator().cast(Type.DOUBLE_TYPE, Type.FLOAT_TYPE));
    assertEquals("D2L", new Generator().cast(Type.DOUBLE_TYPE, Type.LONG_TYPE));
    assertEquals("D2I", new Generator().cast(Type.DOUBLE_TYPE, Type.INT_TYPE));
    assertEquals("D2I I2B", new Generator().cast(Type.DOUBLE_TYPE, Type.BYTE_TYPE));

    assertEquals("F2D", new Generator().cast(Type.FLOAT_TYPE, Type.DOUBLE_TYPE));
    assertEquals("", new Generator().cast(Type.FLOAT_TYPE, Type.FLOAT_TYPE));
    assertEquals("F2L", new Generator().cast(Type.FLOAT_TYPE, Type.LONG_TYPE));
    assertEquals("F2I", new Generator().cast(Type.FLOAT_TYPE, Type.INT_TYPE));
    assertEquals("F2I I2B", new Generator().cast(Type.FLOAT_TYPE, Type.BYTE_TYPE));

    assertEquals("L2D", new Generator().cast(Type.LONG_TYPE, Type.DOUBLE_TYPE));
    assertEquals("L2F", new Generator().cast(Type.LONG_TYPE, Type.FLOAT_TYPE));
    assertEquals("", new Generator().cast(Type.LONG_TYPE, Type.LONG_TYPE));
    assertEquals("L2I", new Generator().cast(Type.LONG_TYPE, Type.INT_TYPE));
    assertEquals("L2I I2B", new Generator().cast(Type.LONG_TYPE, Type.BYTE_TYPE));

    assertEquals("I2D", new Generator().cast(Type.INT_TYPE, Type.DOUBLE_TYPE));
    assertEquals("I2F", new Generator().cast(Type.INT_TYPE, Type.FLOAT_TYPE));
    assertEquals("I2L", new Generator().cast(Type.INT_TYPE, Type.LONG_TYPE));
    assertEquals("", new Generator().cast(Type.INT_TYPE, Type.INT_TYPE));
    assertEquals("I2B", new Generator().cast(Type.INT_TYPE, Type.BYTE_TYPE));
    assertEquals("I2C", new Generator().cast(Type.INT_TYPE, Type.CHAR_TYPE));
    assertEquals("I2S", new Generator().cast(Type.INT_TYPE, Type.SHORT_TYPE));

    assertThrows(
        IllegalArgumentException.class, () -> new Generator().cast(Type.INT_TYPE, Type.VOID_TYPE));
  }

  @Test
  public void testBox() {
    assertEquals("", new Generator().box(Type.getObjectType("java/lang/Object")));
    assertEquals("", new Generator().box(Type.getObjectType("[I")));
    assertEquals("ACONST_NULL", new Generator().box(Type.VOID_TYPE));

    assertEquals(
        "NEW java/lang/Boolean DUP_X1 SWAP INVOKESPECIAL java/lang/Boolean.<init> (Z)V",
        new Generator().box(Type.BOOLEAN_TYPE));
    assertEquals(
        "NEW java/lang/Byte DUP_X1 SWAP INVOKESPECIAL java/lang/Byte.<init> (B)V",
        new Generator().box(Type.BYTE_TYPE));
    assertEquals(
        "NEW java/lang/Character DUP_X1 SWAP INVOKESPECIAL java/lang/Character.<init> (C)V",
        new Generator().box(Type.CHAR_TYPE));
    assertEquals(
        "NEW java/lang/Short DUP_X1 SWAP INVOKESPECIAL java/lang/Short.<init> (S)V",
        new Generator().box(Type.SHORT_TYPE));
    assertEquals(
        "NEW java/lang/Integer DUP_X1 SWAP INVOKESPECIAL java/lang/Integer.<init> (I)V",
        new Generator().box(Type.INT_TYPE));
    assertEquals(
        "NEW java/lang/Long DUP_X2 DUP_X2 POP INVOKESPECIAL java/lang/Long.<init> (J)V",
        new Generator().box(Type.LONG_TYPE));
    assertEquals(
        "NEW java/lang/Float DUP_X1 SWAP INVOKESPECIAL java/lang/Float.<init> (F)V",
        new Generator().box(Type.FLOAT_TYPE));
    assertEquals(
        "NEW java/lang/Double DUP_X2 DUP_X2 POP INVOKESPECIAL java/lang/Double.<init> (D)V",
        new Generator().box(Type.DOUBLE_TYPE));
  }

  @Test
  public void testValueOf() {
    assertEquals("", new Generator().valueOf(Type.getObjectType("java/lang/Object")));
    assertEquals("", new Generator().valueOf(Type.getType("[I")));
    assertEquals("ACONST_NULL", new Generator().valueOf(Type.VOID_TYPE));

    assertEquals(
        "INVOKESTATIC java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;",
        new Generator().valueOf(Type.BOOLEAN_TYPE));
    assertEquals(
        "INVOKESTATIC java/lang/Byte.valueOf (B)Ljava/lang/Byte;",
        new Generator().valueOf(Type.BYTE_TYPE));
    assertEquals(
        "INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;",
        new Generator().valueOf(Type.CHAR_TYPE));
    assertEquals(
        "INVOKESTATIC java/lang/Short.valueOf (S)Ljava/lang/Short;",
        new Generator().valueOf(Type.SHORT_TYPE));
    assertEquals(
        "INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;",
        new Generator().valueOf(Type.INT_TYPE));
    assertEquals(
        "INVOKESTATIC java/lang/Long.valueOf (J)Ljava/lang/Long;",
        new Generator().valueOf(Type.LONG_TYPE));
    assertEquals(
        "INVOKESTATIC java/lang/Float.valueOf (F)Ljava/lang/Float;",
        new Generator().valueOf(Type.FLOAT_TYPE));
    assertEquals(
        "INVOKESTATIC java/lang/Double.valueOf (D)Ljava/lang/Double;",
        new Generator().valueOf(Type.DOUBLE_TYPE));
  }

  @Test
  public void testUnbox() {
    assertEquals("", new Generator().unbox(Type.VOID_TYPE));
    assertEquals(
        "CHECKCAST java/lang/Boolean INVOKEVIRTUAL java/lang/Boolean.booleanValue ()Z",
        new Generator().unbox(Type.BOOLEAN_TYPE));
    assertEquals(
        "CHECKCAST java/lang/Number INVOKEVIRTUAL java/lang/Number.intValue ()I",
        new Generator().unbox(Type.BYTE_TYPE));
    assertEquals(
        "CHECKCAST java/lang/Character INVOKEVIRTUAL java/lang/Character.charValue ()C",
        new Generator().unbox(Type.CHAR_TYPE));
    assertEquals(
        "CHECKCAST java/lang/Number INVOKEVIRTUAL java/lang/Number.intValue ()I",
        new Generator().unbox(Type.SHORT_TYPE));
    assertEquals(
        "CHECKCAST java/lang/Number INVOKEVIRTUAL java/lang/Number.intValue ()I",
        new Generator().unbox(Type.INT_TYPE));
    assertEquals(
        "CHECKCAST java/lang/Number INVOKEVIRTUAL java/lang/Number.longValue ()J",
        new Generator().unbox(Type.LONG_TYPE));
    assertEquals(
        "CHECKCAST java/lang/Number INVOKEVIRTUAL java/lang/Number.floatValue ()F",
        new Generator().unbox(Type.FLOAT_TYPE));
    assertEquals(
        "CHECKCAST java/lang/Number INVOKEVIRTUAL java/lang/Number.doubleValue ()D",
        new Generator().unbox(Type.DOUBLE_TYPE));
    assertEquals("", new Generator().unbox(Type.getObjectType("java/lang/Object")));
    assertEquals(
        "CHECKCAST java/lang/Number",
        new Generator().unbox(Type.getObjectType("java/lang/Number")));
    assertEquals("CHECKCAST [I", new Generator().unbox(Type.getType("[I")));
  }

  @Test
  public void testIfCmp() {
    Generator generator = new Generator();
    Label label = generator.newLabel();
    assertEquals("IF_ICMPEQ L0", generator.ifCmp(Type.INT_TYPE, EQ, label));
    assertEquals("IF_ICMPNE L0", generator.ifCmp(Type.INT_TYPE, NE, label));
    assertEquals("IF_ICMPGE L0", generator.ifCmp(Type.INT_TYPE, GE, label));
    assertEquals("IF_ICMPGT L0", generator.ifCmp(Type.INT_TYPE, GT, label));
    assertEquals("IF_ICMPLE L0", generator.ifCmp(Type.INT_TYPE, LE, label));
    assertEquals("IF_ICMPLT L0", generator.ifCmp(Type.INT_TYPE, LT, label));
    assertEquals("LCMP IFGE L0", generator.ifCmp(Type.LONG_TYPE, GE, label));
    assertEquals("FCMPL IFGE L0", generator.ifCmp(Type.FLOAT_TYPE, GE, label));
    assertEquals("FCMPL IFGT L0", generator.ifCmp(Type.FLOAT_TYPE, GT, label));
    assertEquals("FCMPG IFLE L0", generator.ifCmp(Type.FLOAT_TYPE, LE, label));
    assertEquals("FCMPG IFLT L0", generator.ifCmp(Type.FLOAT_TYPE, LT, label));
    assertEquals("DCMPL IFGE L0", generator.ifCmp(Type.DOUBLE_TYPE, GE, label));
    assertEquals("DCMPL IFGT L0", generator.ifCmp(Type.DOUBLE_TYPE, GT, label));
    assertEquals("DCMPG IFLE L0", generator.ifCmp(Type.DOUBLE_TYPE, LE, label));
    assertEquals("DCMPG IFLT L0", generator.ifCmp(Type.DOUBLE_TYPE, LT, label));

    assertEquals(
        "IF_ACMPEQ L0", generator.ifCmp(Type.getObjectType("java/lang/Object"), EQ, label));
    assertEquals(
        "IF_ACMPNE L0", generator.ifCmp(Type.getObjectType("java/lang/Object"), NE, label));
    assertThrows(
        IllegalArgumentException.class,
        () -> generator.ifCmp(Type.getObjectType("java/lang/Object"), GE, label));

    assertEquals("IF_ACMPEQ L0", generator.ifCmp(Type.getType("[I"), EQ, label));
    assertEquals("IF_ACMPNE L0", generator.ifCmp(Type.getType("[I"), NE, label));
    assertThrows(
        IllegalArgumentException.class, () -> generator.ifCmp(Type.getType("[I"), GE, label));

    assertThrows(IllegalArgumentException.class, () -> generator.ifCmp(Type.INT_TYPE, 0, label));
  }

  @Test
  public void testMark() {
    assertEquals("L0", new Generator().mark(new Label()));
  }

  @Test
  public void testIfICmp() {
    Generator generator = new Generator();
    Label label = generator.newLabel();
    assertEquals("IF_ICMPEQ L0", generator.ifICmp(EQ, label));
    assertEquals("IF_ICMPNE L0", generator.ifICmp(NE, label));
    assertEquals("IF_ICMPGE L0", generator.ifICmp(GE, label));
    assertEquals("IF_ICMPGT L0", generator.ifICmp(GT, label));
    assertEquals("IF_ICMPLE L0", generator.ifICmp(LE, label));
    assertEquals("IF_ICMPLT L0", generator.ifICmp(LT, label));
    assertThrows(IllegalArgumentException.class, () -> generator.ifICmp(0, label));
  }

  @Test
  public void testIfZCmp() {
    Generator generator = new Generator();
    Label label = generator.newLabel();
    assertEquals("IFEQ L0", generator.ifZCmp(EQ, label));
    assertEquals("IFNE L0", generator.ifZCmp(NE, label));
    assertEquals("IFGE L0", generator.ifZCmp(GE, label));
    assertEquals("IFGT L0", generator.ifZCmp(GT, label));
    assertEquals("IFLE L0", generator.ifZCmp(LE, label));
    assertEquals("IFLT L0", generator.ifZCmp(LT, label));
  }

  @Test
  public void testIfNull() {
    assertEquals("IFNULL L0", new Generator().ifNull(new Label()));
  }

  @Test
  public void testIfNonNull() {
    assertEquals("IFNONNULL L0", new Generator().ifNonNull(new Label()));
  }

  @Test
  public void testGoto() {
    assertEquals("GOTO L0", new Generator().goTo(new Label()));
  }

  @Test
  public void testTableSwitch() {
    assertEquals("L0 ICONST_M1 L1", new Generator().tableSwitch(new int[0]));
    assertEquals(
        "TABLESWITCH\n"
            + "      0: L0\n"
            + "      1: L1\n"
            + "      default: L2 L0 ICONST_0 L1 ICONST_1 L2 ICONST_M1 L3",
        new Generator().tableSwitch(new int[] {0, 1}));
    assertEquals(
        "LOOKUPSWITCH\n"
            + "      0: L0\n"
            + "      1: L1\n"
            + "      default: L2 L0 ICONST_0 L1 ICONST_1 L2 ICONST_M1 L3",
        new Generator().tableSwitch(new int[] {0, 1}, false));
    assertEquals(
        "LOOKUPSWITCH\n"
            + "      0: L0\n"
            + "      4: L1\n"
            + "      default: L2 L0 ICONST_0 L1 ICONST_4 L2 ICONST_M1 L3",
        new Generator().tableSwitch(new int[] {0, 4}));
    assertEquals(
        "TABLESWITCH\n"
            + "      0: L0\n"
            + "      1: L1\n"
            + "      2: L1\n"
            + "      3: L1\n"
            + "      4: L2\n"
            + "      default: L1 L0 ICONST_0 L2 ICONST_4 L1 ICONST_M1 L3",
        new Generator().tableSwitch(new int[] {0, 4}, true));
    assertThrows(
        IllegalArgumentException.class, () -> new Generator().tableSwitch(new int[] {1, 0}));
  }

  @Test
  public void testRet() {
    assertEquals("RET 5", new Generator().ret(5));
  }

  @Test
  public void testReturnValue() {
    assertEquals("RETURN", new Generator(Opcodes.ACC_PUBLIC, "m", "()V").returnValue());
    assertEquals("IRETURN", new Generator(Opcodes.ACC_PUBLIC, "m", "()Z").returnValue());
    assertEquals("IRETURN", new Generator(Opcodes.ACC_PUBLIC, "m", "()B").returnValue());
    assertEquals("IRETURN", new Generator(Opcodes.ACC_PUBLIC, "m", "()C").returnValue());
    assertEquals("IRETURN", new Generator(Opcodes.ACC_PUBLIC, "m", "()S").returnValue());
    assertEquals("IRETURN", new Generator(Opcodes.ACC_PUBLIC, "m", "()I").returnValue());
    assertEquals("LRETURN", new Generator(Opcodes.ACC_PUBLIC, "m", "()J").returnValue());
    assertEquals("FRETURN", new Generator(Opcodes.ACC_PUBLIC, "m", "()F").returnValue());
    assertEquals("DRETURN", new Generator(Opcodes.ACC_PUBLIC, "m", "()D").returnValue());
    assertEquals("ARETURN", new Generator(Opcodes.ACC_PUBLIC, "m", "()[I").returnValue());
    assertEquals("ARETURN", new Generator(Opcodes.ACC_PUBLIC, "m", "()Lpkg/Class").returnValue());
  }

  @Test
  public void testGetStatic() {
    assertEquals(
        "GETSTATIC pkg/Class.f : I",
        new Generator().getStatic(Type.getObjectType("pkg/Class"), "f", Type.INT_TYPE));
  }

  @Test
  public void testPutStatic() {
    assertEquals(
        "PUTSTATIC pkg/Class.f : I",
        new Generator().putStatic(Type.getObjectType("pkg/Class"), "f", Type.INT_TYPE));
  }

  @Test
  public void testGetField() {
    assertEquals(
        "GETFIELD pkg/Class.f : I",
        new Generator().getField(Type.getObjectType("pkg/Class"), "f", Type.INT_TYPE));
  }

  @Test
  public void testPutField() {
    assertEquals(
        "PUTFIELD pkg/Class.f : I",
        new Generator().putField(Type.getObjectType("pkg/Class"), "f", Type.INT_TYPE));
  }

  @Test
  public void testInvokeVirtual() {
    assertEquals(
        "INVOKEVIRTUAL pkg/Class.m (I)J",
        new Generator().invokeVirtual(Type.getObjectType("pkg/Class"), new Method("m", "(I)J")));
  }

  @Test
  public void testInvokeConstructor() {
    assertEquals(
        "INVOKESPECIAL pkg/Class.<init> (I)J",
        new Generator()
            .invokeConstructor(Type.getObjectType("pkg/Class"), new Method("<init>", "(I)J")));
  }

  @Test
  public void testInvokeStatic() {
    assertEquals(
        "INVOKESTATIC pkg/Class.m (I)J",
        new Generator().invokeStatic(Type.getObjectType("pkg/Class"), new Method("m", "(I)J")));
  }

  @Test
  public void testInvokeInterface() {
    assertEquals(
        "INVOKEINTERFACE pkg/Class.m (I)J (itf)",
        new Generator().invokeInterface(Type.getObjectType("pkg/Class"), new Method("m", "(I)J")));
  }

  @Test
  public void testInvokeDynamic() {
    assertEquals(
        "INVOKEDYNAMIC m(I)J [\n"
            + "      // handle kind 0x2 : GETSTATIC\n"
            + "      pkg/Owner.name(I)\n"
            + "      // arguments:\n"
            + "      1, \n"
            + "      2, \n"
            + "      3\n"
            + "    ]",
        new Generator()
            .invokeDynamic(
                "m",
                "(I)J",
                new Handle(Opcodes.H_GETSTATIC, "pkg/Owner", "name", "I", false),
                1,
                2,
                3));
  }

  @Test
  public void testNewInstance() {
    assertEquals("NEW pkg/Class", new Generator().newInstance(Type.getObjectType("pkg/Class")));
  }

  @Test
  public void testNewArray() {
    assertEquals("NEWARRAY T_BOOLEAN", new Generator().newArray(Type.BOOLEAN_TYPE));
    assertEquals("NEWARRAY T_BYTE", new Generator().newArray(Type.BYTE_TYPE));
    assertEquals("NEWARRAY T_CHAR", new Generator().newArray(Type.CHAR_TYPE));
    assertEquals("NEWARRAY T_SHORT", new Generator().newArray(Type.SHORT_TYPE));
    assertEquals("NEWARRAY T_INT", new Generator().newArray(Type.INT_TYPE));
    assertEquals("NEWARRAY T_FLOAT", new Generator().newArray(Type.FLOAT_TYPE));
    assertEquals("NEWARRAY T_LONG", new Generator().newArray(Type.LONG_TYPE));
    assertEquals("NEWARRAY T_DOUBLE", new Generator().newArray(Type.DOUBLE_TYPE));
    assertEquals("ANEWARRAY pkg/Class", new Generator().newArray(Type.getObjectType("pkg/Class")));
    assertEquals("ANEWARRAY [I", new Generator().newArray(Type.getType("[I")));
  }

  @Test
  public void testArrayLength() {
    assertEquals("ARRAYLENGTH", new Generator().arrayLength());
  }

  @Test
  public void testThrowException() {
    assertEquals("ATHROW", new Generator().throwException());
    assertEquals(
        "NEW pkg/Exception DUP LDC \"msg\" "
            + "INVOKESPECIAL pkg/Exception.<init> (Ljava/lang/String;)V ATHROW",
        new Generator().throwException(Type.getObjectType("pkg/Exception"), "msg"));
  }

  @Test
  public void testCheckcast() {
    assertEquals("", new Generator().checkCast(Type.getObjectType("java/lang/Object")));
    assertEquals("CHECKCAST pkg/Class", new Generator().checkCast(Type.getObjectType("pkg/Class")));
  }

  @Test
  public void testInstanceOf() {
    assertEquals("INSTANCEOF pkg/Class", new Generator().instanceOf(Type.getType("Lpkg/Class;")));
  }

  @Test
  public void testMonitorEnter() {
    assertEquals("MONITORENTER", new Generator().monitorEnter());
  }

  @Test
  public void testMonitorExit() {
    assertEquals("MONITOREXIT", new Generator().monitorExit());
  }

  @Test
  public void testEndMethod() {
    assertEquals("MAXSTACK = 0 MAXLOCALS = 0", new Generator().endMethod());
    assertEquals("", new Generator(Opcodes.ACC_ABSTRACT, "m", "()V").endMethod());
  }

  @Test
  public void testCatchException() {
    assertEquals(
        "TRYCATCHBLOCK L0 L1 L2 null L2",
        new Generator().catchException(new Label(), new Label(), null));
    assertEquals(
        "TRYCATCHBLOCK L0 L1 L2 pkg/Exception L2",
        new Generator()
            .catchException(new Label(), new Label(), Type.getObjectType("pkg/Exception")));
  }

  private static class Generator implements TableSwitchGenerator {

    private final Textifier textifier;
    private final GeneratorAdapter generatorAdapter;

    public Generator() {
      this(Opcodes.ACC_PUBLIC, "m", "()V");
    }

    public Generator(final int access, final String name, final String descriptor) {
      textifier = new Textifier();
      generatorAdapter =
          new GeneratorAdapter(
              Opcodes.ASM6, new TraceMethodVisitor(textifier), access, name, descriptor);
    }

    public String push(final boolean value) {
      generatorAdapter.push(value);
      return toString();
    }

    public String push(final int value) {
      generatorAdapter.push(value);
      return toString();
    }

    public String push(final long value) {
      generatorAdapter.push(value);
      return toString();
    }

    public String push(final float value) {
      generatorAdapter.push(value);
      return toString();
    }

    public String push(final double value) {
      generatorAdapter.push(value);
      return toString();
    }

    public String push(final String value) {
      generatorAdapter.push(value);
      return toString();
    }

    public String push(final Type value) {
      generatorAdapter.push(value);
      return toString();
    }

    public String push(final Handle handle) {
      generatorAdapter.push(handle);
      return toString();
    }

    public String loadThis() {
      generatorAdapter.loadThis();
      return toString();
    }

    public String loadArg(final int arg) {
      generatorAdapter.loadArg(arg);
      return toString();
    }

    public String loadArgs(final int arg, final int count) {
      generatorAdapter.loadArgs(arg, count);
      return toString();
    }

    public String loadArgs() {
      generatorAdapter.loadArgs();
      return toString();
    }

    public String loadArgArray() {
      generatorAdapter.loadArgArray();
      return toString();
    }

    public String storeArg(final int arg) {
      generatorAdapter.storeArg(arg);
      return toString();
    }

    public int newLocal(final Type type) {
      return generatorAdapter.newLocal(type);
    }

    public Type getLocalType(final int local) {
      return generatorAdapter.getLocalType(local);
    }

    public String loadLocal(final int local) {
      generatorAdapter.loadLocal(local);
      return toString();
    }

    public String loadLocal(final int local, final Type type) {
      generatorAdapter.loadLocal(local, type);
      return toString();
    }

    public String storeLocal(final int local) {
      generatorAdapter.storeLocal(local);
      return toString();
    }

    public String storeLocal(final int local, final Type type) {
      generatorAdapter.storeLocal(local, type);
      return toString();
    }

    public String arrayLoad(final Type type) {
      generatorAdapter.arrayLoad(type);
      return toString();
    }

    public String arrayStore(final Type type) {
      generatorAdapter.arrayStore(type);
      return toString();
    }

    public String pop() {
      generatorAdapter.pop();
      return toString();
    }

    public String pop2() {
      generatorAdapter.pop2();
      return toString();
    }

    public String dup() {
      generatorAdapter.dup();
      return toString();
    }

    public String dup2() {
      generatorAdapter.dup2();
      return toString();
    }

    public String dupX1() {
      generatorAdapter.dupX1();
      return toString();
    }

    public String dupX2() {
      generatorAdapter.dupX2();
      return toString();
    }

    public String dup2X1() {
      generatorAdapter.dup2X1();
      return toString();
    }

    public String dup2X2() {
      generatorAdapter.dup2X2();
      return toString();
    }

    public String swap() {
      generatorAdapter.swap();
      return toString();
    }

    public String swap(final Type prev, final Type type) {
      generatorAdapter.swap(prev, type);
      return toString();
    }

    public String math(final int op, final Type type) {
      generatorAdapter.math(op, type);
      return toString();
    }

    public String not() {
      generatorAdapter.not();
      return toString();
    }

    public String iinc(final int local, final int amount) {
      generatorAdapter.iinc(local, amount);
      return toString();
    }

    public String cast(final Type from, final Type to) {
      generatorAdapter.cast(from, to);
      return toString();
    }

    public String box(final Type type) {
      generatorAdapter.box(type);
      return toString();
    }

    public String valueOf(final Type type) {
      generatorAdapter.valueOf(type);
      return toString();
    }

    public String unbox(final Type type) {
      generatorAdapter.unbox(type);
      return toString();
    }

    public Label newLabel() {
      return generatorAdapter.newLabel();
    }

    public String mark(final Label label) {
      generatorAdapter.mark(label);
      return toString();
    }

    public String ifCmp(final Type type, final int mode, final Label label) {
      generatorAdapter.ifCmp(type, mode, label);
      return toString();
    }

    public String ifICmp(final int mode, final Label label) {
      generatorAdapter.ifICmp(mode, label);
      return toString();
    }

    public String ifZCmp(final int mode, final Label label) {
      generatorAdapter.ifZCmp(mode, label);
      return toString();
    }

    public String ifNull(final Label label) {
      generatorAdapter.ifNull(label);
      return toString();
    }

    public String ifNonNull(final Label label) {
      generatorAdapter.ifNonNull(label);
      return toString();
    }

    public String goTo(final Label label) {
      generatorAdapter.goTo(label);
      return toString();
    }

    public String ret(final int local) {
      generatorAdapter.ret(local);
      return toString();
    }

    public String tableSwitch(final int[] keys) {
      generatorAdapter.tableSwitch(keys, this);
      return toString();
    }

    public String tableSwitch(final int[] keys, final boolean useTable) {
      generatorAdapter.tableSwitch(keys, this, useTable);
      return toString();
    }

    @Override
    public void generateCase(int key, Label end) {
      generatorAdapter.push(key);
    }

    @Override
    public void generateDefault() {
      generatorAdapter.push(-1);
    }

    public String returnValue() {
      generatorAdapter.returnValue();
      return toString();
    }

    public String getStatic(final Type owner, final String name, final Type type) {
      generatorAdapter.getStatic(owner, name, type);
      return toString();
    }

    public String putStatic(final Type owner, final String name, final Type type) {
      generatorAdapter.putStatic(owner, name, type);
      return toString();
    }

    public String getField(final Type owner, final String name, final Type type) {
      generatorAdapter.getField(owner, name, type);
      return toString();
    }

    public String putField(final Type owner, final String name, final Type type) {
      generatorAdapter.putField(owner, name, type);
      return toString();
    }

    public String invokeVirtual(final Type owner, final Method method) {
      generatorAdapter.invokeVirtual(owner, method);
      return toString();
    }

    public String invokeConstructor(final Type type, final Method method) {
      generatorAdapter.invokeConstructor(type, method);
      return toString();
    }

    public String invokeStatic(final Type owner, final Method method) {
      generatorAdapter.invokeStatic(owner, method);
      return toString();
    }

    public String invokeInterface(final Type owner, final Method method) {
      generatorAdapter.invokeInterface(owner, method);
      return toString();
    }

    public String invokeDynamic(
        final String name, final String desc, final Handle bsm, final Object... bsmArgs) {
      generatorAdapter.invokeDynamic(name, desc, bsm, bsmArgs);
      return toString();
    }

    public String newInstance(final Type type) {
      generatorAdapter.newInstance(type);
      return toString();
    }

    public String newArray(final Type type) {
      generatorAdapter.newArray(type);
      return toString();
    }

    public String arrayLength() {
      generatorAdapter.arrayLength();
      return toString();
    }

    public String throwException() {
      generatorAdapter.throwException();
      return toString();
    }

    public String throwException(final Type type, final String msg) {
      generatorAdapter.throwException(type, msg);
      return toString();
    }

    public String checkCast(final Type type) {
      generatorAdapter.checkCast(type);
      return toString();
    }

    public String instanceOf(final Type type) {
      generatorAdapter.instanceOf(type);
      return toString();
    }

    public String monitorEnter() {
      generatorAdapter.monitorEnter();
      return toString();
    }

    public String monitorExit() {
      generatorAdapter.monitorExit();
      return toString();
    }

    public String endMethod() {
      generatorAdapter.endMethod();
      return toString();
    }

    public String catchException(final Label start, final Label end, final Type exception) {
      generatorAdapter.catchException(start, end, exception);
      return toString();
    }

    @Override
    public String toString() {
      String result =
          textifier
              .text
              .stream()
              .map(text -> text.toString().trim())
              .collect(Collectors.joining(" "));
      textifier.text.clear();
      return result;
    }
  }
}
