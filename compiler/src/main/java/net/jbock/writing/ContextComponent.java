package net.jbock.writing;

import dagger.BindsInstance;
import dagger.Component;

@WritingScope
@Component
public interface ContextComponent {

    ParserClass parserClass();

    ImplClass implClass();

    static ContextComponent create(CommandRepresentation commandRepresentation) {
        return DaggerContextComponent.factory().create(commandRepresentation);
    }

    @Component.Factory
    interface Factory {

        ContextComponent create(@BindsInstance CommandRepresentation commandRepresentation);
    }
}
