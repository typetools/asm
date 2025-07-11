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
package org.objectweb.asm;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Unit tests for {@link Constants}.
 *
 * @author Eric Bruneton
 */
class ConstantsTest {

  private enum ConstantType {
    ASM_VERSION,
    CLASS_VERSION,
    ACCESS_FLAG,
    ASM_ACCESS_FLAG,
    NEW_ARRAY_TYPE,
    REFERENCE_KIND,
    FRAME_TYPE,
    VERIFICATION_TYPE_INFO_TAG,
    OPCODE,
    OTHER
  };

  @Test
  void testAsmVersions() {
    List<Field> asmVersions = getConstants(ConstantType.ASM_VERSION);

    Set<Integer> asmVersionValues =
        asmVersions.stream().map(ConstantsTest::getIntValue).collect(Collectors.toSet());

    assertEquals(asmVersions.size(), asmVersionValues.size());
  }

  @Test
  void testClassVersions() {
    List<Field> classVersions = getConstants(ConstantType.CLASS_VERSION);

    Set<Integer> classVersionValues =
        classVersions.stream().map(ConstantsTest::getIntValue).collect(Collectors.toSet());

    assertEquals(classVersions.size(), classVersionValues.size());
  }

  @Test
  void testAccessFlags() throws IllegalAccessException {
    List<Field> accessFlags = getConstants(ConstantType.ACCESS_FLAG);

    for (Field accessFlag : accessFlags) {
      assertEquals(0, accessFlag.getInt(null) & ~0xFFFF);
      assertEquals(1, Integer.bitCount(accessFlag.getInt(null)));
    }
  }

  @Test
  void testAsmAccessFlags() throws IllegalAccessException {
    List<Field> asmAccessFlags = getConstants(ConstantType.ASM_ACCESS_FLAG);

    for (Field asmAccessFlag : asmAccessFlags) {
      assertEquals(0, asmAccessFlag.getInt(null) & 0xFFFF);
      assertEquals(1, Integer.bitCount(asmAccessFlag.getInt(null)));
    }
  }

  @Test
  void testNewArrayTypes() {
    List<Field> newArrayTypes = getConstants(ConstantType.NEW_ARRAY_TYPE);

    Set<Integer> newArrayTypeValues =
        newArrayTypes.stream().map(ConstantsTest::getIntValue).collect(Collectors.toSet());

    assertEquals(newArrayTypes.size(), newArrayTypeValues.size());
    for (int newArrayType : newArrayTypeValues) {
      assertEquals(0, newArrayType & ~0xFF);
    }
  }

  @Test
  void testReferenceKinds() {
    List<Field> referenceKinds = getConstants(ConstantType.REFERENCE_KIND);

    Set<Integer> referenceKindValues =
        referenceKinds.stream().map(ConstantsTest::getIntValue).collect(Collectors.toSet());

    assertEquals(referenceKinds.size(), referenceKindValues.size());
    for (int referenceKind : referenceKindValues) {
      assertEquals(0, referenceKind & ~0xFF);
    }
  }

  @Test
  void testFrameTypes() {
    List<Field> frameTypes = getConstants(ConstantType.FRAME_TYPE);

    Set<Integer> frameTypeValues =
        frameTypes.stream().map(ConstantsTest::getIntValue).collect(Collectors.toSet());

    assertEquals(frameTypes.size(), frameTypeValues.size());
  }

  @Test
  void testVerificationTypeInfoTags() {
    List<Field> verificationTypeInfoTags = getConstants(ConstantType.VERIFICATION_TYPE_INFO_TAG);

    Set<Integer> verificationTypeInfoTagValues =
        verificationTypeInfoTags.stream()
            .map(ConstantsTest::getIntegerValue)
            .collect(Collectors.toSet());

    assertEquals(verificationTypeInfoTags.size(), verificationTypeInfoTagValues.size());
    for (int verificationTypeInfoTag : verificationTypeInfoTagValues) {
      assertEquals(0, verificationTypeInfoTag & ~0xFF);
    }
  }

