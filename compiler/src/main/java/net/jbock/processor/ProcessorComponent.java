package net.jbock.processor;

import io.jbock.simple.Component;

import javax.annotation.processing.ProcessingEnvironment;

@Component(omitMockBuilder = true)
interface ProcessorComponent {

    MethodStep methodStep();

    CommandStep commandStep();

    @Component.Factory
    interface Factory {
        ProcessorComponent create(ProcessingEnvironment processingEnvironment);
    }

    static ProcessorComponent create(ProcessingEnvironment processingEnvironment) {
        return ProcessorComponent_Impl.factory().create(processingEnvironment);
    }
}
