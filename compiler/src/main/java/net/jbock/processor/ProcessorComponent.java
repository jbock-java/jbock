package net.jbock.processor;

import io.jbock.simple.Component;

import javax.annotation.processing.ProcessingEnvironment;

@Component
public interface ProcessorComponent {

    MethodStep methodStep();

    CommandStep commandStep();

    @Component.Factory
    interface Factory {
        ProcessorComponent create(ProcessingEnvironment env);
    }

    static ProcessorComponent create(ProcessingEnvironment env) {
        return ProcessorComponent_Impl.factory().create(env);
    }
}
