package net.jbock.context;

import dagger.Component;

@Component(modules = ContextModule.class)
@ContextScope
public interface ContextComponent {

    ParserClass parserClass();

    ImplClass implClass();

    static ContextComponent create(ContextModule module) {
        return DaggerContextComponent.factory().create(module);
    }

    @Component.Factory
    interface Factory {

        ContextComponent create(ContextModule module);
    }
}
