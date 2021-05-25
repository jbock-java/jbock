package net.jbock.compiler;

import com.google.auto.common.BasicAnnotationProcessor;
import dagger.BindsInstance;
import dagger.Component;
import net.jbock.scope.ProcessorScope;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import java.util.List;

import static net.jbock.compiler.OperationMode.PRODUCTION;

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
    ProcessorComponent component = DaggerJbockProcessor_ProcessorComponent.builder()
        .processingEnv(processingEnv)
        .operationMode(operationMode)
        .build();
    return List.of(component.commandProcessingStep(),
        component.converterProcessingStep(),
        component.parameterMethodProcessingStep());
  }

  @Component(modules = ProcessingEnvironmentModule.class)
  @ProcessorScope
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
