package net.jbock.compiler;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableList;
import dagger.BindsInstance;
import dagger.Component;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;

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
    return ImmutableList.of(
        component.commandProcessingStep(),
        component.mapperProcessingStep(),
        component.parameterMethodProcessingStep());
  }

  @Component(modules = ProcessingEnvironmentModule.class)
  interface ProcessorComponent {

    CommandProcessingStep commandProcessingStep();

    MapperProcessingStep mapperProcessingStep();

    ParameterMethodProcessingStep parameterMethodProcessingStep();

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
