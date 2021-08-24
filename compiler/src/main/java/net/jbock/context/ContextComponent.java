package net.jbock.context;

import dagger.Component;

@Component(modules = ContextModule.class)
@ContextScope
public interface ContextComponent {

    ParserClass parserClass();

    ImplClass implClass();

    @Component.Factory
    interface Factory {

        ContextComponent create(ContextModule module);
    }
}
