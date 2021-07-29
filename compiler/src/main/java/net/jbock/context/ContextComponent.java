package net.jbock.context;

import dagger.Component;

@Component(modules = ContextModule.class)
@ContextScope
public interface ContextComponent {

    GeneratedClass generatedClass();

    @Component.Factory
    interface Factory {

        ContextComponent create(ContextModule module);
    }
}
