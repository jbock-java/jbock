package net.jbock.writing;

import dagger.BindsInstance;
import dagger.Subcomponent;

@WritingScope
@Subcomponent
public interface ContextComponent {

    ParserClass parserClass();

    ImplClass implClass();

    @Subcomponent.Factory
    interface Factory {

        ContextComponent create(@BindsInstance CommandRepresentation commandRepresentation);
    }
}
