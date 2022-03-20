package net.jbock.writing;

import dagger.BindsInstance;
import dagger.Component;

@WritingScope
@Component
public interface ContextComponent {

    ParserClass parserClass();

    ImplClass implClass();

    static ContextComponent.Builder builder() {
        return DaggerContextComponent.builder();
    }

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder commandRepresentation(CommandRepresentation commandRepresentation);

        ContextComponent build();
    }
}
