package net.jbock.compiler;

import dagger.BindsInstance;
import dagger.Component;
import net.jbock.common.OperationMode;

import javax.annotation.processing.ProcessingEnvironment;

@Component(modules = ProcessorModule.class)
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
