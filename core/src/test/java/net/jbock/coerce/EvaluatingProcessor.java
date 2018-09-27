package net.jbock.coerce;

import com.google.common.collect.ImmutableSet;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.runners.model.Statement;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.testing.compile.Compilation.Status.SUCCESS;
import static com.google.testing.compile.Compiler.javac;

final class EvaluatingProcessor extends AbstractProcessor {

  private final ContextRunnable base;

  private Throwable thrown;

  interface ContextRunnable {
    void run(Elements elements, Types types);
  }

  static class Builder {
    private final String name;
    private String[] source;

    Builder(String name) {
      this.name = name;
    }

    Builder source(String... source) {
      this.source = source;
      return this;
    }

    void run(ContextRunnable base) {
      EvaluatingProcessor evaluatingProcessor = new EvaluatingProcessor(base);
      Compilation compilation = javac().withProcessors(evaluatingProcessor).compile(
          JavaFileObjects.forSourceLines(name, source));
      checkState(compilation.status().equals(SUCCESS), compilation);
      evaluatingProcessor.throwIfStatementThrew();
    }
  }

  static Builder builder(String name) {
    return new Builder(name);
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
    return ImmutableSet.of("*");
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    // just run the test on the last round after compilation is over
    if (roundEnv.processingOver()) {
      try {
        base.run(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
      } catch (Throwable e) {
        thrown = e;
      }
    }
    return false;
  }

  /**
   * Throws what the base {@link Statement} threw, if anything.
   */
  void throwIfStatementThrew() {
    if (thrown != null) {
      throw new RuntimeException(thrown);
    }
  }
}