  @Test
  void testOpcodes() {
    List<Field> opcodes = getConstants(ConstantType.OPCODE);

    Set<Integer> opcodeValues =
        opcodes.stream().map(ConstantsTest::getIntValue).collect(Collectors.toSet());

    assertEquals(opcodes.size(), opcodeValues.size());
    for (int opcode : opcodeValues) {
      assertEquals(0, opcode & ~0xFF);
      assertEquals(0, opcode & Opcodes.SOURCE_MASK);
    }
  }

  @Test
  void testIsWhitelisted() {
    assertFalse(Constants.isWhitelisted("org/jacoco/core/internal/flow/ClassProbesVisitor"));
    assertFalse(Constants.isWhitelisted("org/objectweb/asm/ClassWriter"));
    assertFalse(Constants.isWhitelisted("org/objectweb/asm/util/CheckClassVisitor"));
    assertFalse(Constants.isWhitelisted("org/objectweb/asm/ClassWriterTest"));
    assertTrue(Constants.isWhitelisted("org/objectweb/asm/ClassWriterTest$DeadCodeInserter"));
    assertTrue(Constants.isWhitelisted("org/objectweb/asm/util/TraceClassVisitor"));
    assertTrue(Constants.isWhitelisted("org/objectweb/asm/util/CheckClassAdapter"));
  }

  @Test
  void testCheckIsPreview_nullStream() {
    Executable checkIsPreview = () -> Constants.checkIsPreview(null);

    assertThrows(IllegalStateException.class, checkIsPreview);
  }

  @Test
  void testCheckIsPreview_invalidStream() {
    InputStream invalidStream = new ByteArrayInputStream(new byte[4]);

    Executable checkIsPreview = () -> Constants.checkIsPreview(invalidStream);

    assertThrows(IllegalStateException.class, checkIsPreview);
  }

  @Test
  void testCheckIsPreview_nonPreviewClass() {
    InputStream nonPreviewStream = new ByteArrayInputStream(new byte[8]);

    Executable checkIsPreview = () -> Constants.checkIsPreview(nonPreviewStream);

    assertThrows(IllegalStateException.class, checkIsPreview);
  }

  @Test
  void testCheckIsPreview_previewClass() {
    byte[] previewClass = new byte[] {0, 0, 0, 0, (byte) 0xFF, (byte) 0xFF};
    InputStream previewStream = new ByteArrayInputStream(previewClass);

    Executable checkIsPreview = () -> Constants.checkIsPreview(previewStream);

    assertDoesNotThrow(checkIsPreview);
  }

  private static List<Field> getConstants(final ConstantType constantType) {
    return Stream.concat(
            Arrays.stream(Opcodes.class.getFields()), Arrays.stream(Constants.class.getFields()))
        .filter(field -> getType(field).equals(constantType))
        .collect(Collectors.toList());
  }

