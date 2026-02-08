package org.objectweb.asm.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.test.AsmTest;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

@SuppressWarnings("UnnecessaryBoxing")
class LambdaRemapTest extends AsmTest implements Opcodes {

  // The method signature of LambdaMetafactory.metafactory(...).
  private static final String LAMBDA_FACTORY_METAFACTORY =
      "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;"
          + "Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;"
          + "Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;";

  // The method signature of LambdaMetafactory.altMetafactory(...).
  private static final String LAMBDA_FACTORY_ALTMETAFACTORY =
      "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;"
          + "[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;";

  /*
   * The 3 following methods correspond to the following Java code:
   *
   *  public class LambdaTest {
   *    public interface TestInterface {
   *      default void helloWorld() {}
   *    }
   *
   *    public static void normalLambda() {
   *      Runnable runnable = Thread::dumpStack;
   *    }
   *
   *    public static void serializableLambda() {
   *      Runnable runnable = (Runnable & Serializable) Thread::dumpStack;
   *    }
   *
   *    public static void advancedLambda() {
   *      Runnable runnable = (Runnable & Serializable & TestInterface) Thread::dumpStack;
   *    }
   *  }
   */

  void func_normalLambda(final MethodVisitor methodVisitor) {
    methodVisitor.visitInvokeDynamicInsn(
        "run",
        "()Ljava/lang/Runnable;",
        new Handle(
            Opcodes.H_INVOKESTATIC,
            "java/lang/invoke/LambdaMetafactory",
            "metafactory",
            LAMBDA_FACTORY_METAFACTORY,
            false),
        Type.getType("()V"),
        new Handle(Opcodes.H_INVOKESTATIC, "java/lang/Thread", "dumpStack", "()V", false),
        Type.getType("()V"));
  }

  void func_serializableLambda(final MethodVisitor methodVisitor) {
    methodVisitor.visitInvokeDynamicInsn(
        "run",
        "()Ljava/lang/Runnable;",
        new Handle(
            Opcodes.H_INVOKESTATIC,
            "java/lang/invoke/LambdaMetafactory",
            "altMetafactory",
            LAMBDA_FACTORY_ALTMETAFACTORY,
            false),
        Type.getType("()V"),
        new Handle(Opcodes.H_INVOKESTATIC, "java/lang/Thread", "dumpStack", "()V", false),
        Type.getType("()V"),
        Integer.valueOf(5),
        Integer.valueOf(0));
  }

  void func_advancedLambda(final MethodVisitor methodVisitor) {
    methodVisitor.visitInvokeDynamicInsn(
        "run",
        "()Ljava/lang/Runnable;",
        new Handle(
            Opcodes.H_INVOKESTATIC,
            "java/lang/invoke/LambdaMetafactory",
            "altMetafactory",
            LAMBDA_FACTORY_ALTMETAFACTORY,
            false),
        Type.getType("()V"),
        new Handle(Opcodes.H_INVOKESTATIC, "java/lang/Thread", "dumpStack", "()V", false),
        Type.getType("()V"),
        Integer.valueOf(7),
        Integer.valueOf(1),
        Type.getType("Lpkg/LambdaTest$TestInterface;"),
        Integer.valueOf(0));
  }

  public static Stream<Remapper> remappersLatest() {
    return Stream.of(
        new Remapper(/* latest */ Opcodes.ASM10_EXPERIMENTAL) {
          @Override
          public String mapMethodName(
              final String owner, final String name, final String descriptor) {
            if ("java/lang/Runnable".equals(owner) && "run".equals(name)) {
              return "call";
            }
            return name;
          }

          @Override
          public String map(final String internalName) {
            if ("java/lang/Runnable".equals(internalName)) {
              return "me/MyRunnable";
            }
            return super.map(internalName);
          }
        },
        new SimpleRemapper(
            /* latest */ Opcodes.ASM10_EXPERIMENTAL,
            Map.of(
                "java/lang/Runnable", "me/MyRunnable",
                "java/lang/Runnable.run()V", "call")));
  }

  @SuppressWarnings("deprecation")
  public static Stream<Remapper> remappersDeprecated() {
    return Stream.of(
        new Remapper() {
          @Override
          public String mapMethodName(
              final String owner, final String name, final String descriptor) {
            if ("java/lang/Runnable".equals(owner) && "run".equals(name)) {
              return "call";
            }
            return name;
          }

          @Override
          public String map(final String internalName) {
            if ("java/lang/Runnable".equals(internalName)) {
              return "me/MyRunnable";
            }
            return super.map(internalName);
          }
        },
        new SimpleRemapper(
            Map.of(
                "java/lang/Runnable", "me/MyRunnable",
                "java/lang/Runnable.run()V", "call")));
  }

  @Nested
  @ParameterizedClass
  @MethodSource("org.objectweb.asm.commons.LambdaRemapTest#remappersLatest")
  class LatestApi {

    @Parameter Remapper remapper;

