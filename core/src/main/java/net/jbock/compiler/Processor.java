package net.jbock.compiler;

import com.google.auto.common.BasicAnnotationProcessor;
import dagger.BindsInstance;
import dagger.Component;
import net.jbock.scope.EnvironmentScope;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import java.util.List;

public final class Processor extends BasicAnnotationProcessor {

  private final OperationMode operationMode;

  public Processor() {
    this(OperationMode.PRODUCTION);
  }

  // visible for testing
  @SuppressWarnings("unused")
  Processor(boolean test) {
    this(test ? OperationMode.TEST : OperationMode.PRODUCTION);
  }

  private Processor(OperationMode operationMode) {
    this.operationMode = operationMode;
  }

  @Override
  protected Iterable<? extends Step> steps() {
    ProcessorComponent component = DaggerProcessor_ProcessorComponent.builder()
        .processingEnv(processingEnv)
        .operationMode(operationMode)
        .build();
    return List.of(component.commandProcessingStep(),
        component.converterProcessingStep(),
        component.parameterMethodProcessingStep());
  }

  @Component(modules = ProcessingEnvironmentModule.class)
  @EnvironmentScope
  interface ProcessorComponent {

    ParameterMethodProcessingStep parameterMethodProcessingStep();

    ConverterProcessingStep converterProcessingStep();

    CommandProcessingStep commandProcessingStep();

    @Component.Builder
    interface Builder {

      @BindsInstance
      Builder processingEnv(ProcessingEnvironment processingEnvironment);

      @BindsInstance
      Builder operationMode(OperationMode mode);

      ProcessorComponent build();
    }
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
