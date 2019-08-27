package net.jbock.compiler;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Assertions;
import org.junit.runners.model.Statement;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.Collections;
import java.util.Set;

import static com.google.testing.compile.Compilation.Status.SUCCESS;
import static com.google.testing.compile.Compiler.javac;

public final class EvaluatingProcessor extends AbstractProcessor {

  private final ContextRunnable base;

  private Throwable thrown;

  static void assertSameType(TypeMirror t1, TypeMirror t2, Types types) {
    boolean sameType = types.isSameType(t1, t2);
    if (!sameType) {
      Assertions.fail("Expecting " + t1 + " but found " + t2);
    }
  }

  public static void assertSameType(String expr, TypeMirror t2, Elements elements, Types types) {
    TypeMirror t1 = TypeExpr.prepare(elements, types).parse(expr);
    assertSameType(t1, t2, types);
  }

  public interface ContextRunnable {
    void run(Elements elements, Types types) throws Exception;
  }

  public static class Builder {

    private final String[] source;

    private Builder(String[] source) {
      this.source = source;
    }

    public void run(ContextRunnable base) {
      run("Dummy", base);
    }

    public void run(String className, ContextRunnable base) {
      EvaluatingProcessor evaluatingProcessor = new EvaluatingProcessor(base);
      Compilation compilation = javac().withProcessors(evaluatingProcessor).compile(
          JavaFileObjects.forSourceLines(className, source));
      if (!compilation.status().equals(SUCCESS)) {
        for (Diagnostic<? extends JavaFileObject> error : compilation.errors()) {
          System.out.println(error);
        }
        Assertions.fail(compilation.status().name());
      }
      evaluatingProcessor.throwIfStatementThrew();
    }
  }

  public static Builder source(String... source) {
    return new Builder(source);
  }

  private EvaluatingProcessor(ContextRunnable base) {
    this.base = base;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Collections.singleton("*");
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    // just run the test on the last round after compilation is over
    if (roundEnv.processingOver()) {
      try {
        base.run(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
      } catch (Throwable e) {
        thrown = e;
      } finally {
        TypeTool.unset();
      }
    }
    return false;
  }

  /**
   * Throws what the base {@link Statement} threw, if anything.
   */
  private void throwIfStatementThrew() {
    if (thrown != null) {
      throw new RuntimeException(thrown);
    }
  }
}
