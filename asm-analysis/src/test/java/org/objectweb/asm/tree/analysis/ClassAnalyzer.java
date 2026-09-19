package org.objectweb.asm.tree.analysis;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

public final class ClassAnalyzer<V extends Value> extends ClassVisitor {

  private final Analyzer<V> analyzer;
  private String className;

  public ClassAnalyzer(final Analyzer<V> analyzer) {
    super(/* latest */ Opcodes.ASM9, null);
    this.analyzer = analyzer;
  }

  @Override
  public void visit(
      final int version,
      final int access,
      final String name,
      final String signature,
      final String superName,
      final String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces);
    className = name;
  }

  @Override
  public MethodVisitor visitMethod(
      final int access,
      final String name,
      final String descriptor,
      final String signature,
      final String[] exceptions) {
    return new MethodNode(Opcodes.ASM9, access, name, descriptor, signature, exceptions) {
      @Override
      public void visitEnd() {
        try {
          analyzer.analyze(className, this);
        } catch (AnalyzerException e) {
          throw new RuntimeException("analyze exception", e);
        }
        super.visitEnd();
      }
    };
  }
}
