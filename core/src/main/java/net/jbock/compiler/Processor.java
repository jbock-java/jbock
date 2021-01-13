package net.jbock.compiler;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableList;
import dagger.BindsInstance;
import dagger.Component;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;

public final class Processor extends BasicAnnotationProcessor {

  @Override
  protected Iterable<? extends Step> steps() {
    ProcessorComponent component = DaggerProcessor_ProcessorComponent.factory().create(processingEnv);
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

    @Component.Factory
    interface Factory {
      ProcessorComponent create(@BindsInstance ProcessingEnvironment processingEnv);
    }
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