  private static ConstantType getType(final Field field) {
    switch (field.getName()) {
      case "ASM4":
      case "ASM5":
      case "ASM6":
      case "ASM7":
      case "ASM8":
      case "ASM9":
      case "ASM10_EXPERIMENTAL":
        return ConstantType.ASM_VERSION;
      case "V_PREVIEW":
      case "V1_1":
      case "V1_2":
      case "V1_3":
      case "V1_4":
      case "V1_5":
      case "V1_6":
      case "V1_7":
      case "V1_8":
      case "V9":
      case "V10":
      case "V11":
      case "V12":
      case "V13":
      case "V14":
      case "V15":
      case "V16":
      case "V17":
      case "V18":
      case "V19":
      case "V20":
      case "V21":
      case "V22":
      case "V23":
      case "V24":
      case "V25":
      case "V26":
        return ConstantType.CLASS_VERSION;
      case "ACC_PUBLIC":
      case "ACC_PRIVATE":
      case "ACC_PROTECTED":
      case "ACC_STATIC":
      case "ACC_FINAL":
      case "ACC_SUPER":
      case "ACC_SYNCHRONIZED":
      case "ACC_OPEN":
      case "ACC_TRANSITIVE":
      case "ACC_VOLATILE":
      case "ACC_BRIDGE":
      case "ACC_STATIC_PHASE":
      case "ACC_VARARGS":
      case "ACC_TRANSIENT":
      case "ACC_NATIVE":
      case "ACC_INTERFACE":
      case "ACC_ABSTRACT":
      case "ACC_STRICT":
      case "ACC_SYNTHETIC":
      case "ACC_ANNOTATION":
      case "ACC_ENUM":
      case "ACC_MANDATED":
      case "ACC_MODULE":
      case "ACC_SEALED":
        return ConstantType.ACCESS_FLAG;
      case "ACC_RECORD":
      case "ACC_DEPRECATED":
      case "ACC_CONSTRUCTOR":
        return ConstantType.ASM_ACCESS_FLAG;
      case "T_BOOLEAN":
      case "T_CHAR":
      case "T_FLOAT":
      case "T_DOUBLE":
      case "T_BYTE":
      case "T_SHORT":
      case "T_INT":
      case "T_LONG":
        return ConstantType.NEW_ARRAY_TYPE;
      case "H_GETFIELD":
      case "H_GETSTATIC":
      case "H_PUTFIELD":
      case "H_PUTSTATIC":
      case "H_INVOKEVIRTUAL":
      case "H_INVOKESTATIC":
      case "H_INVOKESPECIAL":
      case "H_NEWINVOKESPECIAL":
      case "H_INVOKEINTERFACE":
        return ConstantType.REFERENCE_KIND;
      case "F_NEW":
      case "F_FULL":
      case "F_APPEND":
      case "F_CHOP":
      case "F_SAME":
      case "F_SAME1":
      case "F_INSERT":
        return ConstantType.FRAME_TYPE;
      case "TOP":
      case "INTEGER":
      case "FLOAT":
      case "DOUBLE":
      case "LONG":
      case "NULL":
      case "UNINITIALIZED_THIS":
        return ConstantType.VERIFICATION_TYPE_INFO_TAG;
      case "NOP":
      case "ACONST_NULL":
      case "ICONST_M1":
      case "ICONST_0":
      case "ICONST_1":
      case "ICONST_2":
      case "ICONST_3":
      case "ICONST_4":
      case "ICONST_5":
      case "LCONST_0":
      case "LCONST_1":
      case "FCONST_0":
      case "FCONST_1":
      case "FCONST_2":
      case "DCONST_0":
      case "DCONST_1":
      case "BIPUSH":
      case "SIPUSH":
      case "LDC":
      case "LDC_W":
      case "LDC2_W":
      case "ILOAD":
      case "LLOAD":
      case "FLOAD":
      case "DLOAD":
      case "ALOAD":
      case "ILOAD_0":
      case "ILOAD_1":
      case "ILOAD_2":
      case "ILOAD_3":
      case "LLOAD_0":
      case "LLOAD_1":
      case "LLOAD_2":
      case "LLOAD_3":
      case "FLOAD_0":
      case "FLOAD_1":
      case "FLOAD_2":
      case "FLOAD_3":
      case "DLOAD_0":
      case "DLOAD_1":
      case "DLOAD_2":
      case "DLOAD_3":
      case "ALOAD_0":
      case "ALOAD_1":
      case "ALOAD_2":
      case "ALOAD_3":
      case "IALOAD":
      case "LALOAD":
      case "FALOAD":
      case "DALOAD":
      case "AALOAD":
      case "BALOAD":
      case "CALOAD":
      case "SALOAD":
      case "ISTORE":
      case "LSTORE":
      case "FSTORE":
      case "DSTORE":
      case "ASTORE":
      case "ISTORE_0":
      case "ISTORE_1":
      case "ISTORE_2":
      case "ISTORE_3":
      case "LSTORE_0":
      case "LSTORE_1":
      case "LSTORE_2":
      case "LSTORE_3":
      case "FSTORE_0":
      case "FSTORE_1":
      case "FSTORE_2":
      case "FSTORE_3":
      case "DSTORE_0":
      case "DSTORE_1":
      case "DSTORE_2":
      case "DSTORE_3":
      case "ASTORE_0":
      case "ASTORE_1":
      case "ASTORE_2":
      case "ASTORE_3":
      case "IASTORE":
      case "LASTORE":
      case "FASTORE":
      case "DASTORE":
      case "AASTORE":
      case "BASTORE":
      case "CASTORE":
      case "SASTORE":
      case "POP":
      case "POP2":
      case "DUP":
      case "DUP_X1":
      case "DUP_X2":
      case "DUP2":
      case "DUP2_X1":
      case "DUP2_X2":
      case "SWAP":
      case "IADD":
      case "LADD":
      case "FADD":
      case "DADD":
      case "ISUB":
      case "LSUB":
      case "FSUB":
      case "DSUB":
      case "IMUL":
      case "LMUL":
      case "FMUL":
      case "DMUL":
      case "IDIV":
      case "LDIV":
      case "FDIV":
      case "DDIV":
      case "IREM":
      case "LREM":
      case "FREM":
      case "DREM":
      case "INEG":
      case "LNEG":
      case "FNEG":
      case "DNEG":
      case "ISHL":
      case "LSHL":
      case "ISHR":
      case "LSHR":
      case "IUSHR":
      case "LUSHR":
      case "IAND":
      case "LAND":
      case "IOR":
      case "LOR":
      case "IXOR":
      case "LXOR":
      case "IINC":
      case "I2L":
      case "I2F":
      case "I2D":
      case "L2I":
      case "L2F":
      case "L2D":
      case "F2I":
      case "F2L":
      case "F2D":
      case "D2I":
      case "D2L":
      case "D2F":
      case "I2B":
      case "I2C":
      case "I2S":
      case "LCMP":
      case "FCMPL":
      case "FCMPG":
      case "DCMPL":
      case "DCMPG":
      case "IFEQ":
      case "IFNE":
      case "IFLT":
      case "IFGE":
      case "IFGT":
      case "IFLE":
      case "IF_ICMPEQ":
      case "IF_ICMPNE":
      case "IF_ICMPLT":
      case "IF_ICMPGE":
      case "IF_ICMPGT":
      case "IF_ICMPLE":
      case "IF_ACMPEQ":
      case "IF_ACMPNE":
      case "GOTO":
      case "JSR":
      case "RET":
      case "TABLESWITCH":
      case "LOOKUPSWITCH":
      case "IRETURN":
      case "LRETURN":
      case "FRETURN":
      case "DRETURN":
      case "ARETURN":
      case "RETURN":
      case "GETSTATIC":
      case "PUTSTATIC":
      case "GETFIELD":
      case "PUTFIELD":
      case "INVOKEVIRTUAL":
      case "INVOKESPECIAL":
      case "INVOKESTATIC":
      case "INVOKEINTERFACE":
      case "INVOKEDYNAMIC":
      case "NEW":
      case "NEWARRAY":
      case "ANEWARRAY":
      case "ARRAYLENGTH":
      case "ATHROW":
      case "CHECKCAST":
      case "INSTANCEOF":
      case "MONITORENTER":
      case "MONITOREXIT":
      case "WIDE":
      case "MULTIANEWARRAY":
      case "IFNULL":
      case "IFNONNULL":
      case "GOTO_W":
      case "JSR_W":
      case "ASM_IFEQ":
      case "ASM_IFNE":
      case "ASM_IFLT":
      case "ASM_IFGE":
      case "ASM_IFGT":
      case "ASM_IFLE":
      case "ASM_IF_ICMPEQ":
      case "ASM_IF_ICMPNE":
      case "ASM_IF_ICMPLT":
      case "ASM_IF_ICMPGE":
      case "ASM_IF_ICMPGT":
      case "ASM_IF_ICMPLE":
      case "ASM_IF_ACMPEQ":
      case "ASM_IF_ACMPNE":
      case "ASM_GOTO":
      case "ASM_JSR":
      case "ASM_IFNULL":
      case "ASM_IFNONNULL":
      case "ASM_GOTO_W":
        return ConstantType.OPCODE;
      case "WIDE_JUMP_OPCODE_DELTA":
      case "ASM_OPCODE_DELTA":
      case "ASM_IFNULL_OPCODE_DELTA":
      case "SOURCE_DEPRECATED":
      case "SOURCE_MASK":
        return ConstantType.OTHER;
      default:
        throw new IllegalArgumentException("Unknown constant " + field.getName());
    }
  }

  private static int getIntValue(final Field field) {
    try {
      return field.getInt(null);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static int getIntegerValue(final Field field) {
    try {
      return (int) field.get(null);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
