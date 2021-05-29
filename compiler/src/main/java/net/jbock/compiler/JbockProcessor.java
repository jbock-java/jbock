package net.jbock.compiler;

import com.google.auto.common.BasicAnnotationProcessor;
import net.jbock.common.OperationMode;

import javax.lang.model.SourceVersion;
import java.util.List;

import static net.jbock.common.OperationMode.PRODUCTION;

public final class JbockProcessor extends BasicAnnotationProcessor {

  private final OperationMode operationMode;

  public JbockProcessor() {
    this.operationMode = PRODUCTION;
  }

  // visible for testing
  JbockProcessor(boolean test) {
    this.operationMode = OperationMode.valueOf(test);
  }

  @Override
  protected Iterable<? extends Step> steps() {
    ProcessorComponent component = DaggerProcessorComponent.builder()
        .processingEnv(processingEnv)
        .operationMode(operationMode)
        .build();
    return List.of(component.commandProcessingStep(),
        component.converterProcessingStep(),
        component.parameterMethodProcessingStep());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