    @Test
    void normalLambda() {
      ClassNode classNode = new ClassNode();
      ClassRemapper classRemapper =
          new ClassRemapper(/* latest api */ Opcodes.ASM9, classNode, remapper);
      classRemapper.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, "C", null, "java/lang/Object", null);
      MethodVisitor methodVisitor =
          classRemapper.visitMethod(Opcodes.ACC_PUBLIC, "hello", "()V", null, null);
      methodVisitor.visitCode();

      func_normalLambda(methodVisitor);

      InvokeDynamicInsnNode invokeDynamic =
          (InvokeDynamicInsnNode) classNode.methods.get(0).instructions.get(0);
      assertEquals("()Lme/MyRunnable;", invokeDynamic.desc);
      assertEquals("call", invokeDynamic.name);
    }

    @Test
    void serializableLambda() {
      ClassNode classNode = new ClassNode();
      ClassRemapper classRemapper =
          new ClassRemapper(/* latest api */ Opcodes.ASM9, classNode, remapper);
      classRemapper.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, "C", null, "java/lang/Object", null);
      MethodVisitor methodVisitor =
          classRemapper.visitMethod(Opcodes.ACC_PUBLIC, "hello", "()V", null, null);
      methodVisitor.visitCode();

      func_serializableLambda(methodVisitor);

      InvokeDynamicInsnNode invokeDynamic =
          (InvokeDynamicInsnNode) classNode.methods.get(0).instructions.get(0);
      assertEquals("()Lme/MyRunnable;", invokeDynamic.desc);
      assertEquals("call", invokeDynamic.name);
    }

    @Test
    void advancedLambda() {
      ClassNode classNode = new ClassNode();
      ClassRemapper classRemapper =
          new ClassRemapper(/* latest api */ Opcodes.ASM9, classNode, remapper);
      classRemapper.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, "C", null, "java/lang/Object", null);
      MethodVisitor methodVisitor =
          classRemapper.visitMethod(Opcodes.ACC_PUBLIC, "hello", "()V", null, null);
      methodVisitor.visitCode();

      func_advancedLambda(methodVisitor);

      InvokeDynamicInsnNode invokeDynamic =
          (InvokeDynamicInsnNode) classNode.methods.get(0).instructions.get(0);
      assertEquals("()Lme/MyRunnable;", invokeDynamic.desc);
      assertEquals("call", invokeDynamic.name);
    }
  }

  @Nested
  @ParameterizedClass
  @MethodSource("org.objectweb.asm.commons.LambdaRemapTest#remappersDeprecated")
  class DeprecatedApi {
    @Parameter Remapper remapper;

    @Test
    void normalLambda() {
      ClassNode classNode = new ClassNode();
      ClassRemapper classRemapper =
          new ClassRemapper(/* latest api */ Opcodes.ASM9, classNode, remapper);
      classRemapper.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, "C", null, "java/lang/Object", null);
      MethodVisitor methodVisitor =
          classRemapper.visitMethod(Opcodes.ACC_PUBLIC, "hello", "()V", null, null);
      methodVisitor.visitCode();

      func_normalLambda(methodVisitor);

      InvokeDynamicInsnNode invokeDynamic =
          (InvokeDynamicInsnNode) classNode.methods.get(0).instructions.get(0);
      assertEquals("()Lme/MyRunnable;", invokeDynamic.desc);
      assertEquals("run", invokeDynamic.name);
    }

    @Test
    void serializableLambda() {
      ClassNode classNode = new ClassNode();
      ClassRemapper classRemapper =
          new ClassRemapper(/* latest api */ Opcodes.ASM9, classNode, remapper);
      classRemapper.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, "C", null, "java/lang/Object", null);
      MethodVisitor methodVisitor =
          classRemapper.visitMethod(Opcodes.ACC_PUBLIC, "hello", "()V", null, null);
      methodVisitor.visitCode();

      func_serializableLambda(methodVisitor);

      InvokeDynamicInsnNode invokeDynamic =
          (InvokeDynamicInsnNode) classNode.methods.get(0).instructions.get(0);
      assertEquals("()Lme/MyRunnable;", invokeDynamic.desc);
      assertEquals("run", invokeDynamic.name);
    }

    @Test
    void advancedLambda() {
      ClassNode classNode = new ClassNode();
      ClassRemapper classRemapper =
          new ClassRemapper(/* latest api */ Opcodes.ASM9, classNode, remapper);
      classRemapper.visit(Opcodes.V11, Opcodes.ACC_PUBLIC, "C", null, "java/lang/Object", null);
      MethodVisitor methodVisitor =
          classRemapper.visitMethod(Opcodes.ACC_PUBLIC, "hello", "()V", null, null);
      methodVisitor.visitCode();

      func_advancedLambda(methodVisitor);

      InvokeDynamicInsnNode invokeDynamic =
          (InvokeDynamicInsnNode) classNode.methods.get(0).instructions.get(0);
      assertEquals("()Lme/MyRunnable;", invokeDynamic.desc);
      assertEquals("run", invokeDynamic.name);
    }
  }
}
